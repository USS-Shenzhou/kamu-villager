package cn.ussshenzhou.villager;

import net.minecraft.world.entity.player.Player;

/**
 * @author USS_Shenzhou
 * <br>CC0
 */
public class HXYAHelper {

    public static boolean isCen(Player player) {
        var name = player.getGameProfile().getName();
        return "BadCen".equals(name) || "Dev".equals(name);
    }

    public static boolean isKaMu(Player player) {
        var name = player.getGameProfile().getName();
        return "KaMuaMua".equals(name) || "Dev".equals(name);
    }

    public static boolean isHeiMao(Player player) {
        var name = player.getGameProfile().getName();
        return "Hei_Mao".equals(name) || "Dev".equals(name);
    }

    public static boolean isMelor(Player player) {
        var name = player.getGameProfile().getName();
        return "Melor_".equals(name) || "Dev".equals(name);
    }

    public static boolean isUncle(Player player) {
        var name = player.getGameProfile().getName();
        return "Uncle_Red".equals(name) || "Dev".equals(name);
    }
}
