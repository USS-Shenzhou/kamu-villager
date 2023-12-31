package cn.ussshenzhou.villager.gui;

import cn.ussshenzhou.t88.gui.container.TScrollContainer;
import cn.ussshenzhou.t88.gui.util.HorizontalAlignment;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import cn.ussshenzhou.t88.gui.widegt.TLabel;
import cn.ussshenzhou.villager.ModDataAttachments;
import cn.ussshenzhou.villager.Profession;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import static cn.ussshenzhou.villager.gui.SelfTradeScreen.BUTTON_WIDTH;

/**
 * @author USS_Shenzhou
 */
public class SelfTradePanel extends TScrollContainer {

    public SelfTradePanel() {
        super();
        initFromProfession();
    }

    private void initFromProfession() {
        var player = Minecraft.getInstance().player;
        //noinspection DataFlowIssue
        Profession profession = player.getData(ModDataAttachments.PROFESSION);
        switch (profession) {
            case NITWIT -> {
                add(new TLabel(Component.literal("傻子！"))
                        .setHorizontalAlignment(HorizontalAlignment.CENTER)
                        .setBackground(0x80ffffff)
                );
            }
            case ARMORER -> {
            }
            case BUTCHER -> {
            }
            case CLERIC -> {
            }
            case FARMER -> {
            }
            case FLETCHER -> {
            }
            case TOOL_SMITH -> {
            }
            case WEAPON_SMITH -> {
            }
        }
    }

    @Override
    public void layout() {
        for (int i = 0; i < children.size(); i++) {
            if (i == 0) {
                children.get(i).setBounds(0, 0, BUTTON_WIDTH, 20);
            } else {
                LayoutHelper.BBottomOfA(children.get(i), 2, children.get(i - 1));
            }
        }
    }
}
