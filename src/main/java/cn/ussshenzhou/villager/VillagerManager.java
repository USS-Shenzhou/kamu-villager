package cn.ussshenzhou.villager;

import cn.ussshenzhou.villager.entity.VillagerVillager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VillagerManager {

    private static final HashMap<UUID, LinkedList<VillagerVillager>> PLAYER_AND_VILLAGERS = new HashMap<>();

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            PLAYER_AND_VILLAGERS.values().forEach(villagerVillagers -> villagerVillagers.removeIf(VillagerVillager::isRemoved));
        }
    }

    public static void follow(Player master, VillagerVillager villager) {
        PLAYER_AND_VILLAGERS.computeIfAbsent(master.getUUID(), player -> new LinkedList<>()).add(villager);
    }

    public static void command(Player master, VillagerVillager.Command command) {
        PLAYER_AND_VILLAGERS.get(master.getUUID()).forEach(villager -> villager.setCommand(command));
    }

    @SubscribeEvent
    public static void changeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        var player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }
        PLAYER_AND_VILLAGERS.get(player.getUUID()).forEach(villager -> villager.teleportTo((ServerLevel) player.level(), player.getX(), player.getY(), player.getZ(), Set.of(), villager.getYRot(), villager.getXRot()));
    }

}
