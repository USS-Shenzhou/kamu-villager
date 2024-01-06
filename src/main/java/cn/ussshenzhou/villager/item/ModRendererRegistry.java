package cn.ussshenzhou.villager.item;

import cn.ussshenzhou.villager.entity.ModEntityTypes;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
public class ModRendererRegistry {

    @SubscribeEvent
    public static void registryRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.VILLAGER_VILLAGER.get(), VillagerRenderer::new);
    }
}
