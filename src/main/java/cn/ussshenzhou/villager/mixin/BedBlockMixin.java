package cn.ussshenzhou.villager.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author USS_Shenzhou
 */
@Mixin(BedBlock.class)
public class BedBlockMixin {

    @Inject(method = "canSetSpawn", at = @At("RETURN"), cancellable = true)
    private static void wontExplodeAnyMore(Level pLevel, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

}
