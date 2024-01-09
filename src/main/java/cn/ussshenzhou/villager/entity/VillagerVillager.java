package cn.ussshenzhou.villager.entity;

import cn.ussshenzhou.villager.VillagerManager;
import cn.ussshenzhou.villager.entity.ai.FollowMasterGoal;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
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

    private FollowMasterGoal followMasterGoal = new FollowMasterGoal(this, 0.75f, 4, 64, false);

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
        /*if (!level().isClientSide) {
            if (master == null || random.nextFloat() > 0.1f) {
                return;
            }
            switch (command) {
                case FOLLOW -> follow();
                case MOVE -> {

                }
                case DIG -> {

                }
            }
        }*/
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
        switch (command) {
            case FOLLOW -> {

            }
            case MOVE -> {

            }
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

    public enum Command {
        FOLLOW,
        MOVE,
        DIG
    }
}
