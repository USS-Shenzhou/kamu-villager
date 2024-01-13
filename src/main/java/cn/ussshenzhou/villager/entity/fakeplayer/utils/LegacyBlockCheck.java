package cn.ussshenzhou.villager.entity.fakeplayer.utils;

import cn.ussshenzhou.t88.task.TaskHelper;
import cn.ussshenzhou.villager.entity.fakeplayer.FalsePlayer;
import cn.ussshenzhou.villager.entity.fakeplayer.FalsePlayerTickHelper;
import com.google.common.base.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class LegacyBlockCheck {

    private static void placeFinal(FalsePlayer bot, LivingEntity player, BlockPos loc) {
        if (bot.level().getBlockState(loc).getBlock() != Blocks.COBBLESTONE) {
            bot.level().playSound(null, loc, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1, 1);
            bot.setItem(new ItemStack(Blocks.COBBLESTONE));
            bot.level().setBlock(loc, Blocks.COBBLESTONE.defaultBlockState(), 1);

            Block under = bot.level().getBlockState(loc.offset(0, -1, 0)).getBlock();
            if (under == Blocks.LAVA) {
                bot.level().setBlock(loc.offset(0, -1, 0), Blocks.COBBLESTONE.defaultBlockState(), 1);
            }
        }
    }

    public static void placeBlock(FalsePlayer bot, LivingEntity player, Block block, BlockPos pos) {

        var level = bot.level();
        Block under = level.getBlockState(pos.offset(0, -1, 0)).getBlock();

        if (LegacyMats.SPAWN.contains(under)) {
            placeFinal(bot, player, pos.offset(0, -1, 0));
            TaskHelper.addServerTask(() -> {
                placeFinal(bot, player, pos);
            }, 2);
        }

        Set<Block> face = new HashSet<>(Arrays.asList(
                level.getBlockState(pos.offset(1, 0, 0)).getBlock(),
                level.getBlockState(pos.offset(-1, 0, 0)).getBlock(),
                level.getBlockState(pos.offset(0, 0, 1)).getBlock(),
                level.getBlockState(pos.offset(0, 0, -1)).getBlock()));

        boolean a = false;
        for (Block side : face) {
            if (!LegacyMats.SPAWN.contains(side)) {
                a = true;
                //needtest
                break;
            }
        }

        if (a) {
            placeFinal(bot, player, pos);
            return;
        }

        Set<Block> edge = new HashSet<>(Arrays.asList(
                level.getBlockState(pos.offset(1, -1, 0)).getBlock(),
                level.getBlockState(pos.offset(-1, -1, 0)).getBlock(),
                level.getBlockState(pos.offset(0, -1, 1)).getBlock(),
                level.getBlockState(pos.offset(0, -1, -1)).getBlock()));

        boolean b = false;
        for (Block side : edge) {
            if (!LegacyMats.SPAWN.contains(side)) {
                b = true;
                //needtest
                break;
            }
        }

        if (b && LegacyMats.SPAWN.contains(under)) {
            placeFinal(bot, player, pos.offset(0, -1, 0));
            TaskHelper.addServerTask(() -> {
                placeFinal(bot, player, pos);
            }, 2);
            return;
        }

        Block c1 = level.getBlockState(pos.offset(1, -1, 1)).getBlock();
        Block c2 = level.getBlockState(pos.offset(1, -1, -1)).getBlock();
        Block c3 = level.getBlockState(pos.offset(-1, -1, 1)).getBlock();
        Block c4 = level.getBlockState(pos.offset(-1, -1, -1)).getBlock();

        boolean t = false;

        if (!LegacyMats.SPAWN.contains(c1) || !LegacyMats.SPAWN.contains(c2)) {

            Block b1 = level.getBlockState(pos.offset(1, -1, 0)).getBlock();
            if (LegacyMats.SPAWN.contains(b1)) {
                placeFinal(bot, player, pos.offset(1, -1, 0));
            }

            t = true;

        } else if (!LegacyMats.SPAWN.contains(c3) || !LegacyMats.SPAWN.contains(c4)) {

            Block b1 = level.getBlockState(pos.offset(-1, -1, 0)).getBlock();
            if (LegacyMats.SPAWN.contains(b1)) {
                placeFinal(bot, player, pos.offset(-1, -1, 0));
            }

            t = true;
        }

        if (t) {
            TaskHelper.addServerTask(() -> {
                var b2Pos = pos.offset(0, -1, 0);
                Block b2 = level.getBlockState(b2Pos).getBlock();
                if (LegacyMats.SPAWN.contains(b2)) {
                    bot.level().playSound(null, b2Pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1, 1);
                    placeFinal(bot, player,b2Pos);
                }
            }, 1);

            TaskHelper.addServerTask( () -> {
                bot.level().playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1, 1);
                placeFinal(bot, player, pos);
            }, 3);
            return;
        }

        bot.level().playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1, 1);
        placeFinal(bot, player, pos);
    }

    public static boolean tryPreMLG(FalsePlayer bot, Vec3 botLoc) {
        if (bot.isBotOnGround() || bot.getVelocity().y >= -0.8D || bot.getNoFallTicks() > 7) {
            return false;
        }
        if (tryPreMLG(bot, botLoc, 3)) {
            return true;
        }
        return tryPreMLG(bot, botLoc, 2);
    }

    private static boolean tryPreMLG(FalsePlayer falsePlayer, Vec3 botLoc, int blocksBelow) {
        AABB box = falsePlayer.getBoundingBox();
        double[] xVals = new double[]{
                box.minX,
                box.maxX - 0.01
        };

        double[] zVals = new double[]{
                box.minZ,
                box.maxZ - 0.01
        };
        Set<Vec3> below2Set = new HashSet<>();

        for (double x : xVals) {
            for (double z : zVals) {
                Vec3 below = Vec3Helper.clone(botLoc);
                below.x = (x);
                below.z = (z);
                below.y = falsePlayer.getOnPos().getY();
                for (int i = 0; i < blocksBelow - 1; i++) {
                    below.y = (below.y - 1);

                    // Blocks before must all be pass-through
                    BlockState state = falsePlayer.level().getBlockState(Vec3Helper.blockPos(below));
                    if (state.isSolid() || LegacyMats.canStandOn(state.getBlock())) {
                        return false;
                    }
                    below = Vec3Helper.clone(below);
                }
                below.y = (falsePlayer.getOnPos().getY() - blocksBelow);
                below2Set.add(below);
            }
        }

        // Second block below must have at least one unplaceable block (that is landable)
        boolean nether = falsePlayer.level().dimension() == Level.NETHER;
        Iterator<Vec3> itr = below2Set.iterator();
        while (itr.hasNext()) {
            var pos = Vec3Helper.blockPos(itr.next());
            var next = falsePlayer.level().getBlockState(pos);
            boolean placeable = nether ? LegacyMats.canPlaceTwistingVines(next)
                    : LegacyMats.canPlaceWater(pos, next, Optional.absent());
            if (placeable || (!next.isSolid() && !LegacyMats.canStandOn(next.getBlock()))) {
                itr.remove();
            }
        }

        // Clutch
        if (!below2Set.isEmpty()) {
            List<Vec3> below2List = new ArrayList<>(below2Set);
            below2List.sort((a, b) -> {
                BlockState aBlock = falsePlayer.level().getBlockState(Vec3Helper.blockPos(a).offset(0, 1, 0));
                BlockState bBlock = falsePlayer.level().getBlockState(Vec3Helper.blockPos(b).offset(0, 1, 0));
                if (aBlock.isAir() && !bBlock.isAir()) {
                    return -1;
                }
                if (!bBlock.isAir() && aBlock.isAir()) {
                    return 1;
                }
                return Double.compare(BotUtils.getHorizSqDist(a, botLoc), BotUtils.getHorizSqDist(b, botLoc));
            });

            Vec3 faceLoc = below2List.get(0);
            Vec3 loc = Vec3Helper.clone(faceLoc).add(0, 1, 0);
            falsePlayer.face(faceLoc);
            falsePlayer.look(BlockFace.DOWN);

            TaskHelper.addServerTask((t) -> falsePlayer.face(faceLoc), 1);

            falsePlayer.punch();
            falsePlayer.level().playSound(null, falsePlayer.getOnPos(), SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1, 1);
            falsePlayer.setItem(new ItemStack(Blocks.COBBLESTONE));
            falsePlayer.level().setBlock(Vec3Helper.blockPos(loc), Blocks.COBBLESTONE.defaultBlockState(), 1);
        }

        return false;
    }

    public static void clutch(FalsePlayer falsePlayer, LivingEntity target) {
        Vec3 botLoc = falsePlayer.position();

        Block type = falsePlayer.level().getBlockState(falsePlayer.getOnPos().offset(0, -1, 0)).getBlock();
        Block type2 = falsePlayer.level().getBlockState(falsePlayer.getOnPos().offset(0, -2, 0)).getBlock();

        if (!(LegacyMats.SPAWN.contains(type) && LegacyMats.SPAWN.contains(type2))) {
            return;
        }

        if (target.position().y >= botLoc.y) {
            BlockPos pos = Vec3Helper.blockPos(botLoc).offset(0, -1, 0);

            Set<Tuple<BlockPos, Block>> face = new HashSet<>(Arrays.asList(
                    new Tuple<>(pos.offset(1, 0, 0), falsePlayer.level().getBlockState(pos.offset(1, 0, 0)).getBlock()),
                    new Tuple<>(pos.offset(-1, 0, 0), falsePlayer.level().getBlockState(pos.offset(-1, 0, 0)).getBlock()),
                    new Tuple<>(pos.offset(0, 0, 1), falsePlayer.level().getBlockState(pos.offset(0, 0, 1)).getBlock()),
                    new Tuple<>(pos.offset(0, 0, -1), falsePlayer.level().getBlockState(pos.offset(0, 0, -1)).getBlock())
            ));

            BlockPos at = null;
            for (Tuple<BlockPos, Block> side : face) {
                if (!LegacyMats.SPAWN.contains(side.getB())) {
                    at = side.getA();
                }
            }

            if (at != null) {
                FalsePlayerTickHelper.SLOW.add(falsePlayer);
                FalsePlayerTickHelper.NO_FACE.add(falsePlayer);

                TaskHelper.addServerTask((t) -> {
                    falsePlayer.stand();
                    FalsePlayerTickHelper.SLOW.remove(falsePlayer);
                }, 12);

                TaskHelper.addServerTask((t) -> {
                    FalsePlayerTickHelper.NO_FACE.remove(falsePlayer);
                }, 15);

                Vec3 faceLoc = new Vec3(at.getX(), at.getY(), at.getZ()).add(0, -1.5, 0);

                falsePlayer.face(faceLoc);
                falsePlayer.look(BlockFace.DOWN);
                TaskHelper.addServerTask((t) -> {
                    falsePlayer.face(faceLoc);
                }, 1);

                falsePlayer.punch();
                falsePlayer.sneak();
                falsePlayer.level().playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1, 1);
                falsePlayer.setItem(new ItemStack(Blocks.COBBLESTONE));
                falsePlayer.level().setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), 1);
            }
        }
    }
}
