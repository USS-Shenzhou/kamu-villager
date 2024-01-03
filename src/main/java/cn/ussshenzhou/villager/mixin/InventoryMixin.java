package cn.ussshenzhou.villager.mixin;

import cn.ussshenzhou.villager.GeneralForgeBusListener;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author USS_Shenzhou
 */
@Mixin(Inventory.class)
public class InventoryMixin {

    @ModifyExpressionValue(method = "dropAll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    private boolean checkUndroppable(boolean original, @Local ItemStack drop) {
        for (ItemStack i : GeneralForgeBusListener.UNDROPPABLE) {
            if (ItemStack.isSameItemSameTags(i, drop)) {
                //noinspection PointlessBooleanExpression
                return !false;
            }
        }
        return original;
    }

}
