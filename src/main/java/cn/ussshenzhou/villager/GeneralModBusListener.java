package cn.ussshenzhou.villager;

import cn.ussshenzhou.villager.entity.ModEntityTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
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
        event.put(ModEntityTypes.VILLAGER_VILLAGER.get(), Villager.createAttributes()
                .add(Attributes.ATTACK_DAMAGE, 1)
                .build()
        );
        event.put(ModEntityTypes.FALSE_PLAYER.get(), Player.createAttributes().build());
    }
}
