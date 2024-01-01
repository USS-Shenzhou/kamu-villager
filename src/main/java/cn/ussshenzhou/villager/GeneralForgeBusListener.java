package cn.ussshenzhou.villager;

import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.villager.item.ModItems;
import cn.ussshenzhou.villager.network.ChooseProfessionPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GeneralForgeBusListener {

    /*@SubscribeEvent
    public static void onPlayerDeath(PlayerEvent.Clone event) {
        if (event.isWasDeath() && event.getOriginal().hasData(ModDataAttachments.PROFESSION)) {
            event.getEntity().setData(ModDataAttachments.PROFESSION, event.getOriginal().getData(ModDataAttachments.PROFESSION).get());
        }
    }*/

    @SubscribeEvent
    public static void onPlayerCreate(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().hasData(ModDataAttachments.PROFESSION)) {
            event.getEntity().setData(ModDataAttachments.PROFESSION, Profession.NITWIT);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        var player = event.getEntity();
        if (!player.level().isClientSide) {
            NetworkHelper.sendToPlayer((ServerPlayer) player, new ChooseProfessionPacket(player, player.getData(ModDataAttachments.PROFESSION).get()));
        }
    }

    //-----Eastern Eggs-----

    public static ItemStack LAVA_BOTTLE = null;

    @SubscribeEvent
    public static void justInit(PlayerEvent.PlayerLoggedInEvent event) {
        if (LAVA_BOTTLE == null) {
            LAVA_BOTTLE = new ItemStack(ModItems.LAVA_BOTTLE.get());
            LAVA_BOTTLE.getOrCreateTag().putInt("CustomPotionColor", 0x00ff9900);
            var list = new ListTag();
            var c = new CompoundTag();
            c.putBoolean("ambient", false);
            c.putByte("amplifier", (byte) 1);
            c.putInt("duration", 400);
            c.putString("id","minecraft:regeneration");
            c.putBoolean("show_icon", true);
            c.putBoolean("show_particles", true);
            list.add(c);
            LAVA_BOTTLE.setHoverName(Component.literal("一小瓶热腾腾的饮料"));
            LAVA_BOTTLE.getOrCreateTag().put("custom_potion_effects", list);
            LAVA_BOTTLE.getOrCreateTag().putString("Potion","minecraft:fire_resistance");
        }
    }

    public static final ItemStack CEN_AXE = new ItemStack(Items.DIAMOND_AXE);

    static {
        CEN_AXE.enchant(Enchantments.SHARPNESS, 6);
        CEN_AXE.getOrCreateTag().putBoolean("Unbreakable", true);
        var l = new ListTag();
        l.add(StringTag.valueOf("不可丢出"));
        var c = new CompoundTag();
        c.put("Lore", l);
        CEN_AXE.getOrCreateTag()
                .put("display", c);
    }

    public static final ItemStack MAO_SHOVEL = new ItemStack(Items.DIAMOND_SHOVEL);

    static {
        MAO_SHOVEL.enchant(Enchantments.BLOCK_FORTUNE, 4);
        MAO_SHOVEL.enchant(Enchantments.BLOCK_EFFICIENCY, 6);
        CEN_AXE.getOrCreateTag().putBoolean("Unbreakable", true);
        var l = new ListTag();
        l.add(StringTag.valueOf("不可丢出"));
        var c = new CompoundTag();
        c.put("Lore", l);
        CEN_AXE.getOrCreateTag()
                .put("display", c);
    }

    private static final List<ItemStack> UNDROPPABLE = List.of(CEN_AXE, MAO_SHOVEL);

    @SubscribeEvent
    public static void unDroppableItem(ItemTossEvent event) {
        if (undroppable(event.getEntity().getItem())) {
            event.setCanceled(true);
            event.getPlayer().addItem(event.getEntity().getItem());
            event.getPlayer().getInventory().setChanged();
        }
    }

    private static boolean undroppable(ItemStack itemStack) {
        for (ItemStack i : UNDROPPABLE) {
            if (ItemStack.isSameItemSameTags(i, itemStack)) {
                return true;
            }
        }
        return false;
    }
}
