package cn.ussshenzhou.villager.entity.fakeplayer;

import cn.ussshenzhou.villager.entity.fakeplayer.events.FalsePlayerLocateTargetEvent;
import cn.ussshenzhou.villager.entity.fakeplayer.utils.EnumTargetGoal;
import cn.ussshenzhou.villager.entity.fakeplayer.utils.LegacyBlockCheck;
import cn.ussshenzhou.villager.entity.fakeplayer.utils.LegacyMats;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;
import java.util.Map;

public class FalsePlayerTickHelper {
    private final Map<LivingEntity, Vec3> btList = new HashMap<>();
    private final Map<LivingEntity, Boolean> btCheck = new HashMap<>();
    private EnumTargetGoal goal;
    private AABB region;
    private double regionWeightX;
    private double regionWeightY;
    private double regionWeightZ;
    private final LegacyBlockCheck blockCheck;

    public FalsePlayerTickHelper() {
        this.goal = EnumTargetGoal.NEAREST_VULNERABLE_PLAYER;
        this.blockCheck = new LegacyBlockCheck(this);
    }

    private void tick(FalsePlayer falsePlayer) {
        if (!falsePlayer.isAlive()) {
            return;
        }

        if (falsePlayer.tickDelay(20)) {
            center(falsePlayer);
        }

        Vec3 loc = new Vec3(falsePlayer.position().x, falsePlayer.position().y, falsePlayer.position().z);
        LivingEntity livingTarget = locateTarget(falsePlayer, loc);

        blockCheck.tryPreMLG(falsePlayer, loc);

        if (livingTarget == null) {
            stopMining(falsePlayer);
            return;
        }

        blockCheck.clutch(falsePlayer, livingTarget);

        fallDamageCheck(falsePlayer);
        miscellaneousChecks(falsePlayer, livingTarget);

        LivingEntity botPlayer = falsePlayer.getBukkitEntity();
        BlockPos target = offsets ? livingTarget.getBlockPos().add(falsePlayer.getOffset()) : livingTarget.getBlockPos();

        boolean ai = falsePlayer.hasNeuralNetwork();

        NeuralNetwork network = ai ? falsePlayer.getNeuralNetwork() : null;

        if (ai) {
            network.feed(BotData.generate(falsePlayer, livingTarget));
        }

        if (falsePlayer.tickDelay(3) && !miningAnim.containsKey(botPlayer)) {
            BlockPos botEyeLoc = botPlayer.getEyeBlockPos();
            BlockPos playerEyeLoc = livingTarget.getEyeBlockPos();
            BlockPos playerLoc = livingTarget.getBlockPos();

            if (ai) {
                if (network.check(BotNode.BLOCK) && loc.distance(livingTarget.getBlockPos()) < 6) {
                    falsePlayer.block(10, 10);
                }
            }

            if (LegacyUtils.checkFreeSpace(botEyeLoc, playerEyeLoc) || LegacyUtils.checkFreeSpace(botEyeLoc, playerLoc)) {
                attack(falsePlayer, livingTarget, loc);
            }
        }

        boolean waterGround = (LegacyMats.WATER.contains(loc.clone().add(0, -0.1, 0).getBlock().getType())
                && !LegacyMats.AIR.contains(loc.clone().add(0, -0.6, 0).getBlock().getType()));

        boolean withinTargetXZ = false, sameXZ = false;

        if (btCheck.containsKey(botPlayer)) {
            sameXZ = btCheck.get(botPlayer);
        }

        if (waterGround || falsePlayer.isBotOnGround() || onBoat(botPlayer)) {
            byte sideResult = 1;

            if (towerList.containsKey(botPlayer)) {
                if (loc.getBlockY() > livingTarget.getBlockPos().getBlockY()) {
                    towerList.remove(botPlayer);
                    resetHand(falsePlayer, livingTarget, botPlayer);
                }
            }

            Block block = loc.clone().add(0, 1, 0).getBlock();

            if (Math.abs(loc.getBlockX() - target.getBlockX()) <= 3 &&
                    Math.abs(loc.getBlockZ() - target.getBlockZ()) <= 3) {
                withinTargetXZ = true;
            }

            boolean bothXZ = withinTargetXZ || sameXZ;

            if (checkAt(falsePlayer, block, botPlayer)) {
                return;
            }

            if (checkFenceAndGates(falsePlayer, loc.getBlock(), botPlayer)) {
                return;
            }

            if (checkObstacles(falsePlayer, loc.getBlock(), botPlayer)) {
                return;
            }

            if (checkDown(falsePlayer, botPlayer, livingTarget.getBlockPos(), bothXZ)) {
                return;
            }

            if ((withinTargetXZ || sameXZ) && checkUp(falsePlayer, livingTarget, botPlayer, target, withinTargetXZ, sameXZ)) {
                return;
            }

            if (bothXZ) {
                sideResult = checkSide(falsePlayer, livingTarget, botPlayer);
            }

            switch (sideResult) {
                case 1:
                    resetHand(falsePlayer, livingTarget, botPlayer);
                    if (!noJump.contains(botPlayer) && !waterGround) {
                        move(falsePlayer, livingTarget, loc, target, ai);
                    }
                    return;

                case 2:
                    if (!waterGround) {
                        move(falsePlayer, livingTarget, loc, target, ai);
                    }
            }
        } else if (LegacyMats.WATER.contains(loc.getBlock().getType())) {
            swim(falsePlayer, target, botPlayer, livingTarget, LegacyMats.WATER.contains(loc.clone().add(0, -1, 0).getBlock().getType()));
        }
    }

    private void center(FalsePlayer falsePlayer) {
        if (falsePlayer == null || !falsePlayer.isAlive()) {
            return;
        }

        Vec3 prev = null;
        if (btList.containsKey(falsePlayer)) {
            prev = btList.get(falsePlayer);
        }

        var loc = falsePlayer.position().scale(1);

        if (prev != null) {
            if (Mth.floor(loc.x) == Mth.floor(prev.x) && Mth.floor(loc.z) == Mth.floor(prev.z)) {
                btCheck.put(falsePlayer, true);
            } else {
                btCheck.put(falsePlayer, false);
            }
        }

        btList.put(falsePlayer, loc);
    }

    private LivingEntity locateTarget(FalsePlayer falsePlayer, Vec3 loc, EnumTargetGoal... targetGoal) {
        LivingEntity result = null;

        EnumTargetGoal g = goal;
        if (targetGoal.length > 0) {
            g = targetGoal[0];
        }
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
                for (Player player : falsePlayer.level().players()) {
                    if (!player.isCreative() && validateCloserEntity(falsePlayer, player, loc, result)) {
                        result = player;
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
        FalsePlayerLocateTargetEvent event = new FalsePlayerLocateTargetEvent(falsePlayer, result);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return null;
        }
        return event.getTarget();
    }

    private boolean validateCloserEntity(FalsePlayer falsePlayer, LivingEntity entity, Vec3 loc, LivingEntity result) {
        double regionDistEntity = getWeightedRegionDist(entity.position());
        if (regionDistEntity == Double.MAX_VALUE) {
            return false;
        }
        double regionDistResult = result == null ? 0 : getWeightedRegionDist(result.position());
        return falsePlayer.level() == entity.level() && !entity.isDeadOrDying()
                && (result == null || (loc.distanceToSqr(entity.position()) + regionDistEntity) < (loc.distanceToSqr(result.position())) + regionDistResult);
    }

    private double getWeightedRegionDist(Vec3 loc) {
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
}
