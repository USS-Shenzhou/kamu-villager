package cn.ussshenzhou.villager.mixin;

import cn.ussshenzhou.villager.GeneralForgeBusListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author USS_Shenzhou
 */
@Mixin(BowItem.class)
public class BowItemMixin {

    @Redirect(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V"))
    private void moreAccurate(AbstractArrow instance, Entity entity, float xRot, float yRot, float z, float v, float inaccuracy) {
        var player = (Player) entity;
        instance.shootFromRotation(player, player.getXRot(), player.getYRot(), z, v, ItemStack.isSameItemSameTags(player.getMainHandItem(), GeneralForgeBusListener.MAO_BOW) ? inaccuracy * 0.2f : inaccuracy);
    }

    @Redirect(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getItemEnchantmentLevel(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/item/ItemStack;)I",ordinal = 1))
    private int hiddenPower(Enchantment pEnchantment, ItemStack pStack) {
        if (ItemStack.isSameItemSameTags(pStack, GeneralForgeBusListener.MAO_BOW)){
            return 3;
        }
        //noinspection deprecation
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, pStack);
    }
}
