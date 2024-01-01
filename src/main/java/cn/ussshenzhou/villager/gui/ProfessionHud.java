package cn.ussshenzhou.villager.gui;

import cn.ussshenzhou.t88.gui.util.HorizontalAlignment;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import cn.ussshenzhou.t88.gui.widegt.TItem;
import cn.ussshenzhou.t88.gui.widegt.TLabel;
import cn.ussshenzhou.t88.gui.widegt.TPanel;
import cn.ussshenzhou.villager.ModDataAttachments;
import cn.ussshenzhou.villager.Profession;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/**
 * @author USS_Shenzhou
 */
public class ProfessionHud extends TPanel {
    private final TItem icon;
    private final TLabel text;
    private final TLabel pickHint;
    private Profession profession;
    private static final int HEIGHT = 24;

    public ProfessionHud() {
        super();
        var profession = Minecraft.getInstance().player.getData(ModDataAttachments.PROFESSION).get();
        icon = new TItem(profession.icon);
        text = new TLabel(Component.literal(profession.name));
        this.profession = profession;
        icon.setItemSize(HEIGHT);
        this.add(icon);
        this.add(text);
        text.setHorizontalAlignment(HorizontalAlignment.CENTER);
        text.setAutoScroll(false);
        text.setFontSize(7 * 1.5f);
        pickHint = new TLabel();
        this.add(pickHint);
        pickHint.setVisibleT(false);
        pickHint.setHorizontalAlignment(HorizontalAlignment.CENTER);
        pickHint.setAutoScroll(false);
        pickHint.setFontSize(7 * 1.5f);
    }

    public void showPickProfessionHint(Block block) {
        Profession p = Profession.fromBlock(block);
        if (p != null) {
            if (p == profession) {
                hidePickProfessionHint();
                return;
            }
            pickHint.setText(Component.literal("按下 R 选取职业：" + p.name));
            pickHint.setVisibleT(true);
            icon.setVisibleT(false);
            text.setVisibleT(false);
        }
    }

    public void hidePickProfessionHint() {
        pickHint.setVisibleT(false);
        icon.setVisibleT(true);
        text.setVisibleT(true);
    }

    @Override
    public void tickT() {
        super.tickT();
        var p = Minecraft.getInstance().player.getData(ModDataAttachments.PROFESSION).get();
        if (profession != p) {
            icon.setItem(new ItemStack(p.icon));
            text.setText(Component.literal(p.name));
            profession = p;
        }
    }

    @Override
    public void layout() {
        icon.setBounds(0, 0, icon.getPreferredSize());
        LayoutHelper.BRightOfA(text, 0, icon, width - icon.getWidth(), HEIGHT);
        pickHint.setBounds(0, 0, width, height);
        super.layout();
    }

    @Override
    public void resizeAsHud(int screenWidth, int screenHeight) {
        this.setAbsBounds(screenWidth / 2 - 80 / 2, (int) (screenHeight * 0.65), 80, HEIGHT);
        super.resizeAsHud(screenWidth, screenHeight);
    }
}
