package cn.ussshenzhou.villager.entity.ai;

import cn.ussshenzhou.villager.entity.VillagerVillager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import org.joml.Vector3f;

import java.util.EnumSet;

/**
 * @author USS_Shenzhou
 */
public class DigGoalDisabled extends Goal {
    private final VillagerVillager villager;
    private Vector3f towards;
    protected int breakTime;
    protected int lastBreakProgress;
    private BlockPos targetPos = null;

    public DigGoalDisabled(VillagerVillager villager) {
        this.villager = villager;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return villager.getCommand() == VillagerVillager.Command.DIG && villager.getDigDirection() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return villager.getCommand() == VillagerVillager.Command.DIG && villager.getDigDirection() != null;
    }

    @Override
    public void start() {
        towards = new Vector3f(villager.getDigDirection()).normalize().mul(villager.getSpeed() * 0.75f);
        this.breakTime = 0;
        var farTarget = new Vector3f(towards).mul(20);
        villager.getNavigation().moveTo(farTarget.x, farTarget.y, farTarget.z, villager.getSpeed() * 0.75f);
    }

    @Override
    public void stop() {
        super.stop();
    }

    protected int getBreakTime() {
        var block = villager.level().getBlockState(targetPos);
        return (int) (block.getDestroySpeed(villager.level(), targetPos) * villager.getMainHandItem().getDestroySpeed(block) * 15);
    }

    @Override
    public void tick() {
        //var des = villager.position().toVector3f().add(towards).add(towards);
        //villager.getNavigation().moveTo(des.x, des.y, des.z, villager.getSpeed() * 0.75f);
        if (villager.getNavigation().isStuck()) {
            if (targetPos == null) {
                var des = villager.position().toVector3f().add(towards).add(towards);
                targetPos = new BlockPos((int) des.x, (int) des.y, (int) des.z);
                villager.getNavigation().moveTo(des.x, des.y, des.z, villager.getSpeed() * 0.75f);
            }
            if (!villager.swinging) {
                villager.swing(villager.getUsedItemHand());
            }
        }

        ++this.breakTime;
        int i = (int) ((float) this.breakTime / (float) this.getBreakTime() * 10);
        if (i != this.lastBreakProgress) {
            villager.level().destroyBlockProgress(villager.getId(), targetPos, i);
            this.lastBreakProgress = i;
        }

        if (this.breakTime >= this.getBreakTime()) {
            villager.level().destroyBlock(targetPos, false);
            this.breakTime = 0;
            lastBreakProgress = -1;
        }
    }
}
