package cn.ussshenzhou.villager.mixin;

import cn.ussshenzhou.villager.render.VillagerArmorLayer;
import cn.ussshenzhou.villager.render.VillagerArmorModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author USS_Shenzhou
 */
@Mixin(VillagerRenderer.class)
public class VillagerRendererMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addLayer(EntityRendererProvider.Context context, CallbackInfo ci) {
        var thiz = (VillagerRenderer) (Object) this;
        thiz.addLayer(new VillagerArmorLayer<>(thiz, new VillagerArmorModel<>(context.bakeLayer(VillagerArmorLayer.LAYER_LOCATION))));
    }
}
