package cn.ussshenzhou.villager.render;

import cn.ussshenzhou.villager.entity.ModEntityTypes;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class GeneralModBusListener {

    @SubscribeEvent
    public static void addAttribute(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.VILLAGER_VILLAGER.get(), Villager.createAttributes().build());
    }
}
