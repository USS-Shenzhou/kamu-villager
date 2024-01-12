package cn.ussshenzhou.villager.entity.fakeplayer.utils;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BotUtils {

    public static final Set<Block> NO_FALL = new HashSet<>(Arrays.asList(
            Blocks.WATER,
            Blocks.LAVA,
            Blocks.TWISTING_VINES,
            Blocks.TWISTING_VINES_PLANT,
            Blocks.WEEPING_VINES,
            Blocks.WEEPING_VINES_PLANT,
            Blocks.SWEET_BERRY_BUSH,
            Blocks.POWDER_SNOW,
            Blocks.COBWEB,
            Blocks.VINE
    ));

    public static UUID randomSteveUUID() {
        UUID uuid = UUID.randomUUID();

        if (uuid.hashCode() % 2 == 0) {
            return uuid;
        }

        return randomSteveUUID();
    }

    public static boolean overlaps(AABB playerBox, AABB blockBox) {
        return playerBox.intersects(blockBox);
    }

    public static double getHorizSqDist(BlockPos blockLoc, BlockPos pLoc) {
        return square(blockLoc.getX() + 0.5 - pLoc.getX()) + square(blockLoc.getZ() + 0.5 - pLoc.getZ());
    }

    public static double getHorizSqDist(Vec3 blockA, Vec3 pB) {
        return square(blockA.x + 0.5 - pB.x) + square(blockA.z + 0.5 - pB.z);
    }

    public static double square(double d) {
        return d * d;
    }
}
