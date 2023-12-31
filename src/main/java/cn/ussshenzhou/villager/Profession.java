package cn.ussshenzhou.villager;

import cn.ussshenzhou.villager.gui.ProfessionHud;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;

/**
 * @author USS_Shenzhou
 */

public enum Profession {
    NITWIT(Items.KNOWLEDGE_BOOK, "傻子"),
    ARMORER(Items.IRON_CHESTPLATE, "盔甲匠"),
    BUTCHER(Items.PORKCHOP, "屠夫"),
    CLERIC(Items.BREWING_STAND, "牧师"),
    FARMER(Items.IRON_HOE, "农民"),
    FLETCHER(Items.BOW, "制剑师"),
    TOOL_SMITH(Items.IRON_PICKAXE, "工具匠"),
    WEAPON_SMITH(Items.IRON_SWORD, "武器匠");

    public final Item icon;
    public final String name;

    Profession(Item icon, String name) {
        this.icon = icon;
        this.name = name;
    }

    public static @Nullable Profession fromBlock(Block block) {
        if (block == Blocks.BLAST_FURNACE) {
            return ARMORER;
        } else if (block == Blocks.SMOKER) {
            return BUTCHER;
        } else if (block == Blocks.BREWING_STAND) {
            return CLERIC;
        } else if (block == Blocks.COMPOSTER) {
            return FARMER;
        } else if (block == Blocks.FLETCHING_TABLE) {
            return FLETCHER;
        } else if (block == Blocks.SMITHING_TABLE) {
            return TOOL_SMITH;
        } else if (block == Blocks.GRINDSTONE) {
            return WEAPON_SMITH;
        }
        return null;
    }
}
