package cn.ussshenzhou.villager.entity.fakeplayer.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * @author USS_Shenzhou
 */
public class Vec3Helper {

    public static Vec3 clone(Vec3 vec3) {
        return new Vec3(vec3.x, vec3.y, vec3.z);
    }

    public static BlockPos blockPos(Vec3 vec3) {
        return new BlockPos(Mth.floor(vec3.x), Mth.floor(vec3.y), Mth.floor(vec3.z));
    }
}
