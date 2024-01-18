package cn.ussshenzhou.villager.entity.ai;

import cn.ussshenzhou.villager.entity.VillagerFollower;
import cn.ussshenzhou.villager.entity.fakeplayer.FalsePlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.LinkedHashSet;

/**
 * @author USS_Shenzhou
 * @see net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal
 */
public class MasterTargetedByTargetGoal extends TargetGoal {
    private final VillagerFollower villager;
    private LivingEntity ownerLastTargetedBy;
    private int timestamp;
    private final LinkedHashSet<WeakReference<LivingEntity>> targets = new LinkedHashSet<>();

    public MasterTargetedByTargetGoal(VillagerFollower villager) {
        super(villager.getThis(), false);
        this.villager = villager;
        this.setFlags(EnumSet.of(Flag.TARGET));
        NeoForge.EVENT_BUS.addListener(this::onMasterTargetedBy);
    }

    protected void onMasterTargetedBy(LivingChangeTargetEvent event) {
        var otherEntity = event.getEntity();
        if (event.getOriginalTarget() == null) {
            return;
        }
        if (event.getOriginalTarget() == villager.getMaster()) {
            targets.removeIf(livingEntityWeakReference -> livingEntityWeakReference.get() == otherEntity);
        }
        if (otherEntity instanceof Piglin || (otherEntity instanceof Player player && FalsePlayer.isRealPlayer(player))) {
            return;
        }
        if (event.getNewTarget() == villager.getMaster()) {
            targets.add(new WeakReference<>(otherEntity));
        }
    }

    public void init() {
        targets.clear();
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
            this.ownerLastTargetedBy = livingentity.getLastHurtByMob();
            int i = livingentity.getLastHurtByMobTimestamp();
            if (i != this.timestamp && this.canAttack(this.ownerLastTargetedBy, TargetingConditions.DEFAULT)) {
                return true;
            }
            targets.removeIf(reference -> reference.get() == null);
            for (WeakReference<LivingEntity> entityWeakReference : targets) {
                if (this.canAttack(entityWeakReference.get(), TargetingConditions.DEFAULT)) {
                    this.ownerLastTargetedBy = entityWeakReference.get();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastTargetedBy);
        LivingEntity livingentity = this.villager.getMaster();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtByMobTimestamp();
        }

        super.start();
    }
}
