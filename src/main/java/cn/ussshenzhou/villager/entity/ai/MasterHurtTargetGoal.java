package cn.ussshenzhou.villager.entity.ai;

import cn.ussshenzhou.villager.entity.VillagerFollower;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

/**
 * @author USS_Shenzhou
 * @see net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal
 */
public class MasterHurtTargetGoal extends TargetGoal {
    private final VillagerFollower villager;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public MasterHurtTargetGoal(VillagerFollower villager) {
        super(villager.getThis(), false);
        this.villager = villager;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this method as well.
     */
    @Override
    public boolean canUse() {
        if (villager.getThis().getRandom().nextFloat() > 0.1f) {
            return false;
        }
        LivingEntity livingentity = this.villager.getMaster();
        if (livingentity == null) {
            return false;
        } else {
            this.ownerLastHurt = livingentity.getLastHurtMob();
            int i = livingentity.getLastHurtMobTimestamp();
            return i != this.timestamp && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT);
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurt);
        LivingEntity livingentity = this.villager.getMaster();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtMobTimestamp();
        }

        super.start();
    }
}
