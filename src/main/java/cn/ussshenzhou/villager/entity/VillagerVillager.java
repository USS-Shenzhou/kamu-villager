package cn.ussshenzhou.villager.entity;

import cn.ussshenzhou.t88.task.TaskHelper;
import cn.ussshenzhou.villager.VillagerManager;
import cn.ussshenzhou.villager.entity.ai.*;
import cn.ussshenzhou.villager.entity.fakeplayer.utils.LegacyMats;
import cn.ussshenzhou.villager.entity.fakeplayer.utils.Vec3Helper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author USS_Shenzhou
 */
public class VillagerVillager extends Villager implements VillagerFollower {
    @Nullable
    private UUID masterUUID = null;
    private Command command = null;

    private final FloatGoal floatGoal = new FloatGoal(this);
    private final MeleeAttackGoal attackGoal = new MeleeAttackGoal(this, 0.85f, true);
    private final FollowMasterGoal followMasterGoal = new FollowMasterGoal(this, 0.75f, 5, 160, false);
    private final MasterTargetedByTargetGoal masterTargetedByTargetGoal = new MasterTargetedByTargetGoal(this);
    private final MasterHurtTargetGoal masterHurtTargetGoal = new MasterHurtTargetGoal(this);
    private final HurtByTargetGoal lastHurtByGoal = new HurtByTargetGoal(this);
    private final DigGoal digGoal = new DigGoal(this);
    @Nullable
    private Vector3f digDirection = null;

    public VillagerVillager(EntityType<? extends Villager> pEntityType, Level pLevel) {
        this(pEntityType, pLevel, VillagerType.PLAINS);
    }

    public VillagerVillager(EntityType<? extends Villager> pEntityType, Level pLevel, VillagerType pVillagerType) {
        super(pEntityType, pLevel, pVillagerType);
    }

    @Override
    public void tick() {
        try {
            super.tick();
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("memory")) {
                var nbtops = NbtOps.INSTANCE;
                this.brain = Brain.provider(ImmutableList.of(), ImmutableList.of()).makeBrain(new Dynamic<>(nbtops, nbtops.createMap(ImmutableMap.of(nbtops.createString("memories"), nbtops.emptyMap()))));

            }
        }
        tryFollow();
        if (level().isClientSide) {
            return;
        }
        if (masterUUID == null) {
            return;
        }
        if (command == Command.DIG && digDirection != null && digDirection.y > -0.2) {
            clutch();
        }
        checkBreath();
    }

    public void clutch() {
        Vec3 botLoc = this.position();

        Block type = this.level().getBlockState(Vec3Helper.blockPos(this.position().add(0, -1, 0))).getBlock();
        Block type2 = this.level().getBlockState(Vec3Helper.blockPos(this.position().add(0, -1, 0))).getBlock();

        if (!(LegacyMats.SPAWN.contains(type) && LegacyMats.SPAWN.contains(type2))) {
            return;
        }

        BlockPos pos = Vec3Helper.blockPos(botLoc).offset(0, -1, 0);

        Set<Tuple<BlockPos, Block>> face = new HashSet<>(Arrays.asList(
                new Tuple<>(pos.offset(1, 0, 0), this.level().getBlockState(pos.offset(1, 0, 0)).getBlock()),
                new Tuple<>(pos.offset(-1, 0, 0), this.level().getBlockState(pos.offset(-1, 0, 0)).getBlock()),
                new Tuple<>(pos.offset(0, 0, 1), this.level().getBlockState(pos.offset(0, 0, 1)).getBlock()),
                new Tuple<>(pos.offset(0, 0, -1), this.level().getBlockState(pos.offset(0, 0, -1)).getBlock())
        ));

        BlockPos at = null;
        for (Tuple<BlockPos, Block> side : face) {
            if (!LegacyMats.SPAWN.contains(side.getB())) {
                at = side.getA();
            }
        }

        if (at != null) {
            this.level().playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1, 1);
            this.level().setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), 2);
        }
    }

    private void checkBreath() {
        if (this.isInWall()) {
            assert masterUUID != null;
            var master = getMaster();
            if (master == null) {
                return;
            }
            var pos = master.position();
            this.teleportTo((ServerLevel) master.level(), pos.x, pos.y, pos.z, Set.of(), this.getYRot(), this.getXRot());
        }
    }

    private void tryFollow() {
        if (masterUUID != null) {
            return;
        }
        level().getNearbyPlayers(TargetingConditions.forNonCombat(), this, this.getBoundingBox().inflate(6))
                .stream().filter(player -> this.getSensing().hasLineOfSight(player))
                .min((p0, p1) -> (int) (this.distanceTo(p0) - this.distanceTo(p1)))
                .ifPresent(player -> {
                    this.masterUUID = player.getUUID();
                    VillagerManager.follow(player, this);
                    var nbtops = NbtOps.INSTANCE;
                    this.brain = Brain.provider(ImmutableList.of(), ImmutableList.of()).makeBrain(new Dynamic<>(nbtops, nbtops.createMap(ImmutableMap.of(nbtops.createString("memories"), nbtops.emptyMap()))));
                    setCommand(Command.FOLLOW);
                    this.level().playSound(null, this.getOnPos(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 1, 1);
                });
    }

    @Override
    public void setCommand(Command command) {
        this.command = command;
        this.goalSelector.removeAllGoals(goal -> true);
        this.targetSelector.removeAllGoals(goal -> true);
        this.goalSelector.lockedFlags.clear();
        this.targetSelector.lockedFlags.clear();
        this.setTarget(null);
        this.digDirection = null;
        goalSelector.addGoal(0, floatGoal);
        switch (command) {
            case FOLLOW -> {
                masterTargetedByTargetGoal.init();
                goalSelector.addGoal(1, followMasterGoal);

                targetSelector.addGoal(0, masterTargetedByTargetGoal);
                targetSelector.addGoal(1, masterHurtTargetGoal);
                targetSelector.addGoal(2, lastHurtByGoal);

                TaskHelper.addServerTask(() -> goalSelector.addGoal(0, attackGoal), 5 * 20);
            }
            /*case MOVE -> {

            }*/
            case DIG -> {
                goalSelector.addGoal(0, digGoal);
                var player = this.getMaster();
                if (player == null) {
                    return;
                }
                Vector3f dir = player.getLookAngle().toVector3f();
                if (dir.y < -0.8) {
                    dir.mul(0, 1, 0);
                } else {
                    dir.mul(1, 0, 1);
                }
                digDirection = dir.normalize();
            }
        }
    }

    @Override
    @Nullable
    public Player getMaster() {
        if (masterUUID == null) {
            return null;
        }
        Player player = this.level().getPlayerByUUID(masterUUID);
        if (player == null) {
            if (this.level().isClientSide()) {
                return null;
            } else {
                player = this.level().getServer().getPlayerList().getPlayer(masterUUID);
            }
        }
        return player;
    }

    public Command getCommand() {
        return command;
    }

    @Nullable
    public Vector3f getDigDirection() {
        if (digDirection != null) {
            return new Vector3f(digDirection);
        }
        return null;
    }

    @Override
    public void tellWitnessesThatIWasMurdered(Entity pMurderer) {
        //super.tellWitnessesThatIWasMurdered(pMurderer);
    }

    private static final Set<Item> WANTED_ITEMS = ImmutableSet.of(
            Items.BREAD,
            Items.POTATO,
            Items.CARROT,
            Items.WHEAT,
            Items.WHEAT_SEEDS,
            Items.BEETROOT,
            Items.BEETROOT_SEEDS,
            Items.TORCHFLOWER_SEEDS,
            Items.PITCHER_POD
    );

    @Override
    public boolean wantsToPickUp(ItemStack pStack) {
        if (random.nextFloat() < 0.8) {
            return false;
        }
        Item item = pStack.getItem();
        return (WANTED_ITEMS.contains(item)
                || this.acceptItem(pStack)
                || this.getVillagerData().getProfession().requestedItems().contains(item)
        ) && this.getInventory().canAddItem(pStack);
    }

    private boolean acceptItem(ItemStack item) {
        return item.is(Tags.Items.ARMORS)
                || item.is(Tags.Items.TOOLS);
    }

    @Override
    protected void pickUpItem(ItemEntity pItemEntity) {
        ItemStack itemstack = pItemEntity.getItem();
        ItemStack itemstack1 = this.equipItemIfPossible(itemstack.copy());
        if (!itemstack1.isEmpty()) {
            this.onItemPickup(pItemEntity);
            this.take(pItemEntity, itemstack1.getCount());
            itemstack.shrink(itemstack1.getCount());
            if (itemstack.isEmpty()) {
                pItemEntity.discard();
            }
        }
    }

    @Override
    public void releasePoi(MemoryModuleType<GlobalPos> pModuleType) {
        //super.releasePoi(pModuleType);
    }

    @Override
    public void restoreFrom(Entity pEntity) {
        super.restoreFrom(pEntity);
        if (pEntity instanceof VillagerVillager villager) {
            this.command = villager.command;
            this.masterUUID = villager.masterUUID;
            VillagerManager.follow(masterUUID, this);
            masterTargetedByTargetGoal.init();
            this.setTarget(null);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        if (masterUUID != null) {
            pCompound.putUUID("master", masterUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (masterUUID == null) {
            try {
                masterUUID = pCompound.getUUID("master");
                VillagerManager.follow(masterUUID, this);
                var nbtops = NbtOps.INSTANCE;
                this.brain = Brain.provider(ImmutableList.of(), ImmutableList.of()).makeBrain(new Dynamic<>(nbtops, nbtops.createMap(ImmutableMap.of(nbtops.createString("memories"), nbtops.emptyMap()))));
                if (command == null) {
                    setCommand(Command.FOLLOW);
                }
            } catch (Exception ignored) {
            }
        }
    }
}
