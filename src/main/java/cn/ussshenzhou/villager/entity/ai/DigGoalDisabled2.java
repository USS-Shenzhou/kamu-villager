package cn.ussshenzhou.villager.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author USS_Shenzhou
 */
public class DigGoalDisabled2 extends BreakDoorGoal {
    private final List<Integer> anglesToAttemptBreak = IntStream.rangeClosed(-45, 45).filter(i -> i % 10 == 0).boxed().toList();

    public DigGoalDisabled2(Mob pMob) {

        super(pMob, (o) -> true);
        this.doorBreakTime = 24 * 20;
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!GoalUtils.hasGroundPathNavigation(this.mob)) {
            return false;
        } else {
            GroundPathNavigation groundpathnavigation = (GroundPathNavigation) this.mob.getNavigation();
            Path path = groundpathnavigation.getPath();
            if (path != null) {
                if (!path.isDone()) {
                    for (int i = 0; i < Math.min(path.getNextNodeIndex() + 2, path.getNodeCount()); ++i) {
                        Node node = path.getNode(i);
                        this.doorPos = new BlockPos(node.x, node.y + 1, node.z);
                        if (this.breakable()) {
                            return true;
                        }
                        this.doorPos = new BlockPos(node.x, node.y, node.z);
                        if (this.breakable()) {
                            return true;
                        }
                    }

                }
            } else {
                if (mob.getTarget() != null) {
                    var direction = mob.getTarget().position().subtract(mob.position()).normalize().scale(0.9);
                    for (int angle : anglesToAttemptBreak) {
                        var p = rotateVector(direction, new Vec3(0, 1, 0), angle);
                        p = p.add(mob.position().add(0, 1, 0));
                        this.doorPos = new BlockPos((int) p.x, (int) p.y, (int) p.z);
                        if (this.breakable()) {
                            return true;
                        }

                        p = p.add(0, -1, 0);
                        this.doorPos = new BlockPos((int) p.x, (int) p.y, (int) p.z);
                        if (this.breakable()) {
                            return true;
                        }
                    }
                }
                this.doorPos = this.mob.blockPosition().above();
                return this.breakable();
            }
        }
        return false;
    }

    public static Vec3 rotateVector(Vec3 v, Vec3 k, double degrees) {
        double theta = Math.toRadians(degrees);
        k = k.normalize();
        return v
                .scale(Math.cos(theta))
                .add(k.cross(v)
                        .scale(Math.sin(theta)))
                .add(k.scale(k.dot(v))
                        .scale(1 - Math.cos(theta)));
    }

    private boolean breakable() {
        if (this.mob.distanceToSqr(this.doorPos.getX(), this.doorPos.getY(), this.doorPos.getZ()) <= 2.25) {
            var blockState = mob.level().getBlockState(doorPos);
            return !blockState.isAir() && blockState.getBlock().getExplosionResistance() <= 6;
        }
        return false;
    }

    double speedBuffer;

    @Override
    public void start() {
        super.start();
        speedBuffer = mob.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();
        mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.isOpen() || (this.breakTime < this.getDoorBreakTime() && this.mob.distanceToSqr(this.doorPos.getX(), this.doorPos.getY(), this.doorPos.getZ()) < 4);
    }

    @Override
    public void stop() {
        mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speedBuffer);
        mob.getNavigation().stop();
        this.mob.level().destroyBlockProgress(this.mob.getId(), this.doorPos, -1);
    }

    @Override
    protected boolean isOpen() {
        if (this.hasDoor) {
            BlockState blockstate = this.mob.level().getBlockState(this.doorPos);
            if (blockstate.getBlock() instanceof DoorBlock) {
                return blockstate.getValue(DoorBlock.OPEN);
            } else if (blockstate.isAir()) {
                this.hasDoor = false;
                return true;
            }
        }
        return false;
    }

    @Override
    protected int getDoorBreakTime() {
        doorBreakTime = (int) (mob.level().getBlockState(doorPos).getDestroySpeed(mob.level(), doorPos) * 2.5 * 20);
        return doorBreakTime;
    }

    @Override
    public void tick() {
        if (!this.mob.swinging) {
            this.mob.swing(this.mob.getUsedItemHand());
        }

        ++this.breakTime;
        int i = (int) ((float) this.breakTime / (float) this.getDoorBreakTime() * 10);
        if (i != this.lastBreakProgress) {
            this.mob.level().destroyBlockProgress(this.mob.getId(), this.doorPos, i);
            this.lastBreakProgress = i;
        }

        if (this.breakTime >= this.getDoorBreakTime()) {
            this.mob.level().destroyBlock(this.doorPos, false);
        }
    }
}
