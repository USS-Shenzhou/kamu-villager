package cn.ussshenzhou.villager;

import cn.ussshenzhou.villager.entity.VillagerVillager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("MapOrSetKeyShouldOverrideHashCodeEquals")
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VillagerManager {

    private static final HashMap<UUID, LinkedHashSet<VillagerVillager>> PLAYER_AND_VILLAGERS = new HashMap<>();

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            PLAYER_AND_VILLAGERS.values().forEach(villagerVillagers -> villagerVillagers.removeIf(VillagerVillager::isRemoved));
        }
    }

    public static void follow(@Nonnull Player master, VillagerVillager villager) {
        PLAYER_AND_VILLAGERS.computeIfAbsent(master.getUUID(), player -> new LinkedHashSet<>()).add(villager);
    }

    public static void follow(UUID master, VillagerVillager villager) {
        PLAYER_AND_VILLAGERS.computeIfAbsent(master, player -> new LinkedHashSet<>()).add(villager);
    }

    public static void command(Player master, VillagerVillager.Command command) {
        PLAYER_AND_VILLAGERS.computeIfAbsent(master.getUUID(), player -> new LinkedHashSet<>()).forEach(villager -> villager.setCommand(command));
    }

    @SubscribeEvent
    public static void changeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        var player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }
        MinecraftServer minecraftServer = (MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER);
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(15 * 1000);
                minecraftServer.execute(() -> {
                    var set = PLAYER_AND_VILLAGERS.get(player.getUUID());
                    if (set!=null){
                        set.forEach(villager -> villager.teleportTo((ServerLevel) player.level(), player.getX(), player.getY(), player.getZ(), Set.of(), villager.getYRot(), villager.getXRot()));
                    }
                });
            } catch (InterruptedException ignored) {
            }
        });
    }

}
