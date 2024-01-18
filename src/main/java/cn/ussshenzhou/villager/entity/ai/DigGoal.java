package cn.ussshenzhou.villager.entity.ai;

import cn.ussshenzhou.villager.entity.Command;
import cn.ussshenzhou.villager.entity.VillagerVillager;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.LinkedHashSet;

/**
 * @author USS_Shenzhou
 */
public class DigGoal extends Goal {
    private final VillagerVillager villager;
    private Vector3f towards;
    protected int breakTime;
    protected int lastBreakProgress;
    private BlockPos targetPos = null;

    public DigGoal(VillagerVillager villager) {
        this.villager = villager;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return villager.getCommand() == Command.DIG && villager.getDigDirection() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return villager.getCommand() == Command.DIG && villager.getDigDirection() != null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        super.stop();
    }

    protected int getBreakTime() {
        var block = villager.level().getBlockState(targetPos);
        return (int) (block.getDestroySpeed(villager.level(), targetPos) / villager.getMainHandItem().getDestroySpeed(block) * 30 * 0.7f);
    }

    @Override
    public void tick() {
        assert villager.getDigDirection() != null;
        if (targetPos != null) {
            if (!checkPos(targetPos)) {
                targetPos = null;
            }
        }
        if (targetPos == null) {
            var d = villager.getDigDirection().mul(0.2f);
            villager.setDeltaMovement(d.x, d.y, d.z);
            targetPos = findTargetPos();
            if (targetPos == null) {
                return;
            }
        }
        villager.lookAt(EntityAnchorArgument.Anchor.EYES, targetPos.getCenter());
        dig();
    }

    private BlockPos findTargetPos() {
        assert villager.getDigDirection() != null;
        if (targetPos != null) {
            return targetPos;
        }
        LinkedHashSet<BlockPos> targets = new LinkedHashSet<>();
        var potentialTargetPos = new BlockPos.MutableBlockPos();
        if (villager.getDigDirection().y == 0) {
            for (float distance = 0.5f; distance < 3.5; distance++) {
                var reach = villager.getDigDirection().mul(distance);
                for (int degree = -45; degree < 45; degree += 10) {
                    var from = villager.position().toVector3f();
                    reach.rotateY(10f / 180 * 3.1415927f);
                    from.add(reach);
                    potentialTargetPos.set(from.x, from.y, from.z);
                    if (checkPos(potentialTargetPos)) {
                        targets.add(potentialTargetPos.immutable());
                        break;
                    }
                    if (checkPos(potentialTargetPos.move(0, 1, 0))) {
                        targets.add(potentialTargetPos.immutable());
                        break;
                    }
                }
            }
        } else {
            for (float y = -0.3f; y >= -1.3f; y--) {
                for (float x = -0.5f; x <= 0.5f; x++) {
                    for (float z = -0.5f; z <= 0.5f; z++) {
                        var from = villager.position().toVector3f().add(x, y, z);
                        potentialTargetPos.set(from.x, from.y, from.z);
                        if (checkPos(potentialTargetPos)) {
                            targets.add(potentialTargetPos.immutable());
                            break;
                        }
                    }
                }
            }
        }
        return targets.stream().findFirst().orElse(null);
    }

    private boolean checkPos(BlockPos potentialTargetPos) {
        var state = villager.level().getBlockState(potentialTargetPos);
        if (state.liquid()) {
            villager.level().setBlock(potentialTargetPos, Blocks.COBBLESTONE.defaultBlockState(), 2);
            return true;
        }
        return isBreakable(state);
    }

    private boolean isBreakable(BlockState blockState) {
        return !blockState.isAir();
    }

    private void dig() {
        if (!villager.swinging) {
            villager.swing(villager.getUsedItemHand());
        }
        ++this.breakTime;
        int i = (int) ((float) this.breakTime / (float) this.getBreakTime() * 10);
        if (i != this.lastBreakProgress) {
            villager.level().destroyBlockProgress(villager.getId(), targetPos, i);
            this.lastBreakProgress = i;
        }
        if (this.breakTime >= this.getBreakTime()) {
            villager.level().destroyBlock(targetPos, true);
            this.breakTime = 0;
            lastBreakProgress = -1;
        }
    }
}
