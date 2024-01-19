package cn.ussshenzhou.villager.entity.fakeplayer.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * @author USS_Shenzhou
 * <br/>This file is modified from <a href="https://github.com/HorseNuggets/TerminatorPlus">TerminatorPlus</a> under EPL-2.0 license, and can be distributed under EPL-2.0 license only.
 */
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
