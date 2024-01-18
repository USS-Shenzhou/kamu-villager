package cn.ussshenzhou.villager.entity.fakeplayer;

import cn.ussshenzhou.t88.analyzer.back.T88AnalyzerClient;
import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.t88.task.TaskHelper;
import cn.ussshenzhou.villager.entity.fakeplayer.events.BotDamageByPlayerEvent;
import cn.ussshenzhou.villager.entity.fakeplayer.events.BotFallDamageEvent;
import cn.ussshenzhou.villager.entity.fakeplayer.events.BotKilledByPlayerEvent;
import cn.ussshenzhou.villager.entity.fakeplayer.utils.*;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import org.jetbrains.annotations.Nullable;

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
    private Vec3 velocity = new Vec3(0, 0, 0);
    private Vec3 oldVelocity = new Vec3(0, 0, 0);
    private List<BlockPos> standingOn = new ArrayList<>();
    private UUID targetPlayer = null;
    private boolean blocking;
    public ItemStack defaultWeapon;

    public static final String NAME = "Steve";

    public FalsePlayer(MinecraftServer pServer, ServerLevel pLevel, GameProfile pGameProfile) {
        super(pServer, pLevel, pGameProfile, ClientInformation.createDefault());
    }

    public static FalsePlayer create(ServerLevel level) {
        return create(level, 0, 0, 0);
    }

    public static FalsePlayer create(ServerLevel level, double x, double y, double z) {
        MinecraftServer server = (MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER);
        UUID uuid = BotUtils.randomSteveUUID();
        var profile = new CustomGameProfile(uuid, NAME);
        FalsePlayer falsePlayer = new FalsePlayer(server, level, profile);
        falsePlayer.connection = new ServerGamePacketListenerImpl(server, new FakeConnection(), falsePlayer, CommonListenerCookie.createInitial(profile));
        falsePlayer.populateDefaultEquipmentSlots(level.random);
        falsePlayer.getAdvancements().stopListening();
        falsePlayer.setPos(x, y, z);
        level.players().stream().filter(FalsePlayer::isRealPlayer).forEach(p -> p.connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, falsePlayer)));
        falsePlayer.renderAll();
        //TaskHelper.addServerTask(() -> falsePlayer.getInventory().setChanged(), 5);
        return falsePlayer;
    }

    public static boolean isRealPlayer(Player player) {
        return !(player instanceof FalsePlayer);
    }


    protected void populateDefaultEquipmentSlots(RandomSource r) {
        for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            if (r.nextFloat() < 0.2f) {
                continue;
            }
            if (equipmentslot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack itemstack = this.getItemBySlot(equipmentslot);
                if (itemstack.isEmpty()) {
                    Item item = getRandomArmor(r, equipmentslot);
                    if (item != null) {
                        this.setItemSlot(equipmentslot, new ItemStack(item));
                    }
                }
            } else if (equipmentslot.getType() == EquipmentSlot.Type.HAND) {
                ItemStack itemstack = this.getItemBySlot(EquipmentSlot.MAINHAND);
                if (itemstack.isEmpty()) {
                    Item item = getRandomWeapon(r);
                    if (item != null) {
                        this.defaultWeapon = new ItemStack(item);
                    } else {
                        this.defaultWeapon = ItemStack.EMPTY;
                    }
                    this.setItemSlot(EquipmentSlot.MAINHAND, defaultWeapon);
                    float d = 3;
                    if (item instanceof SwordItem sword) {
                        d = sword.getDamage();
                    } else if (item instanceof AxeItem axe) {
                        d = axe.getAttackDamage();
                    }
                    this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(d);
                }
            }
        }
    }

    private Item getRandomArmor(RandomSource r, EquipmentSlot slot) {
        float f = r.nextFloat();
        float diamond = getDiamondFactor();
        float iron = getIronFactor() + diamond;
        switch (slot) {
            case HEAD:
                if (f < diamond) {
                    return Items.DIAMOND_HELMET;
                } else if (f < iron) {
                    return Items.IRON_HELMET;
                } else {
                    return null;
                }
            case CHEST:
                if (f < diamond) {
                    return Items.DIAMOND_CHESTPLATE;
                } else if (f < iron) {
                    return Items.IRON_CHESTPLATE;
                } else {
                    return null;
                }
            case LEGS:
                if (f < diamond) {
                    return Items.DIAMOND_LEGGINGS;
                } else if (f < iron) {
                    return Items.IRON_LEGGINGS;
                } else {
                    return null;
                }
            case FEET:
                if (f < diamond) {
                    return Items.DIAMOND_BOOTS;
                } else if (f < iron) {
                    return Items.IRON_BOOTS;
                } else {
                    return null;
                }
            default:
                return null;
        }
    }

    private Item getRandomWeapon(RandomSource r) {
        float f = r.nextFloat();
        float diamond = getDiamondFactor() * 0.8f;
        float iron = getIronFactor() + diamond;
        float stone = diamond > 0.2 ? 1 : 0.8f;
        if (f < diamond) {
            return Items.DIAMOND_SWORD;
        } else if (f < iron) {
            return Items.IRON_SWORD;
        } else if (f < stone) {
            return Items.STONE_SWORD;
        } else {
            return Items.WOODEN_SWORD;
        }
    }

    private float getDiamondFactor() {
        return getNumberOfPlayersHave(16) * 0.1f;
    }

    private float getIronFactor() {
        return getNumberOfPlayersHave(10) * 0.1f;
    }

    private int getNumberOfPlayersHave(int threshold) {
        return (int) Mth.clamp(level().players().stream()
                        .filter(FalsePlayer::isRealPlayer)
                        .filter(player -> player.getArmorValue() >= threshold).count(),
                0, 5);
    }

    private static class FakeConnection extends Connection {
        public FakeConnection() {
            super(PacketFlow.SERVERBOUND);
        }

        @Override
        public void setListener(PacketListener listener) {
        }
    }

    private void renderAll() {
        //Packet<?>[] packets = getRenderPacketsNoInfo();
        ((ServerLevel) level()).players().stream().filter(FalsePlayer::isRealPlayer).forEach(p -> render(p.connection, true));
    }

    public Packet<?>[] getRenderPacketsNoInfo() {
        var l = this.entityData.getNonDefaultValues();
        return new Packet[]{
                new ClientboundAddEntityPacket(this),
                //new ClientboundSetEntityDataPacket(this.getId(), this.entityData, true),
                new ClientboundSetEntityDataPacket(this.getId(), l == null ? List.of() : l),
                new ClientboundRotateHeadPacket(this, (byte) ((this.yHeadRot * 256f) / 360f))
        };
    }

    private void renderNoInfo(ServerGamePacketListenerImpl connection, Packet<?>[] packets, boolean login) {
        connection.send(packets[0]);
        connection.send(packets[1]);
        if (login) {
            TaskHelper.addServerTask(() -> connection.send(packets[2]), 10);
        } else {
            connection.send(packets[2]);
        }
    }

    public void render(ServerGamePacketListenerImpl connection, boolean login) {
        render(connection, getRenderPackets(), login);
    }

    private void render(ServerGamePacketListenerImpl connection, Packet<?>[] packets, boolean login) {
        connection.send(packets[0]);
        connection.send(packets[1]);
        connection.send(packets[2]);

        if (login) {
            TaskHelper.addServerTask(() -> connection.send(packets[3]), 40);
        } else {
            connection.send(packets[3]);
        }
    }

    private Packet<?>[] getRenderPackets() {
        var d = this.getEntityData().getNonDefaultValues();
        return new Packet[]{
                new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, this),
                new ClientboundAddEntityPacket(this),
                //new ClientboundSetEntityDataPacket(this.getId(), this.entityData, true),
                new ClientboundSetEntityDataPacket(this.getId(), d == null ? List.of() : d),
                new ClientboundRotateHeadPacket(this, (byte) ((this.yHeadRot * 256f) / 360f))
        };
    }

    public void renderBot(Object packetListener, boolean login) {
        if (!(packetListener instanceof ServerGamePacketListenerImpl)) {
            throw new IllegalArgumentException("packetListener must be a instance of ServerGamePacketListenerImpl");
        }
        render((ServerGamePacketListenerImpl) packetListener, login);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        if (level().players().stream()
                .filter(player -> !(player instanceof FalsePlayer))
                .filter(realPlayer -> this.distanceToSqr(realPlayer) <= 128 * 128)
                .findAny().isEmpty()) {
            this.removeBot();
        }

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
            if (groundTicks < 10) {
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
        oldVelocity = Vec3Helper.clone(velocity);
        doTick();
        FalsePlayerTickHelper.tick(this);
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
                BlockPos pos = new BlockPos(Mth.floor(x), Mth.floor(position().y - 0.01), Mth.floor(z));
                BlockState state = level().getBlockState(pos);
                var shape = state.getCollisionShape(level(), pos);
                if (shape.isEmpty()) {
                    continue;
                }
                if ((state.isSolid() || LegacyMats.canStandOn(state.getBlock())
                        && BotUtils.overlaps(playerBox, shape.bounds().move(pos)))) {
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
                BlockPos pos = new BlockPos(Mth.floor(x), Mth.floor(position().y - 0.01), Mth.floor(z));
                BlockState state = level().getBlockState(pos);
                var shape = state.getCollisionShape(level(), pos);
                if (shape.isEmpty() || state.getBlock() instanceof TallGrassBlock) {
                    continue;
                }
                AABB blockBox = shape.bounds();
                blockBox = blockBox.move(pos);
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
                if (this.targetPlayer == null) {
                    addFriction(0.5);
                }
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
            Block block = level().getBlockState(Vec3Helper.blockPos(pos)).getBlock();

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
                var shape = state.getCollisionShape(level(), pos);
                if (BotUtils.NO_FALL.contains(state.getBlock())
                        && !shape.isEmpty()
                        && (BotUtils.overlaps(playerBox, shape.bounds())
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

    public void stand() {
        this.setSprinting(false);
        this.setSwimming(false);

        //registerPose(Pose.STANDING);
    }

    public void sneak() {
        this.setSprinting(true);
        //registerPose(Pose.CROUCHING);
    }

    public boolean isFalling() {
        return velocity.y < -0.8;
    }

    public void attemptBlockPlace(Vec3 vec3, BlockPos loc, Block b, boolean down) {
        if (down) {
            look(BlockFace.DOWN);
        } else {
            face(vec3);
        }

        setItem(new ItemStack(Items.COBBLESTONE));
        punch();

        BlockState block = level().getBlockState(loc);

        if (!block.isSolid()) {
            level().setBlock(loc, b.defaultBlockState(), 2);
            level().playSound(null, loc, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1, 1);
        }
    }

    public void setVelocity(Vec3 vector) {
        this.velocity = vector;
    }

    public void setBotPitch(float pitch) {
        super.setXRot(pitch);
    }

    public List<BlockPos> getStandingOn() {
        return standingOn;
    }

    public void addVelocity(Vec3 vector) { // This can cause lag? (maybe i fixed it with the new static method)
        if (MathUtils.isNotFinite(vector)) {
            velocity = vector;
            return;
        }

        velocity = velocity.add(vector);
    }

    public void jump(Vec3 vel) {
        if (jumpTicks == 0 && groundTicks > 1) {
            jumpTicks = 4;
            velocity = vel;
        }
    }

    public void jump() {
        jump(new Vec3(0, 0.5, 0));
    }

    public void swim() {
        this.setSwimming(true);
        //registerPose(Pose.SWIMMING);
    }

    public void removeVisually() {
        this.removeTab();
        this.setDead();
    }

    public void removeBot() {
        this.remove(RemovalReason.DISCARDED);
        FalsePlayerTickHelper.remove(this);
        this.removeVisually();
    }

    private void removeTab() {
        sendPacket(new ClientboundPlayerInfoRemovePacket(List.of(this.getUUID())));
    }

    private void setDead() {
        sendPacket(new ClientboundRemoveEntitiesPacket(getId()));

        this.dead = true;
        this.inventoryMenu.removed(this);
        this.containerMenu.removed(this);
    }

    private void dieCheck() {
        // I replaced HashSet with ConcurrentHashMap.newKeySet which creates a "ConcurrentHashSet"
        // this should fix the concurrentmodificationexception mentioned above, I used the ConcurrentHashMap.newKeySet to make a "ConcurrentHashSet"
        TaskHelper.addServerTask(this::removeBot, 20);
        this.removeTab();
    }

    @Override
    public void die(DamageSource pCause) {
        this.gameEvent(GameEvent.ENTITY_DIE);
        if (net.neoforged.neoforge.common.CommonHooks.onLivingDeath(this, pCause)) {
            return;
        }
        this.connection.send(new ClientboundPlayerCombatKillPacket(this.getId(), CommonComponents.EMPTY));
        this.removeEntitiesOnShoulder();
        if (this.level().getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
            this.tellNeutralMobsThatIDied();
        }
        if (!this.isSpectator()) {
            this.dropAllDeathLoot(pCause);
        }
        this.getScoreboard().forAllObjectives(ObjectiveCriteria.DEATH_COUNT, this, ScoreAccess::increment);
        LivingEntity livingentity = this.getKillCredit();
        if (livingentity != null) {
            this.awardStat(Stats.ENTITY_KILLED_BY.get(livingentity.getType()));
            livingentity.awardKillScore(this, this.deathScore, pCause);
            this.createWitherRose(livingentity);
        }
        this.level().broadcastEntityEvent(this, (byte) 3);
        this.awardStat(Stats.DEATHS);
        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        this.clearFire();
        this.setTicksFrozen(0);
        this.setSharedFlagOnFire(false);
        this.getCombatTracker().recheckStatus();
        this.setLastDeathLocation(Optional.of(GlobalPos.of(this.level().dimension(), this.blockPosition())));
        this.dieCheck();
    }

    protected void tellNeutralMobsThatIDied() {
        AABB aabb = new AABB(this.blockPosition()).inflate(32.0, 10.0, 32.0);
        this.level()
                .getEntitiesOfClass(Mob.class, aabb, EntitySelector.NO_SPECTATORS)
                .stream()
                .filter(p_9188_ -> p_9188_ instanceof NeutralMob)
                .forEach(p_9057_ -> ((NeutralMob) p_9057_).playerDied(this));
    }

    @Override
    public void push(Entity entity) {
        if (!this.isPassengerOfSameVehicle(entity) && !entity.noPhysics && !this.noPhysics) {
            double d0 = entity.getX() - this.getZ();
            double d1 = entity.getX() - this.getZ();
            double d2 = Mth.absMax(d0, d1);
            if (d2 >= 0.009999999776482582D) {
                d2 = Math.sqrt(d2);
                d0 /= d2;
                d1 /= d2;
                double d3 = 1.0D / d2;
                if (d3 > 1.0D) {
                    d3 = 1.0D;
                }

                d0 *= d3;
                d1 *= d3;
                d0 *= 0.05000000074505806D;
                d1 *= 0.05000000074505806D;

                if (!this.isVehicle()) {
                    velocity = velocity.add(new Vec3(-d0, 0.0D, -d1));
                }

                if (!entity.isVehicle()) {
                    entity.push(d0, 0.0D, d1);
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource damagesource, float f) {
        Entity attacker = damagesource.getEntity();
        float damage;
        if (attacker instanceof ServerPlayer serverPlayer) {
            BotDamageByPlayerEvent event = new BotDamageByPlayerEvent(this, serverPlayer, f);
            FalsePlayerTickHelper.onPlayerDamage(event);
            if (event.isCancelled()) {
                return false;
            }

            damage = event.getDamage();
        } else {
            attacker = null;
            damage = f;
        }

        boolean damaged = super.hurt(damagesource, damage);

        if (!damaged && blocking) {
            level().playSound(null, this.getOnPos(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1, 1);
        }

        if (damaged && attacker != null) {
            if (attacker instanceof ServerPlayer serverPlayer && !isAlive()) {
                //just Count kills
                //FalsePlayerTickHelper.onBotKilledByPlayer(new BotKilledByPlayerEvent(this, serverPlayer));
            } else {
                kb(position(), attacker.position(), attacker);
            }
        }

        return damaged;
    }

    private void kb(Vec3 loc1, Vec3 loc2, Entity attacker) {
        var vel = loc1.subtract(loc2);
        vel.y = (0);
        vel = vel.normalize().multiply(0.3, 0.3, 0.3);

        if (isBotOnGround()) {
            vel = vel.multiply(0.8, 0.8, 0.8);
            vel.y = 0.4;
        }
        if (attacker instanceof Player player && player.getMainHandItem().getEnchantmentTags().isEmpty()) {
            int kbLevel = player.getMainHandItem().getEnchantmentLevel(Enchantments.KNOCKBACK);
            if (kbLevel > 0) {
                if (kbLevel == 1) {
                    vel = vel.multiply(1.05, 1.05, 1.05);
                } else {
                    vel = vel.multiply(1.9, 1.9, 1.9);
                }
                vel.y = 0.4;
            }
        }
        velocity = vel;
    }

    public boolean isBotBlocking() {
        return isBlocking();
    }

    public void walk(Vec3 vel) {
        double max = 0.4;
        Vec3 sum = velocity.add(vel);
        if (sum.length() > max) {
            sum.normalize().multiply(max, max, max);
        }
        velocity = sum;
    }

    @Override
    public float getAttackStrengthScale(float pAdjustTicks) {
        return 1;
    }

    @Override
    protected void dropEquipment() {
        //super.dropEquipment();
    }

    @Override
    public int getExperienceReward() {
        return 20;
    }
}
