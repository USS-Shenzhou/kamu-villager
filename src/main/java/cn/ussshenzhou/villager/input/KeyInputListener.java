package cn.ussshenzhou.villager.input;

import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.villager.ModDataAttachments;
import cn.ussshenzhou.villager.Profession;
import cn.ussshenzhou.villager.gui.SelfTradeScreen;
import cn.ussshenzhou.villager.network.ChooseProfessionPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

import static cn.ussshenzhou.villager.ClientForgeBusListener.WORK_BLOCKS;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class KeyInputListener {
    public static final KeyMapping SELF_TRADE = new KeyMapping(
            "自体交易/选取职业", KeyConflictContext.IN_GAME, KeyModifier.NONE,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "我是村民"
    );
    public static final KeyMapping COMMAND = new KeyMapping(
            "指挥", KeyConflictContext.IN_GAME, KeyModifier.NONE,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "我是村民"
    );

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (SELF_TRADE.consumeClick()) {
            if (!tryPickProfession()) {
                minecraft.setScreen(new SelfTradeScreen());
            }
        }
        if (COMMAND.consumeClick()) {

        }
    }

    private static boolean tryPickProfession() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        var level = Minecraft.getInstance().level;
        var blockHit = player.pick(4, 0, false);
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockHitResult) blockHit).getBlockPos();
            var block = level.getBlockState(blockpos).getBlock();
            if (WORK_BLOCKS.contains(block)) {
                var p = Profession.fromBlock(block);
                if (p != player.getData(ModDataAttachments.PROFESSION).get()) {
                    NetworkHelper.sendToServer(new ChooseProfessionPacket(player, p));
                    var mc = Minecraft.getInstance();
                    mc.player.setData(ModDataAttachments.PROFESSION, p);
                    mc.player.getData(ModDataAttachments.PROFESSION).set(p);
                    mc.level.playLocalSound(mc.player, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1, 1);
                    return true;
                }
            }
        }
        return false;
    }
}
