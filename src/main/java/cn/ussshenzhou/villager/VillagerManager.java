package cn.ussshenzhou.villager;

import cn.ussshenzhou.t88.task.TaskHelper;
import cn.ussshenzhou.villager.entity.Command;
import cn.ussshenzhou.villager.entity.VillagerFollower;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VillagerManager {

    private static final HashMap<UUID, LinkedHashSet<VillagerFollower>> PLAYER_AND_VILLAGERS = new HashMap<>();

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            PLAYER_AND_VILLAGERS.values().forEach(villagerFollowers -> villagerFollowers.removeIf(villagerFollower -> ((LivingEntity) villagerFollower).isRemoved()));
        }
    }

    public static void follow(@Nonnull Player master, VillagerFollower villager) {
        PLAYER_AND_VILLAGERS.compute(master.getUUID(), (player, set) -> Objects.requireNonNullElseGet(set, LinkedHashSet::new)).add(villager);
    }

    public static void follow(UUID master, VillagerFollower villager) {
        PLAYER_AND_VILLAGERS.compute(master, (player, set) -> Objects.requireNonNullElseGet(set, LinkedHashSet::new)).add(villager);
    }

    public static void command(Player master, Command command) {
        PLAYER_AND_VILLAGERS.compute(master.getUUID(), (player, set) -> Objects.requireNonNullElseGet(set, LinkedHashSet::new)).forEach(v -> v.setCommand(command));
    }

    @SubscribeEvent
    public static void changeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        var player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }
        TaskHelper.addServerTask(() -> {
            var needTp = PLAYER_AND_VILLAGERS.compute(player.getUUID(), (p, set) -> Objects.requireNonNullElseGet(set, LinkedHashSet::new))
                    .stream().filter(villagerFollower -> villagerFollower.getThis().level().dimension() != event.getTo()).toList();
            needTp.forEach(villager -> {
                        var v = villager.getThis();
                        v.teleportTo((ServerLevel) player.level(), player.getX(), player.getY(), player.getZ(), Set.of(), v.getYRot(), v.getXRot());
                    }
            );
            needTp.forEach(PLAYER_AND_VILLAGERS.get(player.getUUID())::remove);

        }, 15 * 20);
    }

}
