package cn.ussshenzhou.villager.entity.fakeplayer.utils;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.*;

public class LegacyMats {

    public static final Set<Block> AIR = new HashSet<>(Arrays.asList(
            Blocks.WATER,
            Blocks.FIRE,
            Blocks.LAVA,
            Blocks.SNOW,
            Blocks.CAVE_AIR,
            Blocks.VINE,
            Blocks.FERN,
            Blocks.LARGE_FERN,
            Blocks.SHORT_GRASS,
            Blocks.TALL_GRASS,
            Blocks.SEAGRASS,
            Blocks.TALL_SEAGRASS,
            Blocks.KELP,
            Blocks.KELP_PLANT,
            Blocks.SUNFLOWER,
            Blocks.AIR,
            Blocks.VOID_AIR,
            Blocks.FIRE,
            Blocks.SOUL_FIRE
    ));

    public static final Set<Block> NO_CRACK = new HashSet<>(Arrays.asList(
            Blocks.WATER,
            Blocks.FIRE,
            Blocks.LAVA,
            Blocks.CAVE_AIR,
            Blocks.VOID_AIR,
            Blocks.AIR,
            Blocks.SOUL_FIRE
    ));

    public static final Set<Block> SHOVEL = new HashSet<>(Arrays.asList(
            Blocks.CLAY,
            Blocks.DIRT,
            Blocks.GRASS_BLOCK,
            Blocks.COARSE_DIRT,
            Blocks.PODZOL,
            Blocks.MYCELIUM,
            Blocks.GRAVEL,
            Blocks.MUD,
            Blocks.MUDDY_MANGROVE_ROOTS,
            Blocks.SAND,
            Blocks.RED_SAND,
            Blocks.SOUL_SAND,
            Blocks.SOUL_SOIL,
            Blocks.SNOW,
            Blocks.SNOW_BLOCK
    ));

    public static final Set<Block> AXE = new HashSet<>(Arrays.asList(
            Blocks.OAK_PLANKS, Blocks.OAK_DOOR, Blocks.OAK_FENCE, Blocks.OAK_FENCE_GATE, Blocks.OAK_LOG,
            Blocks.OAK_SIGN, Blocks.OAK_SLAB, Blocks.OAK_STAIRS, Blocks.OAK_TRAPDOOR, Blocks.OAK_WALL_SIGN, Blocks.OAK_WOOD,
            Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_DOOR, Blocks.DARK_OAK_FENCE, Blocks.DARK_OAK_FENCE_GATE, Blocks.DARK_OAK_LOG,
            Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_TRAPDOOR, Blocks.DARK_OAK_WALL_SIGN, Blocks.DARK_OAK_WOOD,
            Blocks.ACACIA_PLANKS, Blocks.ACACIA_DOOR, Blocks.ACACIA_FENCE, Blocks.ACACIA_FENCE_GATE, Blocks.ACACIA_LOG,
            Blocks.ACACIA_SIGN, Blocks.ACACIA_SLAB, Blocks.ACACIA_STAIRS, Blocks.ACACIA_TRAPDOOR, Blocks.ACACIA_WALL_SIGN, Blocks.ACACIA_WOOD,
            Blocks.BIRCH_PLANKS, Blocks.BIRCH_DOOR, Blocks.BIRCH_FENCE, Blocks.BIRCH_FENCE_GATE, Blocks.BIRCH_LOG,
            Blocks.BIRCH_SIGN, Blocks.BIRCH_SLAB, Blocks.BIRCH_STAIRS, Blocks.BIRCH_TRAPDOOR, Blocks.BIRCH_WALL_SIGN, Blocks.BIRCH_WOOD,
            Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_DOOR, Blocks.JUNGLE_FENCE, Blocks.JUNGLE_FENCE_GATE, Blocks.JUNGLE_LOG,
            Blocks.JUNGLE_SIGN, Blocks.JUNGLE_SLAB, Blocks.JUNGLE_STAIRS, Blocks.JUNGLE_TRAPDOOR, Blocks.JUNGLE_WALL_SIGN, Blocks.JUNGLE_WOOD,
            Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_DOOR, Blocks.SPRUCE_FENCE, Blocks.SPRUCE_FENCE_GATE, Blocks.SPRUCE_LOG,
            Blocks.SPRUCE_SIGN, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_TRAPDOOR, Blocks.SPRUCE_WALL_SIGN, Blocks.SPRUCE_WOOD,
            Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_DOOR, Blocks.MANGROVE_FENCE, Blocks.MANGROVE_FENCE_GATE, Blocks.MANGROVE_LOG,
            Blocks.MANGROVE_SIGN, Blocks.MANGROVE_SLAB, Blocks.MANGROVE_STAIRS, Blocks.MANGROVE_TRAPDOOR, Blocks.MANGROVE_WALL_SIGN, Blocks.MANGROVE_WOOD,
            Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_DOOR, Blocks.CRIMSON_FENCE, Blocks.CRIMSON_FENCE_GATE, Blocks.CRIMSON_STEM,
            Blocks.CRIMSON_SIGN, Blocks.CRIMSON_SLAB, Blocks.CRIMSON_STAIRS, Blocks.CRIMSON_TRAPDOOR, Blocks.CRIMSON_WALL_SIGN,
            Blocks.WARPED_PLANKS, Blocks.WARPED_DOOR, Blocks.WARPED_FENCE, Blocks.WARPED_FENCE_GATE, Blocks.WARPED_STEM,
            Blocks.WARPED_SIGN, Blocks.WARPED_SLAB, Blocks.WARPED_STAIRS, Blocks.WARPED_TRAPDOOR, Blocks.WARPED_WALL_SIGN,
            Blocks.CHEST, Blocks.TRAPPED_CHEST
    ));

    public static final Set<Block> BREAK = new HashSet<>(Arrays.asList(
            Blocks.AIR,
            Blocks.WATER,
            Blocks.LAVA,
            Blocks.TALL_GRASS,
            Blocks.CAVE_AIR,
            Blocks.VINE,
            Blocks.FERN,
            Blocks.LARGE_FERN,
            Blocks.SUGAR_CANE,
            Blocks.TWISTING_VINES,
            Blocks.TWISTING_VINES_PLANT,
            Blocks.WEEPING_VINES,
            Blocks.SEAGRASS,
            Blocks.TALL_SEAGRASS,
            Blocks.KELP,
            Blocks.KELP_PLANT,
            Blocks.SUNFLOWER,
            Blocks.FIRE,
            Blocks.SOUL_FIRE
    ));

    public static final Set<Block> WATER = new HashSet<>(Arrays.asList(
            Blocks.WATER,
            Blocks.SEAGRASS,
            Blocks.TALL_SEAGRASS,
            Blocks.KELP,
            Blocks.KELP_PLANT
    ));

    public static final Set<Block> SPAWN = new HashSet<>(Arrays.asList(
            Blocks.AIR,
            Blocks.TALL_GRASS,
            Blocks.SNOW,
            Blocks.CAVE_AIR,
            Blocks.VINE,
            Blocks.FERN,
            Blocks.LARGE_FERN,
            Blocks.SUGAR_CANE,
            Blocks.TWISTING_VINES,
            Blocks.WEEPING_VINES,
            Blocks.SEAGRASS,
            Blocks.TALL_SEAGRASS,
            Blocks.KELP,
            Blocks.KELP_PLANT,
            Blocks.SUNFLOWER,
            Blocks.FIRE,
            Blocks.SOUL_FIRE
    ));

    public static final Set<Block> FALL = new HashSet<>(Arrays.asList(
            Blocks.AIR,
            Blocks.TALL_GRASS,
            Blocks.SNOW,
            Blocks.CAVE_AIR,
            Blocks.VINE,
            Blocks.FERN,
            Blocks.LARGE_FERN,
            Blocks.SUGAR_CANE,
            Blocks.TWISTING_VINES,
            Blocks.WEEPING_VINES,
            Blocks.SEAGRASS,
            Blocks.TALL_SEAGRASS,
            Blocks.KELP,
            Blocks.KELP_PLANT,
            Blocks.SUNFLOWER,
            Blocks.WATER
    ));

    public static final Set<Block> FENCE = new HashSet<>(concatTypes(new ArrayList<>(),
            Arrays.asList(Blocks.GLASS_PANE, Blocks.IRON_BARS), Arrays.asList(FenceBlock.class, WallBlock.class)));

    public static final Set<Block> GATES = new HashSet<>(concatTypes(FenceGateBlock.class));

    public static final Set<Block> OBSTACLES = new HashSet<>(concatTypes(Lists.newArrayList(
            Blocks.IRON_BARS,
            Blocks.CHAIN,
            Blocks.END_ROD,
            Blocks.LIGHTNING_ROD,
            Blocks.COBWEB,
            Blocks.SWEET_BERRY_BUSH,
            Blocks.FLOWER_POT,
            Blocks.GLASS_PANE
    ), List.of(), List.of(StainedGlassPaneBlock.class)));

    //Notice: We exclude blocks that cannot exist without a solid block below (such as rails or crops)
    public static final Set<Block> NONSOLID = new HashSet<>(concatTypes(Lists.newArrayList(
            Blocks.COBWEB,
            Blocks.END_GATEWAY,
            Blocks.END_PORTAL,
            Blocks.NETHER_PORTAL,
            Blocks.CAVE_VINES_PLANT,
            Blocks.GLOW_LICHEN,
            Blocks.HANGING_ROOTS,
            Blocks.POWDER_SNOW,
            Blocks.SCULK_VEIN,
            Blocks.TRIPWIRE,
            Blocks.TRIPWIRE_HOOK,
            Blocks.LADDER,
            Blocks.VINE,
            Blocks.SOUL_WALL_TORCH,
            Blocks.REDSTONE_WALL_TORCH,
            Blocks.WALL_TORCH,
            Blocks.WEEPING_VINES_PLANT,
            Blocks.WEEPING_VINES,
            Blocks.CAVE_VINES_PLANT,
            Blocks.CAVE_VINES
    ), Arrays.asList(), Arrays.asList(ButtonBlock.class, CoralWallFanBlock.class, WallSignBlock.class), m -> m.getDescriptionId().toUpperCase().endsWith(".WALL_BANNER")));

    public static final Set<Block> LEAVES = new HashSet<>(concatTypes(LeavesBlock.class));

    public static final Set<Block> INSTANT_BREAK = new HashSet<>(concatTypes(Lists.newArrayList(
            Blocks.TALL_GRASS,
            Blocks.SHORT_GRASS,
            Blocks.FERN,
            Blocks.LARGE_FERN,
            Blocks.KELP_PLANT,
            Blocks.DEAD_BUSH,
            Blocks.WHEAT,
            Blocks.POTATOES,
            Blocks.CARROTS,
            Blocks.BEETROOTS,
            Blocks.PUMPKIN_STEM,
            Blocks.MELON_STEM,
            Blocks.SUGAR_CANE,
            Blocks.SWEET_BERRY_BUSH,
            Blocks.LILY_PAD,
            Blocks.DANDELION,
            Blocks.POPPY,
            Blocks.BLUE_ORCHID,
            Blocks.ALLIUM,
            Blocks.AZURE_BLUET,
            Blocks.RED_TULIP,
            Blocks.ORANGE_TULIP,
            Blocks.WHITE_TULIP,
            Blocks.PINK_TULIP,
            Blocks.OXEYE_DAISY,
            Blocks.CORNFLOWER,
            Blocks.LILY_OF_THE_VALLEY,
            Blocks.WITHER_ROSE,
            Blocks.SUNFLOWER,
            Blocks.LILAC,
            Blocks.ROSE_BUSH,
            Blocks.PEONY,
            Blocks.NETHER_WART,
            Blocks.FLOWER_POT,
            Blocks.AZALEA,
            Blocks.FLOWERING_AZALEA,
            Blocks.REPEATER,
            Blocks.COMPARATOR,
            Blocks.REDSTONE_WIRE,
            Blocks.REDSTONE_TORCH,
            Blocks.REDSTONE_WALL_TORCH,
            Blocks.TORCH,
            Blocks.WALL_TORCH,
            Blocks.SOUL_TORCH,
            Blocks.SOUL_WALL_TORCH,
            Blocks.SCAFFOLDING,
            Blocks.SLIME_BLOCK,
            Blocks.HONEY_BLOCK,
            Blocks.TNT,
            Blocks.TRIPWIRE,
            Blocks.TRIPWIRE_HOOK,
            Blocks.SPORE_BLOSSOM,
            Blocks.RED_MUSHROOM,
            Blocks.BROWN_MUSHROOM,
            Blocks.CRIMSON_FUNGUS,
            Blocks.WARPED_FUNGUS,
            Blocks.CRIMSON_ROOTS,
            Blocks.WARPED_ROOTS,
            Blocks.HANGING_ROOTS,
            Blocks.WEEPING_VINES,
            Blocks.WEEPING_VINES_PLANT,
            Blocks.TWISTING_VINES,
            Blocks.TWISTING_VINES_PLANT,
            Blocks.CAVE_VINES,
            Blocks.CAVE_VINES_PLANT,
            Blocks.SEA_PICKLE
    ), Arrays.asList(), Arrays.asList(SaplingBlock.class, CoralWallFanBlock.class), m -> m.getDescriptionId().toUpperCase().endsWith(".CORAL_FAN") || m.getDescriptionId().toUpperCase().endsWith(".CORAL")
            || m.getDescriptionId().toUpperCase().startsWith("POTTED.")));

    private static List<Block> concatTypes(Class<?>... types) {
        return concatTypes(new ArrayList<>(), Arrays.asList(types));
    }

    private static List<Block> concatTypes(List<Block> Blocks, List<Class<?>> types) {
        return concatTypes(Blocks, Arrays.asList(), types);
    }

    private static List<Block> concatTypes(List<Block> Blocks, List<Block> exclusions, List<Class<?>> types) {
        return concatTypes(Blocks, exclusions, types, m -> false);
    }

    private static List<Block> concatTypes(List<Block> Blocks, List<Block> exclusions, List<Class<?>> types, Predicate<Block> otherFilter) {
        Blocks.addAll(Blocks.stream()
                .filter(m -> {
                    if (!types.contains(m.getClass())) {
                        otherFilter.test(m);
                    }
                    return false;
                }).toList());
        return Blocks;
    }

    /**
     * Checks for non-solid blocks that can hold an entity up.
     */
    public static boolean canStandOn(Block mat) {
        if (mat == Blocks.END_ROD || mat == Blocks.FLOWER_POT || mat == Blocks.REPEATER || mat == Blocks.COMPARATOR
                || mat == Blocks.SNOW || mat == Blocks.LADDER || mat == Blocks.VINE || mat == Blocks.SCAFFOLDING
                || mat == Blocks.AZALEA || mat == Blocks.FLOWERING_AZALEA || mat == Blocks.BIG_DRIPLEAF
                || mat == Blocks.CHORUS_FLOWER || mat == Blocks.CHORUS_PLANT || mat == Blocks.COCOA
                || mat == Blocks.LILY_PAD || mat == Blocks.SEA_PICKLE) {
            return true;
        }

        if (mat.getDescriptionId().toUpperCase().endsWith(".CARPET")) {
            return true;
        }

        if (mat.getDescriptionId().toUpperCase().startsWith("POTTED.")) {
            return true;
        }

        if ((mat.getDescriptionId().toUpperCase().endsWith(".HEAD") || mat.getDescriptionId().toUpperCase().endsWith(".SKULL")) && !mat.getDescriptionId().toUpperCase().equals("PISTON_HEAD")) {
            return true;
        }

        if (mat.getClass() == CandleBlock.class) {
            return true;
        }
        return false;
    }

    public static boolean canPlaceWater(BlockPos pos, BlockState state, Optional<Double> entityYPos) {
        if (state.isSolid()) {
            if (state.getBlock() == Blocks.CHAIN && state.getValue(AXIS) == Direction.Axis.Y
                    && !state.getValue(WATERLOGGED)) {
                return false;
            }
            if ((state.getBlock().getClass() == LeavesBlock.class || state.getBlock() == Blocks.MANGROVE_ROOTS
                    || state.getBlock() == Blocks.IRON_BARS || state.getBlock().getDescriptionId().toUpperCase().endsWith("GLASS_PANE"))
                    && !state.getValue(WATERLOGGED)) {
                return false;
            }
            if (state.getBlock().getClass() == SlabBlock.class && state.getValue(HALF) == Half.TOP
                    && !state.getValue(WATERLOGGED)) {
                return false;
            }
            if (state.getBlock().getClass() == StairBlock.class && state.getValue(HALF) == Half.TOP
                    && !state.getValue(WATERLOGGED)) {
                return false;
            }
            if (state.getBlock().getClass() == StairBlock.class && state.getValue(HALF) == Half.BOTTOM
                    && !state.getValue(WATERLOGGED)
                    && (!entityYPos.isPresent() || (int) entityYPos.get().doubleValue() != pos.getY())) {
                return false;
            }
            if ((state.getBlock().getClass() == FenceBlock.class || state.getBlock().getClass() == WallBlock.class)
                    && !state.getValue(WATERLOGGED)) {
                return false;
            }
            if (state.getBlock() == Blocks.LIGHTNING_ROD
                    && (!state.getValue(WATERLOGGED) && state.getValue(FACING) == Direction.UP || state.getValue(FACING) == Direction.DOWN)
            ) {
                return false;
            }
            if (state.getBlock().getClass() == TrapDoorBlock.class
                    && (state.getValue(HALF) == Half.TOP || state.getValue(HALF) == Half.BOTTOM && state.getValue(OPEN) && !state.getValue(WATERLOGGED))
            ) {
                return false;
            }
            return true;
        } else {
            if (state.getBlock().getDescriptionId().toUpperCase().endsWith(".CARPET")) {
                return true;
            }
            if (state.getBlock().getClass() == CandleBlock.class) {
                return true;
            }
            if (state.getBlock().getDescriptionId().toUpperCase().startsWith("POTTED.")) {
                return true;
            }
            if ((state.getBlock().getDescriptionId().toUpperCase().endsWith(".HEAD") || state.getBlock().getDescriptionId().toUpperCase().endsWith(".SKULL"))
                    && !state.getBlock().getDescriptionId().toUpperCase().equals("PISTON_HEAD")) {
                return true;
            }
            Block block = state.getBlock();
            if (block.equals(Blocks.SNOW)
                    || block.equals(Blocks.AZALEA)
                    || block.equals(Blocks.FLOWERING_AZALEA)
                    || block.equals(Blocks.CHORUS_FLOWER)
                    || block.equals(Blocks.CHORUS_PLANT)
                    || block.equals(Blocks.COCOA)
                    || block.equals(Blocks.LILY_PAD)
                    || block.equals(Blocks.SEA_PICKLE)
                    || block.equals(Blocks.END_ROD)
                    || block.equals(Blocks.FLOWER_POT)
                    || block.equals(Blocks.SCAFFOLDING)
                    || block.equals(Blocks.COMPARATOR)
                    || block.equals(Blocks.REPEATER)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canPlaceTwistingVines(BlockState state) {
        if (state.isSolid()) {
            if (state.getBlock().getClass() == LeavesBlock.class) {
                return false;
            }
            if (state.getBlock().getDescriptionId().toUpperCase().endsWith(".CORAL_FAN") || state.getBlock().getDescriptionId().toUpperCase().endsWith(".CORAL")
                    || state.getBlock().getDescriptionId().toUpperCase().endsWith(".CORAL_WALL_FAN")) {
                return false;
            }
            if (state.getBlock().getDescriptionId().toUpperCase().endsWith("GLASS_PANE")) {
                return false;
            }
            if (state.getBlock().getClass() == SlabBlock.class && state.getValue(HALF) == Half.BOTTOM) {
                return false;
            }
            if (state.getBlock().getClass() == StairBlock.class && state.getValue(HALF) == Half.BOTTOM) {
                return false;
            }
            if (state.getBlock().getClass() == FenceBlock.class || state.getBlock().getClass() == WallBlock.class) {
                return false;
            }
            if (state.getBlock().getDescriptionId().toUpperCase().endsWith(".BANNER")) {
                return false;
            }
            if (state.getBlock().getDescriptionId().toUpperCase().endsWith(".WALL_BANNER")) {
                return false;
            }
            if (state.getBlock().getClass() == BedBlock.class) {
                return false;
            }
            if (state.getBlock().getDescriptionId().toUpperCase().endsWith("CANDLE_CAKE")) {
                return false;
            }
            if (state.getBlock().getClass() == DoorBlock.class) {
                return false;
            }
            if (state.getBlock().getClass() == FenceGateBlock.class) {
                return false;
            }
            if (state.getBlock() == Blocks.PISTON_HEAD && state.getValue(FACING) != Direction.UP) {
                return false;
            }
            if (state.getBlock().getClass() == PistonBaseBlock.class && state.getValue(FACING) != Direction.DOWN
                    && (state.hasProperty(EXTENDED) && state.getValue(EXTENDED))) {
                return false;
            }
            if (state.getBlock().getClass() == TrapDoorBlock.class && state.getValue(HALF) == Half.BOTTOM
                    || (state.hasProperty(OPEN) && state.getValue(OPEN))) {
                return false;
            }
            Block block = state.getBlock();
            if (block.equals(Blocks.POINTED_DRIPSTONE) || block.equals(Blocks.SMALL_AMETHYST_BUD) || block.equals(Blocks.MEDIUM_AMETHYST_BUD) || block.equals(Blocks.LARGE_AMETHYST_BUD) || block.equals(Blocks.AMETHYST_CLUSTER) || block.equals(Blocks.BAMBOO) || block.equals(Blocks.CACTUS) || block.equals(Blocks.DRAGON_EGG) || block.equals(Blocks.TURTLE_EGG) || block.equals(Blocks.CHAIN) || block.equals(Blocks.IRON_BARS) || block.equals(Blocks.LANTERN) || block.equals(Blocks.SOUL_LANTERN) || block.equals(Blocks.ANVIL) || block.equals(Blocks.BREWING_STAND) || block.equals(Blocks.CHEST) || block.equals(Blocks.ENDER_CHEST) || block.equals(Blocks.TRAPPED_CHEST) || block.equals(Blocks.ENCHANTING_TABLE) || block.equals(Blocks.GRINDSTONE) || block.equals(Blocks.LECTERN) || block.equals(Blocks.STONECUTTER) || block.equals(Blocks.BELL) || block.equals(Blocks.CAKE) || block.equals(Blocks.CAMPFIRE) || block.equals(Blocks.SOUL_CAMPFIRE) || block.equals(Blocks.CAULDRON) || block.equals(Blocks.COMPOSTER) || block.equals(Blocks.CONDUIT) || block.equals(Blocks.END_PORTAL_FRAME) || block.equals(Blocks.FARMLAND) || block.equals(Blocks.DAYLIGHT_DETECTOR) || block.equals(Blocks.HONEY_BLOCK) || block.equals(Blocks.HOPPER) || block.equals(Blocks.LIGHTNING_ROD) || block.equals(Blocks.SCULK_SENSOR) || block.equals(Blocks.SCULK_SHRIEKER)) {
                return false;
            }
            return true;
        } else {
            Block block = state.getBlock();
            if (block.equals(Blocks.CHORUS_FLOWER) || block.equals(Blocks.SCAFFOLDING) || block.equals(Blocks.AZALEA) || block.equals(Blocks.FLOWERING_AZALEA)) {
                return true;
            } else if (block.equals(Blocks.SNOW)) {
                return state.getValue(LAYERS) == 1 || state.getValue(LAYERS) == 8;
            }
        }
        return false;
    }

    public static boolean shouldReplace(BlockPos pos, BlockState state, double entityYPos, boolean nether) {
        if ((int) entityYPos != pos.getY()) {
            return false;
        }
        if (nether) {
            return false;
        } else {
            if (state.getBlock().getDescriptionId().toUpperCase().endsWith(".CORAL_FAN") || state.getBlock().getDescriptionId().toUpperCase().endsWith(".CORAL")
                    || state.getBlock().getDescriptionId().toUpperCase().endsWith(".CORAL_WALL_FAN")) {
                return true;
            }
            if (state.getBlock().getClass() == SlabBlock.class && state.getValue(HALF) == Half.BOTTOM) {
                return true;
            }
            if (state.getBlock().getClass() == StairBlock.class && !state.getValue(WATERLOGGED)) {
                return true;
            }
            if (state.getBlock().getClass() == ChainBlock.class && !state.getValue(WATERLOGGED)) {
                return true;
            }
            if (state.getBlock().getClass() == CandleBlock.class) {
                return true;
            }
            if (state.getBlock().getClass() == TrapDoorBlock.class && !state.getValue(WATERLOGGED)) {
                return true;
            }
            Block block = state.getBlock();
            if (block.equals(Blocks.POINTED_DRIPSTONE) || block.equals(Blocks.SMALL_AMETHYST_BUD) || block.equals(Blocks.MEDIUM_AMETHYST_BUD) || block.equals(Blocks.LARGE_AMETHYST_BUD) || block.equals(Blocks.AMETHYST_CLUSTER) || block.equals(Blocks.SEA_PICKLE) || block.equals(Blocks.LANTERN) || block.equals(Blocks.SOUL_LANTERN) || block.equals(Blocks.CHEST) || block.equals(Blocks.ENDER_CHEST) || block.equals(Blocks.TRAPPED_CHEST) || block.equals(Blocks.CAMPFIRE) || block.equals(Blocks.SOUL_CAMPFIRE) || block.equals(Blocks.CONDUIT) || block.equals(Blocks.LIGHTNING_ROD) || block.equals(Blocks.SCULK_SENSOR) || block.equals(Blocks.SCULK_SHRIEKER)) {
                return true;
            }
            return false;
        }
    }
}
