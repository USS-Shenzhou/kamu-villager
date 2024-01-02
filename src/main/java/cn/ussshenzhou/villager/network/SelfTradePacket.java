package cn.ussshenzhou.villager.network;

import cn.ussshenzhou.t88.network.annotation.Consumer;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import cn.ussshenzhou.t88.util.InventoryHelper;
import cn.ussshenzhou.villager.item.ModItems;
import com.google.common.collect.Sets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.PlayNetworkDirection;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static net.minecraft.world.item.Items.*;

/**
 * @author USS_Shenzhou
 */
@NetPacket
public class SelfTradePacket {
    public final UUID uuid;
    public final ItemStack from;
    public final ItemStack to;

    public SelfTradePacket(UUID uuid, ItemStack from, ItemStack to) {
        this.uuid = uuid;
        this.from = from;
        this.to = to;
    }

    @Decoder
    public SelfTradePacket(FriendlyByteBuf buf) {
        uuid = buf.readUUID();
        from = buf.readItem();
        to = buf.readItem();
    }

    @Encoder
    public void writeToNet(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeItem(from);
        buf.writeItem(to);
    }

    public void serverHandler(Player player) {
        if (player == null) {
            return;
        }
        var inventory = player.getInventory();
        if (from.is(Tags.Items.ARMORS) || from.is(Tags.Items.TOOLS) || from.getItem() instanceof PotionItem) {
            //sell equipments
            if (InventoryHelper.consumeExact(inventory, from)) {
                tryAdd(player, to);
            }
        } else if (InventoryHelper.consume(inventory, from)) {
            if (to.is(ModItems.IRON_ARMOR.get())) {
                tryAdd(player, randomPickAndEnchant(player, IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS));
            } else if (to.is(ModItems.DIAMOND_ARMOR.get())) {
                tryAdd(player, randomPickAndEnchant(player, DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS));
            } else if (to.is(ModItems.NETHERITE_ARMOR.get())) {
                tryAdd(player, randomPickAndEnchant(player, NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS, NETHERITE_BOOTS));
            } else if (to.is(ModItems.IRON_TOOL.get())) {
                tryAdd(player, randomPickAndEnchant(player, IRON_PICKAXE, IRON_SHOVEL));
            } else if (to.is(ModItems.DIAMOND_TOOL.get())) {
                tryAdd(player, randomPickAndEnchant(player, DIAMOND_PICKAXE, DIAMOND_SHOVEL));
            } else if (to.is(ModItems.NETHERITE_TOOL.get())) {
                tryAdd(player, randomPickAndEnchant(player, NETHERITE_PICKAXE, NETHERITE_SHOVEL));
            } else if (to.is(ModItems.IRON_WEAPON.get())) {
                tryAdd(player, randomPickAndEnchant(player, IRON_SWORD, IRON_AXE));
            } else if (to.is(ModItems.DIAMOND_WEAPON.get())) {
                tryAdd(player, randomPickAndEnchant(player, DIAMOND_SWORD, DIAMOND_AXE));
            } else if (to.is(ModItems.NETHERITE_WEAPON.get())) {
                tryAdd(player, randomPickAndEnchant(player, NETHERITE_SWORD, NETHERITE_AXE));
            } else if (to.is(ModItems.BOW.get())) {
                tryAdd(player, randomPickAndEnchant(player, BOW));
            } else {
                tryAdd(player, to);
            }
        }
    }

    private ItemStack randomPickAndEnchant(Player player, Item... items) {
        var f = player.getRandom().nextFloat();
        return randomEnchant(player, new ItemStack(items[(int) (f * items.length)]));
    }

    private ItemStack randomEnchant(Player player, ItemStack item) {
        List<EnchantmentInstance> list = this.getEnchantmentList(player, item, 30);
        for (EnchantmentInstance enchantmentinstance : list) {
            item.enchant(enchantmentinstance.enchantment, enchantmentinstance.level);
        }
        return item;
    }

    private List<EnchantmentInstance> getEnchantmentList(Player player, ItemStack pStack, int level) {
        List<EnchantmentInstance> list = EnchantmentHelper.selectEnchantment(player.getRandom(), pStack, level, false);
        if (pStack.is(Items.BOOK) && list.size() > 1) {
            list.remove(player.getRandom().nextInt(list.size()));
        }
        return list;
    }

    private void tryAdd(Player player, ItemStack item) {
        if (!player.addItem(item.copy())) {
            player.drop(item.copy(), false);
        }
        player.getInventory().setChanged();
    }

    @Consumer
    public void serverHandler(NetworkEvent.Context context) {
        if (context.getDirection().equals(PlayNetworkDirection.PLAY_TO_SERVER)) {
            //NetworkHelper.sendTo(PacketDistributor.PLAYER.with(context::getSender), this);
            MinecraftServer minecraftServer = (MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER);
            var player = minecraftServer.getPlayerList().getPlayer(uuid);
            serverHandler(player);
        }
        context.setPacketHandled(true);
    }

}
