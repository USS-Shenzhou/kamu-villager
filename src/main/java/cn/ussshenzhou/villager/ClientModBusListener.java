package cn.ussshenzhou.villager;

import cn.ussshenzhou.villager.entity.ModEntityTypes;
import cn.ussshenzhou.villager.render.VillagerPlayerRenderer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModBusListener {

    @SubscribeEvent
    public static void entityRendererRegistry(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityType.PLAYER, VillagerPlayerRenderer::new);
        //event.registerEntityRenderer(ModEntityTypes.);
    }
}
