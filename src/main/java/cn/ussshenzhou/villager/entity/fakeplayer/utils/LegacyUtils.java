package cn.ussshenzhou.villager.entity.fakeplayer.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

/**
 * @author USS_Shenzhou
 * <br/>This file is modified from <a href="https://github.com/HorseNuggets/TerminatorPlus">TerminatorPlus</a> under EPL-2.0 license, and can be distributed under EPL-2.0 license only.
 */
public class LegacyUtils {

    public static boolean checkFreeSpace(BlockPos a, BlockPos b, Level level) {
        var v = new Vector3f(b.getX() - a.getX(), b.getY() - a.getY(), b.getZ() - a.getZ());

        int n = 32;
        double m = 1 / (double) n;

        double j = Math.floor(v.length() * n);
        v.mul((float) (m / v.length()));

        if (level == null) {
            return false;
        }

        for (int i = 0; i <= j; i++) {
            var block = level.getBlockState(a.offset(Mth.floor(v.x * i), Mth.floor(v.y * i), Mth.floor(v.z * i)));

            if (!LegacyMats.AIR.contains(block.getBlock())) {
                return false;
            }
        }

        return true;
    }
}
