package cn.ussshenzhou.villager.entity.fakeplayer.utils;

import cn.ussshenzhou.t88.util.TaskHelper;
import cn.ussshenzhou.villager.entity.fakeplayer.FalsePlayer;
import cn.ussshenzhou.villager.entity.fakeplayer.FalsePlayerTickHelper;
import com.google.common.base.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class LegacyBlockCheck {

    private final FalsePlayerTickHelper helper;

    public LegacyBlockCheck(FalsePlayerTickHelper helper) {
        this.helper = helper;
    }

    /*private void placeFinal(FalsePlayer bot, LivingEntity player, Vec3 loc) {
        if (loc.getBlock().getType() != Material.COBBLESTONE) {
            for (Player all : Bukkit.getOnlinePlayers())
                all.playSound(loc, Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
            bot.setItem(new ItemStack(Material.COBBLESTONE));
            loc.getBlock().setType(Material.COBBLESTONE);

            Block under = loc.clone().add(0, -1, 0).getBlock();
            if (under.getType() == Material.LAVA) {
                under.setType(Material.COBBLESTONE);
            }
        }
    }

    public void placeBlock(FalsePlayer bot, LivingEntity player, Block block) {

        Vec3 loc = block.getVec3();

        Block under = loc.clone().add(0, -1, 0).getBlock();

        if (LegacyMats.SPAWN.contains(under.getType())) {
            placeFinal(bot, player, loc.clone().add(0, -1, 0));
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                placeFinal(bot, player, block.getVec3());
            }, 2);
        }

        Set<Block> face = new HashSet<>(Arrays.asList(loc.clone().add(1, 0, 0).getBlock(),
                loc.clone().add(-1, 0, 0).getBlock(),
                loc.clone().add(0, 0, 1).getBlock(),
                loc.clone().add(0, 0, -1).getBlock()));

        boolean a = false;
        for (Block side : face) {
            if (!LegacyMats.SPAWN.contains(side.getType())) {
                a = true;
            }
        }

        if (a) {
            placeFinal(bot, player, block.getVec3());
            return;
        }

        Set<Block> edge = new HashSet<>(Arrays.asList(loc.clone().add(1, -1, 0).getBlock(),
                loc.clone().add(-1, -1, 0).getBlock(),
                loc.clone().add(0, -1, 1).getBlock(),
                loc.clone().add(0, -1, -1).getBlock()));

        boolean b = false;
        for (Block side : edge) {
            if (!LegacyMats.SPAWN.contains(side.getType())) {
                b = true;
            }
        }

        if (b && LegacyMats.SPAWN.contains(under.getType())) {
            placeFinal(bot, player, loc.clone().add(0, -1, 0));
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                placeFinal(bot, player, block.getVec3());
            }, 2);
            return;
        }

        Block c1 = loc.clone().add(1, -1, 1).getBlock();
        Block c2 = loc.clone().add(1, -1, -1).getBlock();
        Block c3 = loc.clone().add(-1, -1, 1).getBlock();
        Block c4 = loc.clone().add(-1, -1, -1).getBlock();

        boolean t = false;

        if (!LegacyMats.SPAWN.contains(c1.getType()) || !LegacyMats.SPAWN.contains(c2.getType())) {

            Block b1 = loc.clone().add(1, -1, 0).getBlock();
            if (LegacyMats.SPAWN.contains(b1.getType())) {
                placeFinal(bot, player, b1.getVec3());
            }

            t = true;

        } else if (!LegacyMats.SPAWN.contains(c3.getType()) || !LegacyMats.SPAWN.contains(c4.getType())) {

            Block b1 = loc.clone().add(-1, -1, 0).getBlock();
            if (LegacyMats.SPAWN.contains(b1.getType())) {
                placeFinal(bot, player, b1.getVec3());
            }

            t = true;
        }

        if (t) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Block b2 = loc.clone().add(0, -1, 0).getBlock();
                if (LegacyMats.SPAWN.contains(b2.getType())) {
                    for (Player all : Bukkit.getOnlinePlayers())
                        all.playSound(loc, Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
                    placeFinal(bot, player, b2.getVec3());
                }
            }, 1);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Player all : Bukkit.getOnlinePlayers())
                    all.playSound(loc, Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
                placeFinal(bot, player, block.getVec3());
            }, 3);
            return;
        }

        for (Player all : Bukkit.getOnlinePlayers())
            all.playSound(loc, Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
        placeFinal(bot, player, block.getVec3());
    }*/

    public boolean tryPreMLG(FalsePlayer bot, Vec3 botLoc) {
        if (bot.isBotOnGround() || bot.getVelocity().y >= -0.8D || bot.getNoFallTicks() > 7) {
            return false;
        }
        if (tryPreMLG(bot, botLoc, 3)) {
            return true;
        }
        return tryPreMLG(bot, botLoc, 2);
    }

    private boolean tryPreMLG(FalsePlayer falsePlayer, Vec3 botLoc, int blocksBelow) {
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

            TaskHelper.addServerTask(() -> falsePlayer.face(faceLoc), 1);

            falsePlayer.punch();
            falsePlayer.level().playSound(null, falsePlayer.getOnPos(), SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1, 1);
            falsePlayer.setItem(new ItemStack(Blocks.COBBLESTONE));
            falsePlayer.level().setBlock(Vec3Helper.blockPos(loc), Blocks.COBBLESTONE.defaultBlockState(), 1);
        }

        return false;
    }

    public void clutch(FalsePlayer bot, LivingEntity target) {
        Vec3 botLoc = bot.getVec3();

        Material type = botLoc.clone().add(0, -1, 0).getBlock().getType();
        Material type2 = botLoc.clone().add(0, -2, 0).getBlock().getType();

        if (!(LegacyMats.SPAWN.contains(type) && LegacyMats.SPAWN.contains(type2))) {
            return;
        }

        if (target.getVec3().getBlockY() >= botLoc.getBlockY()) {
            Vec3 loc = botLoc.clone().add(0, -1, 0);

            Set<Block> face = new HashSet<>(Arrays.asList(
                    loc.clone().add(1, 0, 0).getBlock(),
                    loc.clone().add(-1, 0, 0).getBlock(),
                    loc.clone().add(0, 0, 1).getBlock(),
                    loc.clone().add(0, 0, -1).getBlock()
            ));

            Vec3 at = null;
            for (Block side : face) {
                if (!LegacyMats.SPAWN.contains(side.getType())) {
                    at = side.getVec3();
                }
            }

            if (at != null) {
                helper.slow.add(bot);
                helper.noFace.add(bot);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    bot.stand();
                    helper.slow.remove(bot);
                }, 12);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    helper.noFace.remove(bot);
                }, 15);

                Vec3 faceLoc = at.clone().add(0, -1.5, 0);

                bot.faceVec3(faceLoc);
                bot.look(BlockFace.DOWN);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    bot.faceVec3(faceLoc);
                }, 1);

                bot.punch();
                bot.sneak();
                for (Player all : Bukkit.getOnlinePlayers()) {
                    all.playSound(loc, Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
                }
                bot.setItem(new ItemStack(Material.COBBLESTONE));
                loc.getBlock().setType(Material.COBBLESTONE);
            }
        }
    }
}
