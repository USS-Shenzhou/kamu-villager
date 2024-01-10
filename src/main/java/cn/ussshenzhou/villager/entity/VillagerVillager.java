package cn.ussshenzhou.villager.entity;

import cn.ussshenzhou.villager.VillagerManager;
import cn.ussshenzhou.villager.entity.ai.FollowMasterGoal;
import cn.ussshenzhou.villager.entity.ai.MasterTargetedByTargetGoal;
import cn.ussshenzhou.villager.entity.ai.MasterHurtTargetGoal;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * @author USS_Shenzhou
 */
public class VillagerVillager extends Villager {
    @Nullable
    private Player master = null;
    private Command command = null;

    private int delayAttack = 0;
    private final FollowMasterGoal cancelCombatAndFollowGoal = new FollowMasterGoal(this, 0.75f, 48, 96, false);
    private final MeleeAttackGoal attackGoal = new MeleeAttackGoal(this, 1f, true);
    private final FollowMasterGoal followMasterGoal = new FollowMasterGoal(this, 0.75f, 5, 128, false);
    private final MasterTargetedByTargetGoal masterTargetedByTargetGoal = new MasterTargetedByTargetGoal(this);
    private final MasterHurtTargetGoal masterHurtTargetGoal = new MasterHurtTargetGoal(this);

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
        if (delayAttack > 0) {
            delayAttack--;
        } else if (delayAttack == 0) {
            goalSelector.addGoal(0, attackGoal);
        }
    }

    private void tryFollow() {
        if (master != null) {
            return;
        }
        level().getNearbyPlayers(TargetingConditions.forNonCombat(), this, this.getBoundingBox().inflate(32))
                .stream().filter(player -> this.getSensing().hasLineOfSight(player))
                .min((p0, p1) -> (int) (this.distanceTo(p0) - this.distanceTo(p1)))
                .ifPresent(player -> {
                    this.master = player;
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

            }
        }
    }

    @Nullable
    public Player getMaster() {
        return master;
    }

    public Command getCommand() {
        return command;
    }

    @Override
    public void tellWitnessesThatIWasMurdered(Entity pMurderer) {
        //super.tellWitnessesThatIWasMurdered(pMurderer);
    }

    public enum Command {
        FOLLOW,
        //MOVE,
        DIG
    }
}
