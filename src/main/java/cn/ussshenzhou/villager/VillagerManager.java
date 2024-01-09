package cn.ussshenzhou.villager;

import cn.ussshenzhou.villager.entity.VillagerVillager;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VillagerManager {

    private static final HashMap<Player, LinkedList<VillagerVillager>> PLAYER_AND_VILLAGERS = new HashMap<>();

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            PLAYER_AND_VILLAGERS.values().forEach(villagerVillagers -> villagerVillagers.removeIf(VillagerVillager::isRemoved));
        }
    }

    public static void follow(Player master, VillagerVillager villager) {
        PLAYER_AND_VILLAGERS.computeIfAbsent(master, player -> new LinkedList<>()).add(villager);
    }

}
