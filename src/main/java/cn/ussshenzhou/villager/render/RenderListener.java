package cn.ussshenzhou.villager.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RenderListener {

    private static VillagerPlayerRenderer villagerPlayerRenderer = null;

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (villagerPlayerRenderer == null) {
            villagerPlayerRenderer = (VillagerPlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().renderers.get(event.getEntity().getType());
        }
        event.setCanceled(true);
        var player = event.getEntity();
        villagerPlayerRenderer.render(player,
                Mth.lerp(event.getPartialTick(), player.yRotO, player.getYRot()),
                event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight()
        );
    }
}
