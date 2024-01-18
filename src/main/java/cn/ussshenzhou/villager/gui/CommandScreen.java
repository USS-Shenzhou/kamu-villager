package cn.ussshenzhou.villager.gui;

import cn.ussshenzhou.t88.gui.advanced.THoverSensitiveImageButton;
import cn.ussshenzhou.t88.gui.screen.TScreen;
import cn.ussshenzhou.t88.gui.util.ImageFit;
import cn.ussshenzhou.t88.gui.widegt.TItem;
import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.villager.Villager;
import cn.ussshenzhou.villager.VillagerManager;
import cn.ussshenzhou.villager.entity.Command;
import cn.ussshenzhou.villager.input.KeyInputListener;
import cn.ussshenzhou.villager.network.CommandVillagerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

/**
 * @author USS_Shenzhou
 */
public class CommandScreen extends TScreen {
    private static final int SIZE = TItem.DEFAULT_SIZE * 2;
    private static final int GAP = 36;

    private final Button gatherAround = new Button(Component.empty(),
            button -> {
                VillagerManager.command(Minecraft.getInstance().player, Command.FOLLOW);
                NetworkHelper.sendToServer(new CommandVillagerPacket(Command.FOLLOW));
                CommandScreen.this.onClose(true);
            },
            new ResourceLocation(Villager.MOD_ID, "textures/gui/gather.png"),
            new ResourceLocation(Villager.MOD_ID, "textures/gui/gather_hover.png")
    );

    /*private final Button move = new Button(Component.empty(),
            button -> {

                CommandScreen.this.onClose(true);
            },
            new ResourceLocation(Villager.MOD_ID, "textures/gui/move.png"),
            new ResourceLocation(Villager.MOD_ID, "textures/gui/move_hover.png")
    );*/

    private final Button dig = new Button(Component.empty(),
            button -> {
                VillagerManager.command(Minecraft.getInstance().player, Command.DIG);
                NetworkHelper.sendToServer(new CommandVillagerPacket(Command.DIG));
                CommandScreen.this.onClose(true);
            },
            new ResourceLocation(Villager.MOD_ID, "textures/gui/dig.png"),
            new ResourceLocation(Villager.MOD_ID, "textures/gui/dig_hover.png")
    );

    public CommandScreen() {
        super(Component.empty());
        initButton(gatherAround);
        //initButton(move);
        initButton(dig);
        gatherAround.setTooltip(Tooltip.create(Component.literal("召回")));
        //move.setTooltip(Tooltip.create(Component.literal("移动")));
        dig.setTooltip(Tooltip.create(Component.literal("挖掘")));
    }

    public void initButton(THoverSensitiveImageButton button) {
        button.setPadding(4);
        this.add(button);
        button.getBackgroundImage().setImageFit(ImageFit.STRETCH);
        button.getBackgroundImageHovered().setImageFit(ImageFit.STRETCH);
    }

    @Override
    protected void renderBackGround(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
    }

    @Override
    public void layout() {
        Vector2i center = new Vector2i(width / 2, height / 2);
        gatherAround.setBounds(center.x - SIZE / 2 - GAP, center.y - SIZE / 2, SIZE, SIZE);
        //move.setBounds(center.x - SIZE / 2, center.y - SIZE / 2 - GAP, SIZE, SIZE);
        dig.setBounds(center.x - SIZE / 2 + GAP, center.y - SIZE / 2, SIZE, SIZE);
        super.layout();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == KeyInputListener.COMMAND.getKey().getValue()) {
            this.onClose(true);
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    private static class Button extends THoverSensitiveImageButton {

        public Button(Component text1, net.minecraft.client.gui.components.Button.OnPress onPress, @Nullable ResourceLocation backgroundImageLocation, @Nullable ResourceLocation backgroundImageLocationHovered) {
            super(text1, onPress, backgroundImageLocation, backgroundImageLocationHovered);
        }

        @Override
        public void layout() {
            super.layout();
            button.setBounds(0, 0, this.width, this.height);
        }
    }
}
