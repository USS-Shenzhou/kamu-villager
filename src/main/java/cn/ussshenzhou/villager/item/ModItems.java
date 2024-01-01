package cn.ussshenzhou.villager.item;

import cn.ussshenzhou.villager.HXYAHelper;
import cn.ussshenzhou.villager.Villager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * @author USS_Shenzhou
 */
public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Villager.MOD_ID);

    public static final Supplier<Item> QUESTION_MARK = ITEMS.register("question_mark", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> IRON_ARMOR = ITEMS.register("iron_armor", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> DIAMOND_ARMOR = ITEMS.register("diamond_armor", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> NETHERITE_ARMOR = ITEMS.register("netherite_armor", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> IRON_TOOL = ITEMS.register("iron_tool", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> DIAMOND_TOOL = ITEMS.register("diamond_tool", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> NETHERITE_TOOL = ITEMS.register("netherite_tool", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> IRON_WEAPON = ITEMS.register("iron_weapon", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> DIAMOND_WEAPON = ITEMS.register("diamond_weapon", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> NETHERITE_WEAPON = ITEMS.register("netherite_weapon", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> BOW = ITEMS.register("bow", () -> new Item(new Item.Properties()));

    public static final Supplier<Item> LAVA_BOTTLE = ITEMS.register("lava_bottle", () -> new PotionItem(new Item.Properties().stacksTo(1)) {
        @Override
        public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving) {
            if (pEntityLiving instanceof ServerPlayer player && !HXYAHelper.isKaMu(player)) {
                player.setSecondsOnFire(5);
                pStack.shrink(1);
                return pStack;
            }
            return super.finishUsingItem(pStack, pLevel, pEntityLiving);
        }
    });
}
