package cn.ussshenzhou.villager.render;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModBusRenderListener {

    @SubscribeEvent
    public static void regLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(VillagerArmorLayer.LAYER_LOCATION, VillagerArmorModel::createBodyLayer);
    }
}
