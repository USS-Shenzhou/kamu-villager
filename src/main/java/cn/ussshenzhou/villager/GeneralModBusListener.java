package cn.ussshenzhou.villager;

import cn.ussshenzhou.villager.entity.FalseFalsePlayer;
import cn.ussshenzhou.villager.entity.ModEntityTypes;
import cn.ussshenzhou.villager.entity.fakeplayer.FalsePlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.SpawnPlacementRegisterEvent;

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
        event.put(ModEntityTypes.FALSE_FALSE_PLAYER.get(), Zombie.createAttributes().build());
        //event.put(ModEntityTypes.VILLAGER_IRON_GOLEM.get(), IronGolem.createAttributes().build());
    }

    @SubscribeEvent
    public static void addSpawn(SpawnPlacementRegisterEvent event) {
        event.register(ModEntityTypes.FALSE_FALSE_PLAYER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, FalseFalsePlayer::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
    }
}
