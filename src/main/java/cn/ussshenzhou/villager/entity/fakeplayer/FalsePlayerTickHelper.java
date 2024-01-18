package cn.ussshenzhou.villager.entity.fakeplayer;

import cn.ussshenzhou.t88.task.RepeatTask;
import cn.ussshenzhou.t88.task.Task;
import cn.ussshenzhou.t88.task.TaskHelper;
import cn.ussshenzhou.villager.entity.fakeplayer.events.BotDamageByPlayerEvent;
import cn.ussshenzhou.villager.entity.fakeplayer.events.BotKilledByPlayerEvent;
import cn.ussshenzhou.villager.entity.fakeplayer.events.FalsePlayerLocateTargetEvent;
import cn.ussshenzhou.villager.entity.fakeplayer.utils.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("MapOrSetKeyShouldOverrideHashCodeEquals")
public class FalsePlayerTickHelper {
    private static final Map<LivingEntity, Vec3> BT_LIST = new HashMap<>();
    private static final Map<LivingEntity, Boolean> BT_CHECK = new HashMap<>();
    private static EnumTargetGoal goal = EnumTargetGoal.NEAREST_VULNERABLE_PLAYER;
    private static AABB region;
    private static double regionWeightX;
    private static double regionWeightY;
    private static double regionWeightZ;
    public static final Set<FalsePlayer> NO_FACE = new HashSet<>();
    public static final Set<LivingEntity> NO_JUMP = new HashSet<>();
    public static final Set<FalsePlayer> SLOW = new HashSet<>();
    private static final Map<LivingEntity, Task> MINING_ANIM = new HashMap<>();
    private static final Set<FalsePlayer> BOAT_COOLDOWN = new HashSet<>();
    private static final Set<Boat> BOATS = new HashSet<>();
    public static boolean offsets = false;
    private static final Map<LivingEntity, Vec3> TOWER_LIST = new HashMap<>();
    private static final Map<BlockPos, Short> CRACK_LIST = new HashMap<>();
    private static final Map<RepeatTask, Byte> MINING = new HashMap<>();
    protected static final Set<Task> TASK_LIST = new HashSet<>();
    private static final Set<FalsePlayer> FALL_DAMAGE_COOLDOWN = new HashSet<>();

    private static final double ATTACK_MOVE_SPEED = 0.15;
    private static final int ATTACK_SPEED = 7;

    public static void remove(FalsePlayer falsePlayer) {
        try {
            MINING_ANIM.get(falsePlayer).cancel();
        } catch (NullPointerException ignored) {
        }
        List.of(BT_LIST, BT_CHECK, NO_FACE, NO_JUMP, SLOW, MINING_ANIM, BOAT_COOLDOWN, TOWER_LIST, CRACK_LIST, FALL_DAMAGE_COOLDOWN).forEach(
                o -> {
                    if (o instanceof Map<?, ?> map) {
                        map.remove(falsePlayer);
                    } else if (o instanceof Collection<?> collection) {
                        collection.remove(falsePlayer);
                    }
                }
        );
    }

    public static void tick(FalsePlayer falsePlayer) {
        if (!falsePlayer.isAlive()) {
            return;
        }

        if (falsePlayer.tickDelay(20)) {
            center(falsePlayer);
        }
        var level = falsePlayer.level();
        Vec3 loc = Vec3Helper.clone(falsePlayer.position());
        LivingEntity livingTarget = locateTarget(falsePlayer, loc);

        LegacyBlockCheck.tryPreMLG(falsePlayer, loc);

        if (livingTarget == null) {
            stopMining(falsePlayer);
            return;
        }

        LegacyBlockCheck.clutch(falsePlayer, livingTarget);

        fallDamageCheck(falsePlayer);
        miscellaneousChecks(falsePlayer, livingTarget);

        BlockPos target = offsets ? livingTarget.getOnPos().offset(falsePlayer.getOnPos()) : livingTarget.getOnPos();

        if (falsePlayer.tickDelay(ATTACK_SPEED) && !MINING_ANIM.containsKey(falsePlayer)) {
            BlockPos botEyeLoc = Vec3Helper.blockPos(falsePlayer.getEyePosition());
            BlockPos playerEyeLoc = Vec3Helper.blockPos(livingTarget.getEyePosition());
            BlockPos playerLoc = Vec3Helper.blockPos(livingTarget.position());

            if (LegacyUtils.checkFreeSpace(botEyeLoc, playerEyeLoc, level) || LegacyUtils.checkFreeSpace(botEyeLoc, playerLoc, level)) {
                attack(falsePlayer, livingTarget, loc);
            }
        }

        boolean waterGround = LegacyMats.WATER.contains(level.getBlockState(Vec3Helper.blockPos(loc.add(0, -0.1, 0))).getBlock())
                && !LegacyMats.AIR.contains(level.getBlockState(Vec3Helper.blockPos(loc.add(0, -0.6, 0))).getBlock());

        boolean withinTargetXZ = false, sameXZ = false;

        if (BT_CHECK.containsKey(falsePlayer)) {
            sameXZ = BT_CHECK.get(falsePlayer);
        }

        if (waterGround || falsePlayer.isBotOnGround() || onBoat(falsePlayer)) {
            byte sideResult = 1;

            if (TOWER_LIST.containsKey(falsePlayer)) {
                if (loc.y > livingTarget.position().y) {
                    TOWER_LIST.remove(falsePlayer);
                    resetHand(falsePlayer, livingTarget, falsePlayer);
                }
            }

            var blockPos = Vec3Helper.blockPos(loc.add(0, 1, 0));
            Block block = level.getBlockState(blockPos).getBlock();

            if (Math.abs(loc.x - target.getX()) <= 3 &&
                    Math.abs(loc.z - target.getZ()) <= 3) {
                withinTargetXZ = true;
            }

            boolean bothXZ = withinTargetXZ || sameXZ;

            if (checkAt(falsePlayer, blockPos, block, falsePlayer)) {
                return;
            }

            var pos = Vec3Helper.blockPos(loc);
            if (checkFenceAndGates(falsePlayer, pos, falsePlayer)) {
                return;
            }

            if (checkObstacles(falsePlayer, pos, falsePlayer)) {
                return;
            }

            if (checkDown(falsePlayer, falsePlayer, livingTarget.getOnPos(), bothXZ)) {
                return;
            }

            if ((withinTargetXZ || sameXZ) && checkUp(falsePlayer, livingTarget, falsePlayer, target, withinTargetXZ, sameXZ)) {
                return;
            }

            if (bothXZ) {
                sideResult = checkSide(falsePlayer, livingTarget, falsePlayer);
            }

            switch (sideResult) {
                case 1:
                    resetHand(falsePlayer, livingTarget, falsePlayer);
                    if (!NO_JUMP.contains(falsePlayer) && !waterGround) {
                        move(falsePlayer, livingTarget, loc, target);
                    }
                    return;

                case 2:
                    if (!waterGround) {
                        move(falsePlayer, livingTarget, loc, target);
                    }
            }
        } else if (LegacyMats.WATER.contains(level.getBlockState(Vec3Helper.blockPos(loc)).getBlock())) {
            swim(falsePlayer, target, falsePlayer, livingTarget, LegacyMats.WATER.contains(level.getBlockState(Vec3Helper.blockPos(loc).offset(0, -1, 0)).getBlock()));
        }
    }

    private static void swim(FalsePlayer falsePlayer, BlockPos loc, LivingEntity playerNPC, LivingEntity target, boolean anim) {
        if (playerNPC instanceof Player) {
            ((Player) playerNPC).setSprinting(false);
        }

        var at = falsePlayer.getOnPos();

        var vector = loc.getCenter().subtract(at.getCenter());
        if (at.getY() < target.getOnPos().getY()) {
            vector.y = (0);
        }

        vector.normalize().multiply(0.05, 0.05, 0.05);
        vector.y = (vector.y * 1.2);

        if (MINING_ANIM.containsKey(playerNPC)) {
            var task = MINING_ANIM.get(playerNPC);
            if (task != null) {
                task.cancel();
                MINING_ANIM.remove(playerNPC);
            }
        }

        if (anim) {
            falsePlayer.swim();
        } else {
            vector.y = (0);
            vector.multiply(0.7, 0.7, 0.7);
        }

        falsePlayer.face(target.position());
        falsePlayer.addVelocity(vector);
    }

    private static void move(FalsePlayer falsePlayer, LivingEntity livingTarget, Vec3 position, BlockPos target) {
        Vec3 vel = target.getCenter().subtract(position).normalize().multiply(ATTACK_MOVE_SPEED, ATTACK_MOVE_SPEED, ATTACK_MOVE_SPEED);

        if (falsePlayer.tickDelay(5)) {
            falsePlayer.face(livingTarget.position());
        }
        if (!falsePlayer.isBotOnGround()) {
            return; // calling this a second time later on
        }

        falsePlayer.stand(); // eventually create a memory system so packets do not have to be sent every tick
        falsePlayer.setItem(falsePlayer.defaultWeapon); // method to check item in main hand, falsePlayer.getItemInHand()

        try {
            vel.add(falsePlayer.getVelocity());
        } catch (IllegalArgumentException e) {
            if (MathUtils.isNotFinite(vel)) {
                MathUtils.clean(vel);
            }
        }

        if (vel.length() > 1) {
            vel.normalize();
        }

        double distance = position.distanceTo(target.getCenter());

        if (distance <= 5) {
            vel.multiply(0.3, 0.3, 0.3);
        } else {
            vel.multiply(0.4, 0.4, 0.4);
        }

        if (SLOW.contains(falsePlayer)) {
            vel.y = (0);
            vel.multiply(0.5, 0.5, 0.5);
        } else {
            if (falsePlayer.getRandom().nextFloat() < 0.2) {
                vel.y = (0.4);
            }
        }

        vel.y = (vel.y - Math.random() * 0.05);

        falsePlayer.jump(vel);
    }

    private static byte checkSide(FalsePlayer falsePlayer, LivingEntity target, LivingEntity playerNPC) {  // make it so they don't jump when checking side
        var a = playerNPC.getEyePosition();
        var b = target.position().add(0, 1, 0);

        if (falsePlayer.position().distanceToSqr(target.position()) < 2.9 * 2.9 && LegacyUtils.checkFreeSpace(Vec3Helper.blockPos(a), Vec3Helper.blockPos(b), falsePlayer.level())) {
            resetHand(falsePlayer, target, playerNPC);
            return 1;
        }

        LegacyLevel legacyLevel = checkNearby(target, falsePlayer);

        if (legacyLevel == null) {
            resetHand(falsePlayer, target, playerNPC);
            return 1;
        } else if (legacyLevel.isSide() || legacyLevel == LegacyLevel.BELOW || legacyLevel == LegacyLevel.ABOVE) {
            return 0;
        } else {
            return 2;
        }
    }

    private static LegacyLevel checkNearby(LivingEntity target, FalsePlayer falsePlayer) {

        falsePlayer.face(target.position());

        var dir = falsePlayer.getDirection();
        LegacyLevel legacyLevel = null;
        Block get = null;

        var box = falsePlayer.getBoundingBox();
        double[] xVals = new double[]{
                box.minX,
                box.maxX - 0.01
        };

        double[] zVals = new double[]{
                box.minZ,
                box.maxZ - 0.01
        };
        List<BlockPos> locStanding = new ArrayList<>();
        for (double x : xVals) {
            for (double z : zVals) {
                var loc = new BlockPos(Mth.floor(x), falsePlayer.getOnPos().getY(), Mth.floor(z));
                if (!locStanding.contains(loc)) {
                    locStanding.add(loc);
                }
            }
        }
        locStanding.sort(Comparator.comparingDouble(a -> BotUtils.getHorizSqDist(a, falsePlayer.getOnPos())));
        var level = falsePlayer.level();
        //Break potential obstructing walls
        for (var pos : locStanding) {
            boolean up = false;
            get = level.getBlockState(pos).getBlock();
            if (!LegacyMats.FENCE.contains(get)) {
                up = true;
                pos = pos.offset(0, 1, 0);
                get = level.getBlockState(pos).getBlock();
                if (!LegacyMats.FENCE.contains(get)) {
                    get = null;
                }
            }

            if (get != null) {
                int distanceX = pos.getX() - falsePlayer.getOnPos().getX();
                int distanceZ = pos.getZ() - falsePlayer.getOnPos().getZ();
                if (distanceX == 1 && distanceZ == 0) {
                    if (dir == Direction.NORTH || dir == Direction.SOUTH) {
                        falsePlayer.face(pos.getCenter());
                        legacyLevel = up ? LegacyLevel.EAST : LegacyLevel.EAST_D;
                    }
                } else if (distanceX == -1 && distanceZ == 0) {
                    if (dir == Direction.NORTH || dir == Direction.SOUTH) {
                        falsePlayer.face(pos.getCenter());
                        legacyLevel = up ? LegacyLevel.WEST : LegacyLevel.WEST_D;
                    }
                } else if (distanceX == 0 && distanceZ == 1) {
                    if (dir == Direction.EAST || dir == Direction.WEST) {
                        falsePlayer.face(pos.getCenter());
                        legacyLevel = up ? LegacyLevel.SOUTH : LegacyLevel.SOUTH_D;
                    }
                } else if (distanceX == 0 && distanceZ == -1) {
                    if (dir == Direction.EAST || dir == Direction.WEST) {
                        falsePlayer.face(pos.getCenter());
                        legacyLevel = up ? LegacyLevel.NORTH : LegacyLevel.NORTH_D;
                    }
                }

                if (legacyLevel != null) {
                    preBreak(falsePlayer, falsePlayer, pos, get, legacyLevel);
                    return legacyLevel;
                }
            }
        }
        BlockPos getPos = falsePlayer.getOnPos();
        switch (dir) {
            case NORTH:
                getPos = falsePlayer.getOnPos().offset(0, 1, -1);
                get = level.getBlockState(getPos).getBlock();
                if (checkSideBreak(get)) {
                    legacyLevel = LegacyLevel.NORTH;
                } else if (checkSideBreak(level.getBlockState(getPos.offset(0, -1, 0)).getBlock())) {
                    getPos = getPos.offset(0, -1, 0);
                    get = level.getBlockState(getPos).getBlock();
                    legacyLevel = LegacyLevel.NORTH_D;
                } else if (LegacyMats.FENCE.contains(level.getBlockState(getPos.offset(0, -2, 0)).getBlock())) {
                    getPos = getPos.offset(0, -2, 0);
                    get = level.getBlockState(getPos).getBlock();
                    legacyLevel = LegacyLevel.NORTH_D_2;
                } else {
                    var standingPos = falsePlayer.getStandingOn().isEmpty() ? null : falsePlayer.getStandingOn().get(0);
                    Block standing = standingPos == null ? null : level.getBlockState(standingPos).getBlock();
                    if (standing == null) {
                        break;
                    }
                    boolean obstructed = standingPos.getY() == falsePlayer.getOnPos().getY()
                            || (standingPos.getY() + 1 == falsePlayer.getOnPos().getY()
                            && (LegacyMats.FENCE.contains(standing) || LegacyMats.GATES.contains(standing)));
                    if (obstructed) {
                        var belowStandingPos = standingPos.offset(0, -1, 0);
                        Block belowStanding = level.getBlockState(belowStandingPos).getBlock();
                        if (!LegacyMats.BREAK.contains(belowStanding) && !LegacyMats.NONSOLID.contains(belowStanding)) {
                            //Break standing block
                            get = standing;
                            legacyLevel = LegacyLevel.BELOW;
                        } else {
                            //Break above
                            Block above = level.getBlockState(falsePlayer.getOnPos().offset(0, 2, 0)).getBlock();
                            Block aboveSide = level.getBlockState(getPos.offset(0, 1, 0)).getBlock();
                            if (!LegacyMats.BREAK.contains(above)) {
                                get = above;
                                legacyLevel = LegacyLevel.ABOVE;
                            } else if (!LegacyMats.BREAK.contains(aboveSide)) {
                                get = aboveSide;
                                legacyLevel = LegacyLevel.NORTH_U;
                            }
                        }
                    }
                }
                break;
            case SOUTH:
                getPos = falsePlayer.getOnPos().offset(0, 1, 1);
                get = level.getBlockState(getPos).getBlock();
                if (checkSideBreak(get)) {
                    legacyLevel = LegacyLevel.SOUTH;
                } else if (checkSideBreak(level.getBlockState(getPos.offset(0, -1, 0)).getBlock())) {
                    getPos = getPos.offset(0, -1, 0);
                    get = level.getBlockState(getPos).getBlock();
                    legacyLevel = LegacyLevel.SOUTH_D;
                } else if (LegacyMats.FENCE.contains(level.getBlockState(getPos.offset(0, -2, 0)).getBlock())) {
                    getPos = getPos.offset(0, -2, 0);
                    get = level.getBlockState(getPos).getBlock();
                    legacyLevel = LegacyLevel.SOUTH_D_2;
                } else {
                    var standingPos = falsePlayer.getStandingOn().isEmpty() ? null : falsePlayer.getStandingOn().get(0);
                    Block standing = standingPos == null ? null : level.getBlockState(standingPos).getBlock();
                    if (standing == null) {
                        break;
                    }
                    boolean obstructed = standingPos.getY() == falsePlayer.getOnPos().getY()
                            || (standingPos.getY() + 1 == falsePlayer.getOnPos().getY()
                            && (LegacyMats.FENCE.contains(standing) || LegacyMats.GATES.contains(standing)));
                    if (obstructed) {
                        var belowStandingPos = standingPos.offset(0, -1, 0);
                        Block belowStanding = level.getBlockState(belowStandingPos).getBlock();
                        if (!LegacyMats.BREAK.contains(belowStanding) && !LegacyMats.NONSOLID.contains(belowStanding)) {
                            //Break standing block
                            get = standing;
                            legacyLevel = LegacyLevel.BELOW;
                        } else {
                            //Break above
                            Block above = level.getBlockState(falsePlayer.getOnPos().offset(0, 2, 0)).getBlock();
                            Block aboveSide = level.getBlockState(getPos.offset(0, 1, 0)).getBlock();
                            if (!LegacyMats.BREAK.contains(above)) {
                                get = above;
                                legacyLevel = LegacyLevel.ABOVE;
                            } else if (!LegacyMats.BREAK.contains(aboveSide)) {
                                get = aboveSide;
                                legacyLevel = LegacyLevel.SOUTH_U;
                            }
                        }
                    }
                }
                break;
            case EAST:
                getPos = falsePlayer.getOnPos().offset(1, 1, 0);
                get = level.getBlockState(getPos).getBlock();
                if (checkSideBreak(get)) {
                    legacyLevel = LegacyLevel.EAST;
                } else if (checkSideBreak(level.getBlockState(getPos.offset(0, -1, 0)).getBlock())) {
                    getPos = getPos.offset(0, -1, 0);
                    get = level.getBlockState(getPos).getBlock();
                    legacyLevel = LegacyLevel.EAST_D;
                } else if (LegacyMats.FENCE.contains(level.getBlockState(getPos.offset(0, -2, 0)).getBlock())) {
                    getPos = getPos.offset(0, -2, 0);
                    get = level.getBlockState(getPos).getBlock();
                    legacyLevel = LegacyLevel.EAST_D_2;
                } else {
                    var standingPos = falsePlayer.getStandingOn().isEmpty() ? null : falsePlayer.getStandingOn().get(0);
                    Block standing = standingPos == null ? null : level.getBlockState(standingPos).getBlock();
                    if (standing == null) {
                        break;
                    }
                    boolean obstructed = standingPos.getY() == falsePlayer.getOnPos().getY()
                            || (standingPos.getY() + 1 == falsePlayer.getOnPos().getY()
                            && (LegacyMats.FENCE.contains(standing) || LegacyMats.GATES.contains(standing)));
                    if (obstructed) {
                        var belowStandingPos = standingPos.offset(0, -1, 0);
                        Block belowStanding = level.getBlockState(belowStandingPos).getBlock();
                        if (!LegacyMats.BREAK.contains(belowStanding) && !LegacyMats.NONSOLID.contains(belowStanding)) {
                            //Break standing block
                            get = standing;
                            legacyLevel = LegacyLevel.BELOW;
                        } else {
                            //Break above
                            Block above = level.getBlockState(falsePlayer.getOnPos().offset(0, 2, 0)).getBlock();
                            Block aboveSide = level.getBlockState(getPos.offset(0, 1, 0)).getBlock();
                            if (!LegacyMats.BREAK.contains(above)) {
                                get = above;
                                legacyLevel = LegacyLevel.ABOVE;
                            } else if (!LegacyMats.BREAK.contains(aboveSide)) {
                                get = aboveSide;
                                legacyLevel = LegacyLevel.EAST_U;
                            }
                        }
                    }
                }
                break;
            case WEST:
                getPos = falsePlayer.getOnPos().offset(-1, 1, 0);
                get = level.getBlockState(getPos).getBlock();
                if (checkSideBreak(get)) {
                    legacyLevel = LegacyLevel.WEST;
                } else if (checkSideBreak(level.getBlockState(getPos.offset(0, -1, 0)).getBlock())) {
                    getPos = getPos.offset(0, -1, 0);
                    get = level.getBlockState(getPos).getBlock();
                    legacyLevel = LegacyLevel.WEST_D;
                } else if (LegacyMats.FENCE.contains(level.getBlockState(getPos.offset(0, -2, 0)).getBlock())) {
                    getPos = getPos.offset(0, -2, 0);
                    get = level.getBlockState(getPos).getBlock();
                    legacyLevel = LegacyLevel.WEST_D_2;
                } else {
                    var standingPos = falsePlayer.getStandingOn().isEmpty() ? null : falsePlayer.getStandingOn().get(0);
                    Block standing = standingPos == null ? null : level.getBlockState(standingPos).getBlock();
                    if (standing == null) {
                        break;
                    }
                    boolean obstructed = standingPos.getY() == falsePlayer.getOnPos().getY()
                            || (standingPos.getY() + 1 == falsePlayer.getOnPos().getY()
                            && (LegacyMats.FENCE.contains(standing) || LegacyMats.GATES.contains(standing)));
                    if (obstructed) {
                        var belowStandingPos = standingPos.offset(0, -1, 0);
                        Block belowStanding = level.getBlockState(belowStandingPos).getBlock();
                        if (!LegacyMats.BREAK.contains(belowStanding) && !LegacyMats.NONSOLID.contains(belowStanding)) {
                            //Break standing block
                            get = standing;
                            legacyLevel = LegacyLevel.BELOW;
                        } else {
                            //Break above
                            Block above = level.getBlockState(falsePlayer.getOnPos().offset(0, 2, 0)).getBlock();
                            Block aboveSide = level.getBlockState(getPos.offset(0, 1, 0)).getBlock();
                            if (!LegacyMats.BREAK.contains(above)) {
                                get = above;
                                legacyLevel = LegacyLevel.ABOVE;
                            } else if (!LegacyMats.BREAK.contains(aboveSide)) {
                                get = aboveSide;
                                legacyLevel = LegacyLevel.WEST_U;
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }

        if (legacyLevel == LegacyLevel.EAST_D || legacyLevel == LegacyLevel.WEST_D || legacyLevel == LegacyLevel.NORTH_D || legacyLevel == LegacyLevel.SOUTH_D
                || legacyLevel == LegacyLevel.EAST_D_2 || legacyLevel == LegacyLevel.WEST_D_2 || legacyLevel == LegacyLevel.NORTH_D_2 || legacyLevel == LegacyLevel.SOUTH_D_2) {
            if (LegacyMats.AIR.contains(level.getBlockState(falsePlayer.getOnPos().offset(0, 2, 0)).getBlock())
                    && LegacyMats.AIR.contains(level.getBlockState(getPos.offset(0, 2, 0)).getBlock())
                    && !LegacyMats.FENCE.contains(get) && !LegacyMats.GATES.contains(get)) {
                return null;
            }
        }
        if (legacyLevel == LegacyLevel.ABOVE || legacyLevel == LegacyLevel.BELOW) {
            Block check = switch (dir) {
                case NORTH -> level.getBlockState(falsePlayer.getOnPos().offset(0, 2, -1)).getBlock();
                case SOUTH -> level.getBlockState(falsePlayer.getOnPos().offset(0, 2, 1)).getBlock();
                case EAST -> level.getBlockState(falsePlayer.getOnPos().offset(1, 2, 0)).getBlock();
                case WEST -> level.getBlockState(falsePlayer.getOnPos().offset(-1, 2, 0)).getBlock();
                default -> null;
            };

            if (LegacyMats.AIR.contains(level.getBlockState(falsePlayer.getOnPos().offset(0, 2, 0)).getBlock())
                    && LegacyMats.AIR.contains(check)) {
                return null;
            }
        }

        if (legacyLevel != null) {
            if (legacyLevel == LegacyLevel.BELOW) {
                NO_JUMP.add(falsePlayer);
                TaskHelper.addServerTask(() -> {
                    NO_JUMP.remove(falsePlayer);
                }, 15);

                falsePlayer.look(BlockFace.DOWN);
                downMine(falsePlayer, falsePlayer, get);
            } else if (legacyLevel == LegacyLevel.ABOVE) {
                falsePlayer.look(BlockFace.UP);
            }
            preBreak(falsePlayer, falsePlayer, getPos, get, legacyLevel);
        }

        return legacyLevel;
    }

    private static boolean checkSideBreak(Block type) {
        return !LegacyMats.BREAK.contains(type);// && !LegacyMats.LEAVES.contains(type);
    }

    private static boolean checkUp(FalsePlayer falsePlayer, LivingEntity target, LivingEntity playerNPC, BlockPos loc, boolean c, boolean sameXZ) {
        var a = Vec3Helper.clone(playerNPC.position());
        var b = Vec3Helper.clone(target.position());

        a.y = 0;
        b.y = 0;

        boolean above = LegacyWorldManager.aboveGround(playerNPC.level(), playerNPC.getOnPos());

        Direction dir = playerNPC.getDirection();
        BlockPos getPos = switch (dir) {
            case NORTH -> playerNPC.getOnPos().offset(0, 1, -1);
            case SOUTH -> playerNPC.getOnPos().offset(0, 1, 1);
            case EAST -> playerNPC.getOnPos().offset(1, 1, 0);
            case WEST -> playerNPC.getOnPos().offset(-1, 1, 0);
            default -> null;
        };
        if (getPos == null) {
            return false;
        }
        Block get = falsePlayer.level().getBlockState(getPos).getBlock();


        if (LegacyMats.BREAK.contains(get)) {
            if (a.distanceToSqr(b) >= 16 * 16 && above) {
                return false;
            }
        }

        if (playerNPC.getOnPos().getY() < target.getOnPos().getY() - 1) {
            var m0 = playerNPC.level().getBlockState(playerNPC.getOnPos()).getBlock();
            var m1 = playerNPC.level().getBlockState(playerNPC.getOnPos().offset(0, 1, 0)).getBlock();
            var m2 = playerNPC.level().getBlockState(playerNPC.getOnPos().offset(0, 2, 0)).getBlock();

            if (LegacyMats.BREAK.contains(m0) && LegacyMats.BREAK.contains(m1) && LegacyMats.BREAK.contains(m2)) {

                falsePlayer.setItem(new ItemStack(Blocks.COBBLESTONE));

                if (MINING_ANIM.containsKey(playerNPC)) {
                    var task = MINING_ANIM.get(playerNPC);
                    if (task != null) {
                        task.cancel();
                        MINING_ANIM.remove(playerNPC);
                    }
                }

                falsePlayer.look(BlockFace.DOWN);

                // maybe put this in lower if statement onGround()
                if (m0 != Blocks.WATER) {
                    TaskHelper.addServerTask(() -> {
                        falsePlayer.sneak();
                        falsePlayer.setItem(new ItemStack(Blocks.COBBLESTONE));
                        falsePlayer.punch();
                        falsePlayer.look(BlockFace.DOWN);

                        TaskHelper.addServerTask(() -> {
                            falsePlayer.look(BlockFace.DOWN);
                        }, 1);
                        Block place = playerNPC.level().getBlockState(playerNPC.getOnPos()).getBlock();
                        LegacyBlockCheck.placeBlock(falsePlayer, playerNPC, place, playerNPC.getOnPos());
                        if (!TOWER_LIST.containsKey(playerNPC)) {
                            if (c) {
                                TOWER_LIST.put(playerNPC, playerNPC.position());
                            }
                        }
                    }, 3);
                }

                if (falsePlayer.isBotOnGround()) {
                    if (target.position().distanceToSqr(playerNPC.position()) < 16 * 16) {
                        if (NO_JUMP.contains(playerNPC)) {

                            TaskHelper.addServerTask(() -> {
                                falsePlayer.setVelocity(new Vec3(0, 0.5, 0));
                            }, 1);

                        } else {
                            Vec3 vector = loc.getCenter().subtract(playerNPC.getOnPos().getCenter()).normalize();
                            falsePlayer.stand();

                            Vec3 move = falsePlayer.getVelocity().add(vector);
                            if (move.length() > 1) {
                                move = move.normalize();
                            }
                            move.multiply(0.1, 0.1, 0.1);
                            move.y = 0.5;

                            falsePlayer.setVelocity(move);
                            return true;
                        }
                    } else {
                        if (falsePlayer.isBotOnGround()) {
                            Vec3 locBlock = Vec3Helper.clone(playerNPC.position());
                            locBlock.x = locBlock.x + 0.5;
                            locBlock.z = locBlock.z + 0.5;

                            Vec3 vector = locBlock.subtract(playerNPC.position());
                            if (vector.length() > 1) {
                                vector = vector.normalize();
                            }
                            vector.multiply(0.1, 0.1, 0.1);
                            vector.y = 0.5;

                            falsePlayer.addVelocity(vector);
                            return true;
                        }
                    }
                }

                return false;

            } else if (LegacyMats.BREAK.contains(m0) && LegacyMats.BREAK.contains(m1) && !LegacyMats.BREAK.contains(m2)) {
                var level = falsePlayer.level();
                var blockPos = falsePlayer.getOnPos().offset(0, 2, 0);
                Block block = level.getBlockState(blockPos).getBlock();
                falsePlayer.look(BlockFace.UP);
                preBreak(falsePlayer, playerNPC, blockPos, block, LegacyLevel.ABOVE);

                if (falsePlayer.isBotOnGround()) {
                    var locBlock = Vec3Helper.clone(playerNPC.position());
                    locBlock.x = locBlock.x + 0.5;
                    locBlock.z = locBlock.z + 0.5;

                    Vec3 vector = locBlock.subtract(playerNPC.position());
                    if (vector.length() > 1) {
                        vector = vector.normalize();
                    }
                    vector.multiply(0.1, 0.1, 0.1);
                    vector.y = 0;

                    falsePlayer.addVelocity(vector);
                }

                return true;
            } else if (sameXZ && LegacyMats.BREAK.contains(m1)) {
                var level = falsePlayer.level();
                var blockPos = falsePlayer.getStandingOn().get(0);
                Block block = blockPos == null ? null : level.getBlockState(falsePlayer.getStandingOn().get(0)).getBlock();
                if (block != null && blockPos.getY() == playerNPC.getOnPos().getY()
                        && !LegacyMats.BREAK.contains(block)) {
                    falsePlayer.look(BlockFace.DOWN);

                    downMine(falsePlayer, playerNPC, block);
                    preBreak(falsePlayer, playerNPC, blockPos, block, LegacyLevel.BELOW);
                    return true;
                }
            }
        }

        return false;
    }

    @SuppressWarnings("DuplicatedCode")
    private static boolean checkDown(FalsePlayer falsePlayer, LivingEntity player, BlockPos loc, boolean c) { // possibly a looser check for c

        if (LegacyUtils.checkFreeSpace(falsePlayer.getOnPos(), loc, falsePlayer.level()) || LegacyUtils.checkFreeSpace(Vec3Helper.blockPos(player.getEyePosition()), loc, falsePlayer.level())) {
            return false;
        }

        if (c && falsePlayer.getOnPos().getY() > loc.getY() + 1) {
            Block block = falsePlayer.getStandingOn().isEmpty() ? null : falsePlayer.level().getBlockState(falsePlayer.getStandingOn().get(0)).getBlock();
            if (block == null) {
                return false;
            }
            falsePlayer.look(BlockFace.DOWN);
            var pos = falsePlayer.getStandingOn().get(0);
            downMine(falsePlayer, player, block);
            preBreak(falsePlayer, player, pos, block, LegacyLevel.BELOW);
            return true;
        } else {
            Vec3 a = loc.getCenter();
            Vec3 b = Vec3Helper.clone(player.position());

            a.y = 0;
            b.y = 0;

            if (falsePlayer.position().y > loc.getY() + 10 && a.distanceToSqr(b) < 10 * 10) {
                Block block = falsePlayer.getStandingOn().isEmpty() ? null : falsePlayer.level().getBlockState(falsePlayer.getStandingOn().get(0)).getBlock();
                if (block == null) {
                    return false;
                }
                falsePlayer.look(BlockFace.DOWN);

                var pos = falsePlayer.getStandingOn().get(0);
                downMine(falsePlayer, player, block);
                preBreak(falsePlayer, player, pos, block, LegacyLevel.BELOW);
                return true;

            } else {
                return false;
            }
        }
    }

    private static void downMine(FalsePlayer npc, LivingEntity player, Block block) {
        if (!LegacyMats.NO_CRACK.contains(block)) {
            var locBlock = Vec3Helper.clone(player.position());
            locBlock.x = locBlock.x() + 0.5;
            locBlock.z = locBlock.z() + 0.5;

            var vector = locBlock.subtract(player.position());
            if (vector.length() > 1) {
                vector = vector.normalize();
            }
            vector.y = 0;
            vector.multiply(0.1, 0.1, 0.1);
            npc.setVelocity(vector);
        }

        if (npc.isBotInWater()) {
            var locBlock = Vec3Helper.clone(player.position());
            locBlock.x = locBlock.x() + 0.5;
            locBlock.z = locBlock.z() + 0.5;

            var vector = locBlock.subtract(player.position());
            if (vector.length() > 1) {
                vector = vector.normalize();
            }
            vector.multiply(0.3, 0.3, 0.3);
            vector.y = -1;

            if (!FALL_DAMAGE_COOLDOWN.contains(npc)) {
                FALL_DAMAGE_COOLDOWN.add(npc);

                TaskHelper.addServerTask(task -> FALL_DAMAGE_COOLDOWN.remove(npc), 10);
            }

            npc.setVelocity(vector);
        }
    }

    private static boolean checkObstacles(FalsePlayer falsePlayer, BlockPos pos, LivingEntity player) {
        var block = falsePlayer.level().getBlockState(pos);
        if (LegacyMats.OBSTACLES.contains(block.getBlock()) || isDoorObstacle(falsePlayer.level(), pos, block)) {
            preBreak(falsePlayer, player, pos, block.getBlock(), LegacyLevel.AT_D);
            return true;
        }

        return false;
    }

    private static boolean isDoorObstacle(Level level, BlockPos pos, BlockState block) {
        if (block.getBlock().getClass() == DoorBlock.class) {
            return true;
        }
        //noinspection RedundantIfStatement
        if (block.getBlock().getClass() == TrapDoorBlock.class && block.getValue(BlockStateProperties.OPEN)) {
            return true;
        }
        return false;
    }

    private static boolean checkFenceAndGates(FalsePlayer falsePlayer, BlockPos pos, LivingEntity player) {
        var block = falsePlayer.level().getBlockState(pos);
        if (LegacyMats.FENCE.contains(block.getBlock()) || LegacyMats.GATES.contains(block.getBlock())) {
            preBreak(falsePlayer, player, pos, block.getBlock(), LegacyLevel.AT_D);
            return true;
        }

        return false;
    }

    private static boolean checkAt(FalsePlayer falsePlayer, BlockPos pos, Block block, LivingEntity player) {
        if (LegacyMats.BREAK.contains(block)) {
            return false;
        } else {
            preBreak(falsePlayer, player, pos, block, LegacyLevel.AT);
            return true;
        }
    }

    private static void preBreak(FalsePlayer falsePlayer, LivingEntity player, BlockPos pos, Block block, LegacyLevel level) {
        Item item;

        if (LegacyMats.SHOVEL.contains(block)) {
            item = LegacyItems.SHOVEL;
        } else if (LegacyMats.AXE.contains(block)) {
            item = LegacyItems.AXE;
        } else {
            item = LegacyItems.PICKAXE;
        }

        falsePlayer.setItem(new ItemStack(item));

        if (level.isSideDown() || level.isSideDown2()) {
            falsePlayer.setBotPitch(69);

            TaskHelper.addServerTask(t -> {
                BT_CHECK.put(player, true);
            }, 5);
        } else if (level.isSideUp()) {
            falsePlayer.setBotPitch(-53);
        } else if (level == LegacyLevel.AT_D || level == LegacyLevel.AT) {
            var blockLoc = new Vec3(pos.getX() + 0.5, pos.getY() - 1, pos.getZ() + 0.5);
            falsePlayer.face(blockLoc);
        }

        if (!MINING_ANIM.containsKey(player)) {
            var task = TaskHelper.addServerRepeatTask(t -> falsePlayer.punch(), 0, 4);
            MINING_ANIM.put(player, task);
        }

        blockBreakEffect(falsePlayer, player, pos, block, new LegacyLevel.LevelWrapper(level));
    }

    private static void blockBreakEffect(FalsePlayer falsePlayer, LivingEntity player, BlockPos pos, Block block, LegacyLevel.LevelWrapper wrapper) {

        if (LegacyMats.NO_CRACK.contains(block)) {
            return;
        }

        if (!CRACK_LIST.containsKey(pos)) {
            RepeatTask repeatTask = new RepeatTask(thiz -> {
                byte i = MINING.get((RepeatTask) thiz);
                var level = falsePlayer.level();
                Block cur;
                BlockPos curPos;
                if (wrapper.getLevel() == null) {
                    curPos = Vec3Helper.blockPos(player.position().add(0, 1, 0));
                    cur = level.getBlockState(curPos).getBlock();
                } else if (wrapper.getLevel() == LegacyLevel.BELOW) {
                    curPos = falsePlayer.getStandingOn().isEmpty() ? null : falsePlayer.getStandingOn().get(0);
                    cur = curPos == null ? null : level.getBlockState(curPos).getBlock();
                } else {
                    curPos = wrapper.getLevel().offset(player.getOnPos());
                    cur = level.getBlockState(curPos).getBlock();
                }


                // Fix boat clutching while breaking block
                // As a side effect, the falsePlayer is able to break multiple blocks at once while over lava
                if ((wrapper.getLevel().isSideAt() || wrapper.getLevel().isSideUp())
                        && level.getBlockState(falsePlayer.getOnPos().offset(0, -2, 0)).getBlock() == Blocks.LAVA
                        && pos.offset(0, 1, 0).equals(curPos)) {
                    cur = block;
                    wrapper.setLevel(wrapper.getLevel().sideDown());

                    if (wrapper.getLevel().isSideDown() || wrapper.getLevel().isSideDown2()) {
                        falsePlayer.setBotPitch(69);
                    } else if (wrapper.getLevel().isSideUp()) {
                        falsePlayer.setBotPitch(-53);
                    } else if (wrapper.getLevel().isSide()) {
                        falsePlayer.setBotPitch(0);
                    }
                }
                if ((wrapper.getLevel().isSideAt() || wrapper.getLevel().isSideDown())
                        && level.getBlockState(falsePlayer.getOnPos().offset(0, -1, 0)).getBlock() == Blocks.LAVA
                        && pos.offset(0, -1, 0).equals(curPos)) {
                    cur = block;
                    wrapper.setLevel(wrapper.getLevel().sideUp());

                    if (wrapper.getLevel().isSideDown() || wrapper.getLevel().isSideDown2()) {
                        falsePlayer.setBotPitch(69);
                    } else if (wrapper.getLevel().isSideUp()) {
                        falsePlayer.setBotPitch(-53);
                    } else if (wrapper.getLevel().isSide()) {
                        falsePlayer.setBotPitch(0);
                    }
                }

                // wow this repeated code is so bad lmao

                if (player.isDeadOrDying() || !block.equals(cur) || block != cur) {
                    thiz.cancel();

                    sendBlockDestructionPacket(level, CRACK_LIST.get(pos), pos.getX(), pos.getY(), pos.getZ(), -1);

                    CRACK_LIST.remove(pos);
                    MINING.remove((RepeatTask) thiz);
                    return;
                }

                var sound = block.getSoundType(level.getBlockState(pos), level, pos, falsePlayer).getBreakSound();

                if (i == 9) {
                    thiz.cancel();

                    sendBlockDestructionPacket(level, CRACK_LIST.get(pos), pos.getX(), pos.getY(), pos.getZ(), -1);
                    level.playSound(null, pos, sound, SoundSource.BLOCKS, 1, 1);
                    level.destroyBlock(pos, false);

                    if (wrapper.getLevel() == LegacyLevel.ABOVE) {
                        NO_JUMP.add(player);

                        TaskHelper.addServerTask(task -> NO_JUMP.remove(player), 15);
                    }

                    CRACK_LIST.remove(pos);
                    MINING.remove(thiz);
                    return;
                }


                level.playSound(null, pos, sound, SoundSource.BLOCKS, (float) 0.3, 1);


                if (block == Blocks.BARRIER || block == Blocks.BEDROCK || block == Blocks.END_PORTAL_FRAME
                        || block == Blocks.STRUCTURE_BLOCK
                        || block == Blocks.COMMAND_BLOCK || block == Blocks.REPEATING_COMMAND_BLOCK
                        || block == Blocks.CHAIN_COMMAND_BLOCK) {
                    return;
                }

                if (LegacyMats.INSTANT_BREAK.contains(block)) { // instant break blocks
                    level.destroyBlock(pos, false);
                    return;
                }

                sendBlockDestructionPacket(level, CRACK_LIST.get(pos), pos.getX(), pos.getY(), pos.getZ(), i);

                MINING.put((RepeatTask) thiz, (byte) (i + 1));
            }, 0, 2);
            TASK_LIST.add(repeatTask);
            MINING.put(repeatTask, (byte) 0);
            CRACK_LIST.put(pos, (short) falsePlayer.getRandom().nextInt(2000));
        }
    }

    public static void sendBlockDestructionPacket(Level level, short entityId, int x, int y, int z, int progress) {
        ClientboundBlockDestructionPacket crack = new ClientboundBlockDestructionPacket(entityId, new BlockPos(x, y, z), progress);
        for (Player all : level.players()) {
            ((ServerPlayer) all).connection.send(crack);
        }
    }

    private static void center(FalsePlayer falsePlayer) {
        if (falsePlayer == null || !falsePlayer.isAlive()) {
            return;
        }

        Vec3 prev = null;
        if (BT_LIST.containsKey(falsePlayer)) {
            prev = BT_LIST.get(falsePlayer);
        }

        var loc = falsePlayer.position().scale(1);

        if (prev != null) {
            if (Mth.floor(loc.x) == Mth.floor(prev.x) && Mth.floor(loc.z) == Mth.floor(prev.z)) {
                BT_CHECK.put(falsePlayer, true);
            } else {
                BT_CHECK.put(falsePlayer, false);
            }
        }

        BT_LIST.put(falsePlayer, loc);
    }

    private static LivingEntity locateTarget(FalsePlayer falsePlayer, Vec3 loc, EnumTargetGoal... targetGoal) {
        LivingEntity result = null;

        EnumTargetGoal g = goal;
        if (targetGoal.length > 0) {
            g = targetGoal[0];
        }
        var last = falsePlayer.getLastHurtByMob();
        if (last != null && last.isAlive() && last.level() == falsePlayer.level() && last.position().distanceToSqr(falsePlayer.position()) <= 48 * 48) {
            result = last;
        } else {
            switch (g) {
                default:
                    return null;

                /*case NEAREST_PLAYER: {
                    for (Player player : falsePlayer.level().players()) {
                        if (validateCloserEntity(falsePlayer, player, loc, result)) {
                            result = player;
                        }
                    }

                    break;
                }*/

                case NEAREST_VULNERABLE_PLAYER: {
                    for (Player truePlayer : falsePlayer.level().players()) {
                        if (!(truePlayer instanceof FalsePlayer)) {
                            if (!(truePlayer.isCreative() || truePlayer.isSpectator()) && truePlayer.distanceToSqr(falsePlayer) <= 64 * 64 && validateCloserEntity(falsePlayer, truePlayer, loc, result)) {
                                result = truePlayer;
                            }
                        }
                    }
                    break;
                }

                /*case NEAREST_HOSTILE: {
                    for (LivingEntity entity : falsePlayer.level().getNearbyEntities(Monster.class, TargetingConditions.forCombat(), falsePlayer, falsePlayer.getBoundingBox().inflate(64))) {
                        if (entity instanceof Monster && validateCloserEntity(falsePlayer, entity, loc, result)) {
                            result = entity;
                        }
                    }

                    break;
                }

                case NEAREST_RAIDER: {
                    for (LivingEntity entity : falsePlayer.level().getNearbyEntities(Raider.class, TargetingConditions.forCombat(), falsePlayer, falsePlayer.getBoundingBox().inflate(64))) {
                        if ((entity instanceof Raider || (entity instanceof Vex vex && vex.getOwner() instanceof Raider)) && validateCloserEntity(falsePlayer, entity, loc, result)) {
                            result = entity;
                        }
                    }

                    break;
                }

                case NEAREST_MOB: {
                    for (LivingEntity entity : falsePlayer.level().getNearbyEntities(Mob.class, TargetingConditions.forCombat(), falsePlayer, falsePlayer.getBoundingBox().inflate(64))) {
                        if (entity instanceof Mob && validateCloserEntity(falsePlayer, entity, loc, result)) {
                            result = entity;
                        }
                    }

                    break;
                }

                case NEAREST_BOT: {
                    for (FalsePlayer otherBot : manager.fetch()) {
                        if (falsePlayer != otherBot) {
                            LivingEntity player = otherBot.getBukkitEntity();

                            if (validateCloserEntity(player, loc, result)) {
                                result = player;
                            }
                        }
                    }

                    break;
                }

                case NEAREST_BOT_DIFFER: {
                    String name = falsePlayer.getBotName();

                    for (FalsePlayer otherBot : manager.fetch()) {
                        if (falsePlayer != otherBot) {
                            LivingEntity player = otherBot.getBukkitEntity();

                            if (!name.equals(otherBot.getBotName()) && validateCloserEntity(player, loc, result)) {
                                result = player;
                            }
                        }
                    }

                    break;
                }

                case NEAREST_BOT_DIFFER_ALPHA: {
                    String name = NAME_PATTERN.matcher(falsePlayer.getBotName()).replaceAll("");

                    for (FalsePlayer otherBot : manager.fetch()) {
                        if (falsePlayer != otherBot) {
                            LivingEntity player = otherBot.getBukkitEntity();

                            if (!name.equals(NAME_PATTERN.matcher(otherBot.getBotName()).replaceAll("")) && validateCloserEntity(player, loc, result)) {
                                result = player;
                            }
                        }
                    }
                }*/
                case PLAYER: {
                    //Target a single player. Defaults to NEAREST_VULNERABLE_PLAYER if no player found.
                    if (falsePlayer.getTargetPlayer() != null) {
                        Player player = falsePlayer.level().getPlayerByUUID(falsePlayer.getTargetPlayer());
                        if (player != null && validateCloserEntity(falsePlayer, player, loc, null)) {
                            return player;
                        }
                    }
                    return locateTarget(falsePlayer, loc, EnumTargetGoal.NEAREST_VULNERABLE_PLAYER);
                }
            }
        }
        FalsePlayerLocateTargetEvent event = new FalsePlayerLocateTargetEvent(falsePlayer, result);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return null;
        }
        return event.getTarget();
    }

    private static boolean validateCloserEntity(FalsePlayer falsePlayer, LivingEntity entity, Vec3 loc, LivingEntity result) {
        double regionDistEntity = getWeightedRegionDist(entity.position());
        if (regionDistEntity == Double.MAX_VALUE) {
            return false;
        }
        double regionDistResult = result == null ? 0 : getWeightedRegionDist(result.position());
        return falsePlayer.level() == entity.level() && !entity.isDeadOrDying()
                && (result == null || loc.distanceToSqr(entity.position()) + regionDistEntity < loc.distanceToSqr(result.position()) + regionDistResult);
    }

    private static double getWeightedRegionDist(Vec3 loc) {
        if (region == null) {
            return 0;
        }
        var center = region.getCenter();
        double diffX = Math.max(0, Math.abs(center.x - loc.x) - region.getXsize() * 0.5);
        double diffY = Math.max(0, Math.abs(center.y - loc.y) - region.getYsize() * 0.5);
        double diffZ = Math.max(0, Math.abs(center.z - loc.z) - region.getZsize() * 0.5);
        if (regionWeightX == 0 && regionWeightY == 0 && regionWeightZ == 0) {
            if (diffX > 0 || diffY > 0 || diffZ > 0) {
                return Double.MAX_VALUE;
            }
        }
        return diffX * diffX * regionWeightX + diffY * diffY * regionWeightY + diffZ * diffZ * regionWeightZ;
    }

    private static void stopMining(FalsePlayer falsePlayer) {
        if (MINING_ANIM.containsKey(falsePlayer)) {
            var task = MINING_ANIM.get(falsePlayer);
            if (task != null) {
                task.cancel();
                MINING_ANIM.remove(falsePlayer);
            }
        }
    }

    private static void fallDamageCheck(FalsePlayer falsePlayer) {
        /*if (falsePlayer.isFalling()) {
            falsePlayer.look(BlockFace.DOWN);

            Item itemType;

            if (falsePlayer.level().dimension() == Level.NETHER) {
                itemType = Items.TWISTING_VINES;
            } else {
                itemType = Items.WATER_BUCKET;
            }

            falsePlayer.setItem(new ItemStack(itemType));
        }*/
    }

    private static void miscellaneousChecks(FalsePlayer falsePlayer, LivingEntity target) {
        Level level = falsePlayer.level();
        Vec3 vec3 = Vec3Helper.clone(falsePlayer.position());
        var pos = Vec3Helper.blockPos(vec3);
        if (falsePlayer.isOnFire()) {
            if (falsePlayer.level().dimension() != Level.NETHER) {
                placeWaterDown(falsePlayer, level, pos);
            }
        }

        var atType = level.getBlockState(pos).getBlock();

        if (atType == Blocks.FIRE || atType == Blocks.SOUL_FIRE) {
            if (falsePlayer.level().dimension() != Level.NETHER) {
                placeWaterDown(falsePlayer, level, pos);
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, 1);
            } else {
                falsePlayer.look(BlockFace.DOWN);
                falsePlayer.punch();
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, 1);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            }
        }

        if (atType == Blocks.LAVA) {
            if (falsePlayer.level().dimension() == Level.NETHER) {
                falsePlayer.attemptBlockPlace(vec3, pos, Blocks.COBBLESTONE, false);
            } else {
                placeWaterDown(falsePlayer, level, pos);
            }
        }

        Vec3 head = vec3.add(0, 1, 0);
        var headPos = Vec3Helper.blockPos(head);
        var headType = level.getBlockState(headPos).getBlock();

        if (headType == Blocks.LAVA) {
            if (falsePlayer.level().dimension() == Level.NETHER) {
                falsePlayer.attemptBlockPlace(head, headPos, Blocks.COBBLESTONE, false);
            } else {
                placeWaterDown(falsePlayer, level, headPos);
            }
        }

        if (headType == Blocks.FIRE || headType == Blocks.SOUL_FIRE) {
            if (falsePlayer.level().dimension() == Level.NETHER) {
                falsePlayer.look(BlockFace.DOWN);
                falsePlayer.punch();
                level.playSound(null, headPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, 1);
                level.setBlock(headPos, Blocks.AIR.defaultBlockState(), 2);
            } else {
                placeWaterDown(falsePlayer, level, headPos);
            }
        }

        Vec3 under = vec3.add(0, -1, 0);
        var underPos = Vec3Helper.blockPos(under);
        var underType = level.getBlockState(underPos).getBlock();

        if (underType == Blocks.FIRE || underType == Blocks.SOUL_FIRE) {
            falsePlayer.look(BlockFace.DOWN);
            falsePlayer.punch();
            level.setBlock(underPos, Blocks.AIR.defaultBlockState(), 2);
        }

        Vec3 under2 = vec3.add(0, -2, 0);
        var under2Pos = Vec3Helper.blockPos(under2);
        var under2Type = level.getBlockState(under2Pos).getBlock();

        if (under2Type == Blocks.MAGMA_BLOCK) {
            if (LegacyMats.SPAWN.contains(under2Type)) {
                falsePlayer.attemptBlockPlace(under2, under2Pos, Blocks.COBBLESTONE, true);
            }
        }

        if (falsePlayer.position().y <= target.position().y + 1) {
            if (!MINING_ANIM.containsKey(falsePlayer)) {
                var vel = falsePlayer.getVelocity();
                double y = vel.y;

                if (y >= -0.6) {
                    if (level.getBlockState(Vec3Helper.blockPos(vec3.add(0, -0.6, 0))).getBlock() == Blocks.WATER
                            && !LegacyMats.NO_CRACK.contains(under2Type)
                            && level.getBlockState(Vec3Helper.blockPos(falsePlayer.getEyePosition())).isAir()) {

                        Vec3 mlgLoc = vec3.add(0, -1, 0);
                        var mlgLocPos = Vec3Helper.blockPos(mlgLoc);
                        Block place = level.getBlockState(mlgLocPos).getBlock();
                        if (LegacyMats.WATER.contains(place)) {
                            falsePlayer.attemptBlockPlace(mlgLoc, mlgLocPos, Blocks.COBBLESTONE, true);
                        }
                    }
                }
            }
        }

        var underType1Vec3 = vec3.add(0, -0.6, 0);
        var underType1Pos = Vec3Helper.blockPos(underType1Vec3);
        var underType1 = level.getBlockState(underType1Pos).getBlock();

        if (underType1 == Blocks.LAVA) {
            if (!BOAT_COOLDOWN.contains(falsePlayer)) {
                BOAT_COOLDOWN.add(falsePlayer);

                Vec3 place = vec3.add(0, -0.1, 0);

                falsePlayer.setItem(new ItemStack(Items.CHERRY_BOAT));
                falsePlayer.look(BlockFace.DOWN);
                falsePlayer.punch();

                var boat = new Boat(level, place.x, place.y, place.z);
                level.addFreshEntity(boat);
                TaskHelper.addServerTask(t -> {
                    if (boat.isAlive()) {
                        BOATS.remove(boat);
                        boat.remove(Entity.RemovalReason.DISCARDED);
                    }
                }, 20);

                TaskHelper.addServerTask(t -> {
                    falsePlayer.look(BlockFace.DOWN);
                }, 1);

                BOATS.add(boat);

                Vec3 targetLoc = target.position();

                falsePlayer.stand();
                var vector = targetLoc.toVector3f().add(falsePlayer.position().toVector3f().mul(-1)).normalize();
                vector.mul(0.8f);

                var move = falsePlayer.getVelocity().add(vector.x, vector.y, vector.z);
                move.y = 0;
                if (move.length() > 1) {
                    move = move.normalize();
                }
                move.multiply(0.5, 0.5, 0.5);
                move.y = 0.42;
                falsePlayer.setVelocity(move);

                TaskHelper.addServerTask(t -> {
                    BOAT_COOLDOWN.remove(falsePlayer);
                    if (falsePlayer.isAlive()) {
                        falsePlayer.face(target.position());
                    }
                }, 5);
            }
        }
    }

    private static void placeWaterDown(FalsePlayer falsePlayer, Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() == Blocks.WATER) {
            return;
        }

        falsePlayer.look(BlockFace.DOWN);
        falsePlayer.punch();
        level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1, 1);
        falsePlayer.setItem(new ItemStack(Items.BUCKET));

        TaskHelper.addServerTask(t -> {
            if (level.getBlockState(pos).getBlock() == Blocks.WATER) {
                falsePlayer.look(BlockFace.DOWN);
                falsePlayer.setItem(new ItemStack(Items.WATER_BUCKET));
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1, 1);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            }
        }, 5);
    }

    private static void attack(FalsePlayer bot, LivingEntity target, Vec3 loc) {
        if (target instanceof Player player && (player.isCreative() || player.isSpectator()) || loc.distanceToSqr(target.position()) >= 1.5f * 1.5f) {
            return;
        }
        bot.attack(target);
    }

    private static boolean onBoat(LivingEntity player) {
        Set<Boat> cache = new HashSet<>();

        boolean check = false;

        for (Boat boat : BOATS) {
            if (player.level() != boat.level()) {
                continue;
            }

            if (!boat.isAlive()) {
                cache.add(boat);
                continue;
            }

            if (player.position().distanceToSqr(boat.position()) < 1) {
                check = true;
                break;
            }
        }

        BOATS.removeAll(cache);

        return check;
    }

    private static void resetHand(FalsePlayer falsePlayer, LivingEntity target, LivingEntity playerNPC) {
        if (!NO_FACE.contains(falsePlayer)) { // LESSLAG if there is no if statement here
            falsePlayer.face(target.position());
        }

        if (MINING_ANIM.containsKey(playerNPC)) {
            var task = MINING_ANIM.get(playerNPC);
            if (task != null) {
                task.cancel();
                MINING_ANIM.remove(playerNPC);
            }
        }

        if (BOAT_COOLDOWN.contains(falsePlayer)) {
            return;
        }

        falsePlayer.setItem(falsePlayer.defaultWeapon);
    }

    public static void onPlayerDamage(BotDamageByPlayerEvent event) {
        FalsePlayer bot = event.getBot();
        var loc = Vec3Helper.clone(bot.position());
        Player player = event.getPlayer();

        double dot = loc.subtract(player.position()).normalize().dot(getDirection(bot.getXRot(), bot.getYRot()));

        if (bot.isBotBlocking() && dot >= -0.1) {
            player.level().playSound(null, bot.getOnPos(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1, 1);
            event.setCancelled(true);
        }
    }

    public static @NotNull Vec3 getDirection(double rotX, double rotY) {
        Vec3 vector = new Vec3(0, 0, 0);
        vector.y = (-Math.sin(Math.toRadians(rotY)));
        double xz = Math.cos(Math.toRadians(rotY));
        vector.x = (-xz * Math.sin(Math.toRadians(rotX)));
        vector.z = (xz * Math.cos(Math.toRadians(rotX)));
        return vector;
    }
}
