package cn.ussshenzhou.villager.entity.fakeplayer.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class LegacyWorldManager {

    public static boolean aboveGround(Level level, BlockPos loc) {
        int y = 1;

        while (y < 25) {
            if (!level.getBlockState(loc).isAir()) {
                return false;
            }

            y++;
        }

        return true;
    }
}
