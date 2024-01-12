package cn.ussshenzhou.villager.entity.fakeplayer;

import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.villager.entity.fakeplayer.events.BotFallDamageEvent;
import cn.ussshenzhou.villager.entity.fakeplayer.utils.*;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;

/**
 * @author USS_Shenzhou
 */
public class FalsePlayer extends ServerPlayer {
    private int aliveTicks;
    private int kills;
    private byte groundTicks;
    private byte jumpTicks;
    private byte noFallTicks;
    private Vec3 velocity;
    private Vec3 oldVelocity;
    private List<BlockPos> standingOn = new ArrayList<>();
    private UUID targetPlayer = null;

    public FalsePlayer(MinecraftServer pServer, ServerLevel pLevel, GameProfile pGameProfile, ClientInformation pClientInformation) {
        super(pServer, pLevel, pGameProfile, pClientInformation);
    }

    @Override
    public void tick() {
        super.tick();

        if (!isAlive()) {
            return;
        }

        aliveTicks++;

        if (jumpTicks > 0) {
            --jumpTicks;
        }
        if (noFallTicks > 0) {
            --noFallTicks;
        }

        if (checkGround()) {
            if (groundTicks < 5) {
                groundTicks++;
            }
        } else {
            groundTicks = 0;
        }

        updateLocation();

        if (!isAlive()) {
            return;
        }

        float health = getHealth();
        float maxHealth = getMaxHealth();
        float regenAmount = 0.025f;
        float amount;

        if (health < maxHealth - regenAmount) {
            amount = health + regenAmount;
        } else {
            amount = maxHealth;
        }

        setHealth(amount);

        fallDamageCheck();

        oldVelocity = velocity;

        doTick();
    }

    public boolean checkGround() {
        double vy = velocity.y;

        if (vy > 0) {
            return false;
        }

        return checkStandingOn();
    }

    public boolean checkStandingOn() {
        AABB box = getBoundingBox();
        double[] xVals = new double[]{
                box.minX,
                box.maxX
        };

        double[] zVals = new double[]{
                box.minZ,
                box.maxZ
        };
        var playerBox = new AABB(box.minX, position().y - 0.01, box.minZ,
                box.maxX, position().y + getBbHeight(), box.maxZ);
        List<BlockPos> standingOn = new ArrayList<>();
        List<BlockPos> blockPosList = new ArrayList<>();

        for (double x : xVals) {
            for (double z : zVals) {
                BlockPos pos = new BlockPos((int) x, (int) (position().y - 0.01), (int) z);
                BlockState state = level().getBlockState(pos);

                if ((state.isSolid() || LegacyMats.canStandOn(state.getBlock()) && BotUtils.overlaps(playerBox,
                        //needtest May need add pos?
                        state.getCollisionShape(level(), pos).bounds()))) {
                    if (!blockPosList.contains(pos)) {
                        standingOn.add(pos);
                        blockPosList.add(pos);
                    }
                }
            }
        }

        //Fence/wall check
        for (double x : xVals) {
            for (double z : zVals) {
                BlockPos pos = new BlockPos((int) x, (int) (position().y - 0.51), (int) z);
                BlockState state = level().getBlockState(pos);
                AABB blockBox = state.getCollisionShape(level(), pos).bounds();
                /*AABB modifiedBox = new AABB(blockBox.getMinX(), blockBox.getMinY(), blockBox.getMinZ(), blockBox.getMaxX(),
                        blockBox.getMinY() + 1.5, blockBox.getMaxZ());*/
                var modifiedBox = blockBox.setMaxY(blockBox.minY + 1.5);

                if ((LegacyMats.FENCE.contains(state.getBlock()) || LegacyMats.GATES.contains(state.getBlock()))
                        && state.isSolid() && BotUtils.overlaps(playerBox, modifiedBox)) {
                    if (!blockPosList.contains(pos)) {
                        standingOn.add(pos);
                        blockPosList.add(pos);
                    }
                }
            }
        }

        //Closest block comes first
        standingOn.sort((a, b) ->
                Double.compare(BotUtils.getHorizSqDist(a, this.getOnPos()), BotUtils.getHorizSqDist(b, this.getOnPos())));
        this.standingOn = standingOn;
        return !standingOn.isEmpty();
    }

    private void updateLocation() {
        double y;
        // TODO lag????
        MathUtils.clean(velocity);

        if (isBotInWater()) {
            y = Math.min(velocity.y + 0.1, 0.1);
            addFriction(0.8);
            velocity.y = y;
        } else {
            if (groundTicks != 0) {
                velocity.y = 0;
                addFriction(0.5);
                y = 0;
            } else {
                y = velocity.y;
                velocity.y = Math.max(y - 0.1, -3.5);
            }
        }

        this.move(MoverType.SELF, new Vec3(velocity.x, y, velocity.z));
    }

    public boolean isBotInWater() {
        var pos = position();

        for (int i = 0; i <= 2; i++) {
            Block block = level().getBlockState(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z)).getBlock();

            if (block == Blocks.WATER || block == Blocks.LAVA) {
                return true;
            }

            pos.add(0, 0.9, 0);
        }

        return false;
    }

    public void addFriction(double factor) {
        double frictionMin = 0.01;

        double x = velocity.x();
        double z = velocity.z();

        velocity.x = Math.abs(x) < frictionMin ? 0 : x * factor;
        velocity.z = Math.abs(z) < frictionMin ? 0 : z * factor;
    }

    private void fallDamageCheck() {
        if (groundTicks != 0 && noFallTicks == 0 && !(oldVelocity.y >= -0.8) && !isFallBlocked()) {
            BotFallDamageEvent event = new BotFallDamageEvent(this, new ArrayList<>(standingOn));
            NeoForge.EVENT_BUS.post(event);
            if (!event.isCanceled()) {
                hurt(damageSources().fall(), (float) Math.pow(3.6, -oldVelocity.y));
            }
        }
    }

    private boolean isFallBlocked() {
        AABB box = getBoundingBox();
        double[] xVals = new double[]{
                box.minX,
                box.maxX - 0.01
        };

        double[] zVals = new double[]{
                box.minZ,
                box.maxZ - 0.01
        };
        AABB playerBox = new AABB(box.minX, position().y - 0.01, box.minZ,
                box.maxX, position().y + getBbHeight(), box.maxZ);
        for (double x : xVals) {
            for (double z : zVals) {
                BlockPos pos = new BlockPos(Mth.floor(x), getOnPos().getY(), Mth.floor(z));
                BlockState state = level().getBlockState(pos);
                try {
                    if (state.getValue(BlockStateProperties.WATERLOGGED)) {
                        return true;
                    }
                } catch (IllegalArgumentException ignored) {
                }
                if (BotUtils.NO_FALL.contains(state.getBlock()) && (BotUtils.overlaps(playerBox, state.getCollisionShape(level(), pos).bounds())
                        || state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.LAVA)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void doTick() {
        baseTick();
    }

    public boolean tickDelay(int i) {
        return aliveTicks % i == 0;
    }

    public UUID getTargetPlayer() {
        return targetPlayer;
    }

    public void setTargetPlayer(UUID targetPlayer) {
        this.targetPlayer = targetPlayer;
    }

    public boolean isBotOnGround() {
        return groundTicks != 0;
    }

    public Vec3 getVelocity() {
        return Vec3Helper.clone(velocity);
    }

    public int getAliveTicks() {
        return aliveTicks;
    }

    public int getNoFallTicks() {
        return noFallTicks;
    }

    public void face(Vec3 loc) {
        look(loc.subtract(position()), false);
    }

    private void look(Vec3 dir, boolean keepYaw) {
        float yaw, pitch;

        if (keepYaw) {
            yaw = this.getYRot();
            pitch = MathUtils.fetchPitch(dir);
        } else {
            float[] vals = MathUtils.fetchYawPitch(dir);
            yaw = vals[0];
            pitch = vals[1];

            sendPacket(new ClientboundRotateHeadPacket(this, (byte) (yaw * 256 / 360f)));
        }

        setRot(yaw, pitch);
    }

    private void sendPacket(Packet<?> packet) {
        ((ServerLevel) this.level()).players().forEach(player -> player.connection.send(packet));
    }

    public void look(BlockFace face) {
        look(face.getDirection(), face == BlockFace.DOWN || face == BlockFace.UP);
    }

    public void punch() {
        swing(InteractionHand.MAIN_HAND);
    }

    public void setItem(ItemStack item) {
        setItem(item, EquipmentSlot.MAINHAND);
    }


    public void setItemOffhand(ItemStack item) {
        setItem(item, EquipmentSlot.OFFHAND);
    }

    public void setItem(ItemStack item, EquipmentSlot slot) {
        if (item == null) {
            item = ItemStack.EMPTY;
        }

        //System.out.println("set");
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            this.getInventory().setItem(slot.getIndex(), item);
        }

        //System.out.println("slot = " + slot);
        //System.out.println("item = " + item);
        sendPacket(new ClientboundSetEquipmentPacket(getId(), new ArrayList<>(Collections.singletonList(
                new Pair<>(slot, item)
        ))));
    }
}
