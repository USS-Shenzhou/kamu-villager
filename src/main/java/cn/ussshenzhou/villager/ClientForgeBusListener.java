package cn.ussshenzhou.villager;

import cn.ussshenzhou.t88.gui.HudManager;
import cn.ussshenzhou.villager.gui.ProfessionHud;
import com.google.common.collect.Sets;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.HashSet;
import java.util.Map;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeBusListener {
    public static ProfessionHud professionHud;
    public static final HashSet<Block> WORK_BLOCKS = Sets.newHashSet(
            Blocks.BLAST_FURNACE,
            Blocks.SMOKER,
            Blocks.BREWING_STAND,
            Blocks.COMPOSTER,
            Blocks.FLETCHING_TABLE,
            Blocks.SMITHING_TABLE,
            Blocks.GRINDSTONE
    );

    @SuppressWarnings("DataFlowIssue")
    @SubscribeEvent
    public static void checkLookingAt(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            return;
        }
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (professionHud == null) {
            professionHud = new ProfessionHud();
            HudManager.add(professionHud);
        }
        var level = Minecraft.getInstance().level;
        var blockHit = player.pick(4, 0, false);
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockHitResult) blockHit).getBlockPos();
            var block = level.getBlockState(blockpos).getBlock();
            if (WORK_BLOCKS.contains(block)) {
                professionHud.showPickProfessionHint(block);
                return;
            }
        }
        professionHud.hidePickProfessionHint();
    }
}
