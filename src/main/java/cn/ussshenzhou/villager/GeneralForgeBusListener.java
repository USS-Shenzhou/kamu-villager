package cn.ussshenzhou.villager;

import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.t88.task.Task;
import cn.ussshenzhou.t88.task.TaskHelper;
import cn.ussshenzhou.t88.util.InventoryHelper;
import cn.ussshenzhou.villager.entity.FalseFalsePlayer;
import cn.ussshenzhou.villager.entity.ModEntityTypes;
import cn.ussshenzhou.villager.entity.VillagerVillager;
import cn.ussshenzhou.villager.entity.fakeplayer.FalsePlayer;
import cn.ussshenzhou.villager.entity.fakeplayer.events.BotFallDamageEvent;
import cn.ussshenzhou.villager.entity.fakeplayer.utils.BlockFace;
import cn.ussshenzhou.villager.entity.fakeplayer.utils.LegacyMats;
import cn.ussshenzhou.villager.item.ModItems;
import cn.ussshenzhou.villager.network.ChooseProfessionPacket;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSleepInBedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.entity.player.SleepingTimeCheckEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GeneralForgeBusListener {

    @SubscribeEvent
    public static void onPlayerCreate(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().hasData(ModDataAttachments.PROFESSION)) {
            event.getEntity().setData(ModDataAttachments.PROFESSION, Profession.NITWIT);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        var player = event.getEntity();
        if (!player.level().isClientSide) {
            NetworkHelper.sendToPlayer((ServerPlayer) player, new ChooseProfessionPacket(player, player.getData(ModDataAttachments.PROFESSION).get()));
        }
    }

    private static final MobSpawnSettings.SpawnerData PILLAGER = new MobSpawnSettings.SpawnerData(EntityType.PILLAGER, 40, 1, 3);
    private static final MobSpawnSettings.SpawnerData VINDICATOR = new MobSpawnSettings.SpawnerData(EntityType.VINDICATOR, 70, 1, 3);
    private static MobSpawnSettings.SpawnerData FALSE_FALSE_PLAYER = null;

    @SubscribeEvent
    public static void addSpawnEntity(LevelEvent.PotentialSpawns event) {
        if (event.getMobCategory() != MobCategory.MONSTER) {
            return;
        }
        event.addSpawnerData(PILLAGER);
        event.addSpawnerData(VINDICATOR);
        if (FALSE_FALSE_PLAYER == null) {
            FALSE_FALSE_PLAYER = new MobSpawnSettings.SpawnerData(ModEntityTypes.FALSE_FALSE_PLAYER.get(), 60, 1, 1);
        }
        event.addSpawnerData(FALSE_FALSE_PLAYER);
    }

    @SubscribeEvent
    public static void replaceSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getEntity() instanceof FalseFalsePlayer proxy) {
            event.setSpawnCancelled(true);
            var real = FalsePlayer.create((ServerLevel) proxy.level(), event.getX(), event.getY(), event.getZ());
            event.getLevel().addFreshEntity(real);
        } else if (event.getEntity() instanceof Villager villager) {
            event.setSpawnCancelled(true);
            var villagerVillager = new VillagerVillager(ModEntityTypes.VILLAGER_VILLAGER.get(), event.getLevel().getLevel(), villager.getVillagerData().getType());
            villagerVillager.setPos(event.getX(), event.getY(), event.getZ());
            event.getLevel().addFreshEntity(villagerVillager);
        }
    }

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        var playerA = event.getEntity();
        var level = playerA.level();
        if (level.isClientSide) {
            return;
        }
        var playerB = level.getNearbyPlayers(TargetingConditions.DEFAULT, playerA, playerA.getBoundingBox().inflate(1.9))
                .stream()
                .filter(player -> player != playerA)
                .findFirst()
                .orElse(null);
        if (playerB == null || !playerB.isSleeping()) {
            return;
        }
        playerB.stopSleeping();
        var pos = playerB.getPosition(1);
        var villager = new Villager(EntityType.VILLAGER, level);
        villager.finalizeSpawn((ServerLevelAccessor) level, level.getCurrentDifficultyAt(playerB.blockPosition()), MobSpawnType.BREEDING, null, null);
        villager.setPos(pos);
        level.addFreshEntity(villager);
    }

    @SubscribeEvent
    public static void sleepAnyTime(SleepingTimeCheckEvent event) {
        event.setResult(Event.Result.ALLOW);
    }

    @SubscribeEvent
    public static void sendFalsePlayer(PlayerEvent.PlayerLoggedInEvent event) {
        var player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.level().getEntitiesOfClass(FalsePlayer.class, new AABB(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE)).forEach(
                    falsePlayer -> falsePlayer.renderBot(serverPlayer.connection, true)
            );
        }
    }

    @SubscribeEvent
    public static void onFallDamage(BotFallDamageEvent event) {
        FalsePlayer bot = event.getFalsePlayer();
        Level level = bot.level();

        bot.look(BlockFace.DOWN);

        Item itemType;
        Block placeType;
        SoundEvent sound;
        BlockPos groundLoc = null;
        BlockState ground = null;
        boolean nether = level.dimension() == Level.NETHER;
        double yPos = bot.position().y;

        if (nether) {
            itemType = Items.TWISTING_VINES;
            sound = SoundEvents.WEEPING_VINES_PLACE;
            placeType = Blocks.TWISTING_VINES;

            for (BlockPos pos : event.getStandingOn()) {
                if (LegacyMats.canPlaceTwistingVines(level.getBlockState(pos))) {
                    groundLoc = pos;
                    ground = level.getBlockState(pos);
                    break;
                }
            }
        } else {
            itemType = Items.WATER_BUCKET;
            sound = SoundEvents.BUCKET_EMPTY;
            placeType = Blocks.WATER;

            for (BlockPos pos : event.getStandingOn()) {
                if (LegacyMats.canPlaceWater(pos, level.getBlockState(pos), Optional.of(yPos))) {
                    groundLoc = pos;
                    ground = level.getBlockState(pos);
                    break;
                }
            }
        }

        if (groundLoc == null) {
            return;
        }

        var loc = !LegacyMats.shouldReplace(groundLoc, ground, yPos, nether) ? groundLoc.offset(0, 1, 0) : groundLoc;
        var locState = level.getBlockState(loc);
        boolean waterloggable = !nether && locState.hasProperty(BlockStateProperties.WATERLOGGED);
        boolean waterlogged = waterloggable && locState.getValue(BlockStateProperties.WATERLOGGED);

        event.setCanceled(true);

        if (locState.getBlock() != placeType && !waterlogged) {
            bot.punch();
            if (waterloggable) {
                locState.setValue(BlockStateProperties.WATERLOGGED, true);
                level.setBlocksDirty(loc, locState, locState);
            } else {
                level.setBlock(loc, placeType.defaultBlockState(), 2);
            }
            level.playSound(null, loc, sound, SoundSource.PLAYERS, 1, 1);

            if (itemType == Items.WATER_BUCKET) {
                bot.setItem(new ItemStack(Items.BUCKET));

                TaskHelper.addServerTask(() -> {
                    BlockState blockState = level.getBlockState(loc);

                    boolean waterloggedNow = !nether && blockState.hasProperty(BlockStateProperties.WATERLOGGED)
                            && blockState.getValue(BlockStateProperties.WATERLOGGED);
                    if (blockState.getBlock() == Blocks.WATER || waterloggedNow) {
                        bot.look(BlockFace.DOWN);
                        bot.setItem(new ItemStack(Items.WATER_BUCKET));
                        level.playSound(null, loc, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1, 1);
                        if (waterloggedNow) {
                            locState.setValue(BlockStateProperties.WATERLOGGED, false);
                            level.setBlocksDirty(loc, locState, locState);
                        } else {
                            level.setBlock(loc, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                }, 5);
            }
        }
    }

    @SubscribeEvent
    public static void cancelXp(LivingExperienceDropEvent event) {
        if (event.getAttackingPlayer() instanceof FalsePlayer) {
            event.setCanceled(true);
        }
    }

    //-----Eastern Eggs-----

    public static ItemStack LAVA_BOTTLE = null;

    @SubscribeEvent
    public static void justInit(PlayerEvent.PlayerLoggedInEvent event) {
        if (LAVA_BOTTLE == null) {
            LAVA_BOTTLE = new ItemStack(ModItems.LAVA_BOTTLE.get());
            LAVA_BOTTLE.getOrCreateTag().putInt("CustomPotionColor", 0x00ff9900);
            var list = new ListTag();
            var c = new CompoundTag();
            c.putBoolean("ambient", false);
            c.putByte("amplifier", (byte) 2);
            c.putInt("duration", 400);
            c.putString("id", "minecraft:regeneration");
            c.putBoolean("show_icon", true);
            c.putBoolean("show_particles", true);
            list.add(c);
            var f = new CompoundTag();
            f.putBoolean("ambient", false);
            f.putByte("amplifier", (byte) 0);
            f.putInt("duration", 800);
            f.putString("id", "minecraft:fire_resistance");
            f.putBoolean("show_icon", true);
            f.putBoolean("show_particles", true);
            list.add(f);
            LAVA_BOTTLE.setHoverName(Component.literal("一小瓶热腾腾的饮料"));
            LAVA_BOTTLE.getOrCreateTag().put("custom_potion_effects", list);
        }
    }

    public static final ItemStack CEN_AXE = new ItemStack(Items.DIAMOND_AXE);

    static {
        CEN_AXE.enchant(Enchantments.SHARPNESS, 6);
        CEN_AXE.getOrCreateTag().putBoolean("Unbreakable", true);
        var l = new ListTag();
        l.add(StringTag.valueOf("不可丢出"));
        var c = new CompoundTag();
        c.put("Lore", l);
        CEN_AXE.getOrCreateTag()
                .put("display", c);
    }

    public static final ItemStack MAO_SHOVEL = new ItemStack(Items.DIAMOND_SHOVEL);

    static {
        MAO_SHOVEL.enchant(Enchantments.BLOCK_FORTUNE, 4);
        MAO_SHOVEL.enchant(Enchantments.BLOCK_EFFICIENCY, 6);
        MAO_SHOVEL.getOrCreateTag().putBoolean("Unbreakable", true);
        var l = new ListTag();
        l.add(StringTag.valueOf("不可丢出"));
        var c = new CompoundTag();
        c.put("Lore", l);
        MAO_SHOVEL.getOrCreateTag()
                .put("display", c);
        MAO_SHOVEL.setHoverName(Component.literal("钻石铲子"));
    }

    public static final ItemStack MAO_BOW = new ItemStack(Items.BOW);

    static {
        MAO_BOW.enchant(Enchantments.SOUL_SPEED, 1);
        MAO_BOW.getOrCreateTag().putBoolean("Unbreakable", true);
        var l = new ListTag();
        l.add(StringTag.valueOf("在主手时：射击扩散-80%"));
        l.add(StringTag.valueOf("不可丢出"));
        var c = new CompoundTag();
        c.put("Lore", l);
        MAO_BOW.getOrCreateTag()
                .put("display", c);
    }

    public static final ItemStack MELOR_SWORD = new ItemStack(Items.DIAMOND_SWORD);

    static {
        MELOR_SWORD.enchant(Enchantments.MOB_LOOTING, 4);
        MELOR_SWORD.enchant(Enchantments.SHARPNESS, 6);
        MELOR_SWORD.getOrCreateTag().putBoolean("Unbreakable", true);
        var l = new ListTag();
        l.add(StringTag.valueOf("不可丢出"));
        var c = new CompoundTag();
        c.put("Lore", l);
        MELOR_SWORD.getOrCreateTag()
                .put("display", c);
    }

    public static final List<ItemStack> UNDROPPABLE = List.of(CEN_AXE, MAO_SHOVEL, MAO_BOW, MELOR_SWORD);

    @SubscribeEvent
    public static void unDroppableItem(ItemTossEvent event) {
        if (undroppable(event.getEntity().getItem())) {
            event.setCanceled(true);
            event.getPlayer().addItem(event.getEntity().getItem());
            event.getPlayer().getInventory().setChanged();
        }
    }

    private static boolean undroppable(ItemStack itemStack) {
        for (ItemStack i : UNDROPPABLE) {
            if (ItemStack.isSameItemSameTags(i, itemStack)) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerDeath(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            var old = event.getOriginal();
            var nev = event.getEntity();
            if (!nev.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                InventoryHelper.getAllAsStream(old.getInventory())
                        .filter(itemStack -> UNDROPPABLE.stream().anyMatch(undroppable -> ItemStack.isSameItemSameTags(undroppable, itemStack)))
                        .forEach(nev::addItem);
            }
        }
    }
}
