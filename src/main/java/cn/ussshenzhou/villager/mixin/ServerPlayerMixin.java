package cn.ussshenzhou.villager.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * @author USS_Shenzhou
 */
@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @ModifyExpressionValue(method = "startSleepInBed",at = @At(value = "INVOKE",target = "Lnet/minecraft/world/level/dimension/DimensionType;natural()Z"))
    private boolean canSleepAnywhere(boolean original){
        return true;
    }
}
