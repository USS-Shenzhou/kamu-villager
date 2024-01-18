package cn.ussshenzhou.villager.mixin;

import cn.ussshenzhou.t88.task.TaskHelper;
import cn.ussshenzhou.villager.VillagerManager;
import cn.ussshenzhou.villager.entity.Command;
import cn.ussshenzhou.villager.entity.VillagerFollower;
import cn.ussshenzhou.villager.entity.ai.FollowMasterGoal;
import cn.ussshenzhou.villager.entity.ai.MasterHurtTargetGoal;
import cn.ussshenzhou.villager.entity.ai.MasterTargetedByTargetGoal;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings({"MissingUnique", "AlibabaLowerCamelCaseVariableNaming", "AddedMixinMembersNamePattern"})
@Mixin(IronGolem.class)
public class IronGolemMixin extends AbstractGolem implements VillagerFollower {

    @Nullable
    private UUID masterUUID = null;
    private Command command = null;

    private final MeleeAttackGoal attackGoal = new MeleeAttackGoal((IronGolem) (Object) this, 1.1, true);
    private final FollowMasterGoal followMasterGoal = new FollowMasterGoal(this, 1.1, 5, 160, false);
    private final MasterTargetedByTargetGoal masterTargetedByTargetGoal = new MasterTargetedByTargetGoal(this);
    private final MasterHurtTargetGoal masterHurtTargetGoal = new MasterHurtTargetGoal(this);
    private final HurtByTargetGoal lastHurtByGoal = new HurtByTargetGoal((IronGolem) (Object) this);

    protected IronGolemMixin(EntityType<? extends AbstractGolem> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void tick() {
        super.tick();
        tryFollow();
    }

    private void tryFollow() {
        if (masterUUID != null) {
            return;
        }
        level().getNearbyPlayers(TargetingConditions.forNonCombat(), this, this.getBoundingBox().inflate(8))
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
            }
        }
    }

    @Override
    public Command getCommand() {
        return command;
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

    @Override
    public void restoreFrom(Entity pEntity) {
        super.restoreFrom(pEntity);
        if (pEntity instanceof IronGolem ironGolem) {
            this.command = ((VillagerFollower) ironGolem).getCommand();
            var p = ((VillagerFollower) ironGolem).getMaster();
            this.masterUUID = p == null ? null : p.getUUID();
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
