package cn.ussshenzhou.villager.entity;

import cn.ussshenzhou.villager.VillagerManager;
import cn.ussshenzhou.villager.entity.ai.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
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
import net.neoforged.neoforge.common.Tags;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

/**
 * @author USS_Shenzhou
 */
public class VillagerVillager extends Villager {
    @Nullable
    private UUID masterUUID = null;
    private Command command = null;
    @Nullable
    private Vector3f digDirection = null;

    private int delayAttack = 0;
    private final FollowMasterGoal cancelCombatAndFollowGoal = new FollowMasterGoal(this, 0.75f, 48, 96, false);
    private final MeleeAttackGoal attackGoal = new MeleeAttackGoal(this, 1f, true);
    private final FollowMasterGoal followMasterGoal = new FollowMasterGoal(this, 0.75f, 5, 128, false);
    private final MasterTargetedByTargetGoal masterTargetedByTargetGoal = new MasterTargetedByTargetGoal(this);
    private final MasterHurtTargetGoal masterHurtTargetGoal = new MasterHurtTargetGoal(this);

    //private final DigGoalDisabled2 digGoal = new DigGoalDisabled2(this);

    public VillagerVillager(EntityType<? extends Villager> pEntityType, Level pLevel) {
        this(pEntityType, pLevel, VillagerType.PLAINS);
    }

    public VillagerVillager(EntityType<? extends Villager> pEntityType, Level pLevel, VillagerType pVillagerType) {
        super(pEntityType, pLevel, pVillagerType);
    }

    @Override
    public void tick() {
        super.tick();
        tryFollow();
        if (level().isClientSide || masterUUID == null) {
            return;
        }
        if (delayAttack > 0) {
            delayAttack--;
        } else if (delayAttack == 0) {
            goalSelector.addGoal(0, attackGoal);
        }
        checkBreath();
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
        level().getNearbyPlayers(TargetingConditions.forNonCombat(), this, this.getBoundingBox().inflate(32))
                .stream().filter(player -> this.getSensing().hasLineOfSight(player))
                .min((p0, p1) -> (int) (this.distanceTo(p0) - this.distanceTo(p1)))
                .ifPresent(player -> {
                    this.masterUUID = player.getUUID();
                    VillagerManager.follow(player, this);
                    var nbtops = NbtOps.INSTANCE;
                    this.brain = Brain.provider(ImmutableList.of(), ImmutableList.of()).makeBrain(new Dynamic<>(nbtops, nbtops.createMap(ImmutableMap.of(nbtops.createString("memories"), nbtops.emptyMap()))));
                    setCommand(Command.FOLLOW);
                });
    }

    public void setCommand(Command command) {
        this.command = command;
        this.goalSelector.removeAllGoals(goal -> true);
        this.targetSelector.removeAllGoals(goal -> true);
        this.setTarget(null);
        this.digDirection = null;
        switch (command) {
            case FOLLOW -> {
                delayAttack = 5 * 20;
                masterTargetedByTargetGoal.init();
                goalSelector.addGoal(-1, cancelCombatAndFollowGoal);
                goalSelector.addGoal(1, followMasterGoal);
                targetSelector.addGoal(0, masterTargetedByTargetGoal);
                targetSelector.addGoal(1, masterHurtTargetGoal);
            }
            /*case MOVE -> {

            }*/
            case DIG -> {
                /*//goalSelector.addGoal(0, digGoal);
                var player = this.getMaster();
                //this.digDirection = player.getLookAngle().toVector3f();
                var target = this.position().toVector3f().add(player.getLookAngle().toVector3f().normalize().mul(64));
                this.getNavigation().moveTo(target.x, target.y, target.z, getSpeed() * 0.75f);*/
            }
        }
    }

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
        return digDirection;
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
            masterTargetedByTargetGoal.init();
            this.setTarget(null);
        }
    }

    public enum Command {
        FOLLOW,
        //MOVE,
        DIG
    }
}
