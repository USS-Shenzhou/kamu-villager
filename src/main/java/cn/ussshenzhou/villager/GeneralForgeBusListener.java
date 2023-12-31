package cn.ussshenzhou.villager;

import net.minecraft.client.gui.screens.social.PlayerEntry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GeneralForgeBusListener {

    @SubscribeEvent
    public static void onPlayerDeath(PlayerEvent.Clone event) {
        /*if (event.isWasDeath() && event.getOriginal().hasData(ModDataAttachments.PROFESSION)){
            event.getEntity().setData(ModDataAttachments.PROFESSION).
        }*/
    }

    @SubscribeEvent
    public static void onPlayerCreate(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().hasData(ModDataAttachments.PROFESSION)) {
            event.getEntity().setData(ModDataAttachments.PROFESSION, Profession.NITWIT);
        }
    }
}
