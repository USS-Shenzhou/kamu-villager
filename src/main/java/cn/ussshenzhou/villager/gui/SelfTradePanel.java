package cn.ussshenzhou.villager.gui;

import cn.ussshenzhou.t88.gui.advanced.THoverSensitiveImageButton;
import cn.ussshenzhou.t88.gui.container.TScrollContainer;
import cn.ussshenzhou.t88.gui.util.HorizontalAlignment;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import cn.ussshenzhou.t88.gui.widegt.TItem;
import cn.ussshenzhou.t88.gui.widegt.TLabel;
import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.t88.util.InventoryHelper;
import cn.ussshenzhou.villager.*;
import cn.ussshenzhou.villager.item.ModItems;
import cn.ussshenzhou.villager.network.SelfTradePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

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
        Profession profession = player.getData(ModDataAttachments.PROFESSION).get();
        var inventory = player.getInventory();
        switch (profession) {
            case NITWIT -> add(new TLabel(Component.literal("傻子！"))
                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                    .setBackground(0x80ffffff)
            );
            case ARMORER -> {
                InventoryHelper.getAllAsStream(inventory)
                        .filter(itemStack -> itemStack.is(Tags.Items.ARMORS))
                        .forEach(itemStack -> {
                            int value = switch ((ArmorMaterials) ((ArmorItem) itemStack.getItem()).getMaterial()) {
                                case LEATHER -> 1;
                                case IRON, GOLD -> 8;
                                case DIAMOND -> 16;
                                case NETHERITE -> 32;
                                default -> 2;
                            };
                            var b = new SelfTradeOnceButton(itemStack.copy(), new ItemStack(Items.EMERALD, value));
                            b.from.setShowTooltip(true);
                            add(b);
                        });
                add(new SelfTradeButton(new ItemStack(Items.EMERALD, 12), new ItemStack(ModItems.IRON_ARMOR.get())));
                add(new SelfTradeButton(new ItemStack(Items.EMERALD, 24), new ItemStack(ModItems.DIAMOND_ARMOR.get())));
                add(new SelfTradeButton(new ItemStack(Items.EMERALD, 48), new ItemStack(ModItems.NETHERITE_ARMOR.get())));
            }
            case BUTCHER -> {
                InventoryHelper.getAllAsStream(inventory)
                        .map(ItemStack::getItem)
                        .filter(Item::isEdible)
                        .filter(item -> {
                            var food = item.getFoodProperties(new ItemStack(item), null);
                            if (food == null) {
                                return false;
                            }
                            return food.isMeat();
                        })
                        .distinct()
                        .forEach(item -> {
                            var food = item.getFoodProperties(new ItemStack(item), null);
                            if (food == null) {
                                return;
                            }
                            Tuple<Integer, Integer> needAndValue = getNeedAndValue(food);
                            var b = new SelfTradeButton(new ItemStack(item, needAndValue.getA()), new ItemStack(Items.EMERALD, needAndValue.getB()));
                            add(b);
                        });
                add(new SelfTradeButton(new ItemStack(Items.EMERALD, 9), new ItemStack(Items.COOKED_BEEF, 2)));
            }
            case CLERIC -> {
                InventoryHelper.getAllAsStream(inventory)
                        .map(ItemStack::getItem)
                        .filter(item -> item instanceof PotionItem)
                        .distinct()
                        .forEach(item -> add(new SelfTradeButton(new ItemStack(item, 1), new ItemStack(Items.EMERALD, 6))));
                add(new SelfTradeButton(new ItemStack(Items.EMERALD, 6), new ItemStack(Items.GOLDEN_CARROT, 1)));
                add(new SelfTradeButton(new ItemStack(Items.EMERALD, 6), new ItemStack(Items.GOLDEN_APPLE, 1)));
                if (HXYAHelper.isKaMu(player)) {
                    add(new SelfTradeButton(new ItemStack(Items.EMERALD, 6), GeneralForgeBusListener.LAVA_BOTTLE.copy())
                            .setTooltip(Tooltip.create(Component.literal("您好，外卖！")))
                    );
                }
            }
            case FARMER -> {
                InventoryHelper.getAllAsStream(inventory)
                        .filter(itemStack -> itemStack.is(Tags.Items.CROPS))
                        .map(ItemStack::getItem)
                        .filter(Item::isEdible)
                        .distinct()
                        .forEach(item -> {
                            if (item == Items.CARROT) {
                                if (HXYAHelper.isUncle(player)) {
                                    add(new SelfTradeButton(new ItemStack(Items.CARROT, 4), new ItemStack(Items.EMERALD, 1))
                                            .setTooltip(Tooltip.create(Component.literal("胡萝卜垄断！")))
                                    );
                                } else {
                                    return;
                                }
                            }

                            var food = item.getFoodProperties(new ItemStack(item), null);
                            if (food == null) {
                                return;
                            }
                            Tuple<Integer, Integer> needAndValue = getNeedAndValue(food);
                            var b = new SelfTradeButton(new ItemStack(item, needAndValue.getA()), new ItemStack(Items.EMERALD, needAndValue.getB()));
                            add(b);
                        });
                add(new SelfTradeButton(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.WHEAT, 1)));
                add(new SelfTradeButton(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.BEETROOT, 1)));
                add(new SelfTradeButton(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.APPLE, 1)));
                add(new SelfTradeButton(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.MELON_SLICE, 1)));
                add(new SelfTradeButton(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.POTATO, 1)));
                if (HXYAHelper.isUncle(player)) {
                    add(new SelfTradeButton(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.CARROT, 4))
                            .setTooltip(Tooltip.create(Component.literal("家里有很多胡萝卜很正常吧。")))
                    );
                }
            }
            case FLETCHER -> {
                InventoryHelper.getAllAsStream(inventory)
                        .filter(itemStack -> itemStack.getItem() instanceof BowItem)
                        .forEach(itemStack -> {
                            var b = new SelfTradeOnceButton(itemStack.copy(), new ItemStack(Items.EMERALD, 12));
                            b.from.setShowTooltip(true);
                            add(b);
                        });
                add(new SelfTradeButton(new ItemStack(Items.EMERALD, 16), new ItemStack(ModItems.BOW.get())));
                add(new SelfTradeButton(new ItemStack(Items.EMERALD, 4), new ItemStack(Items.ARROW, 16)));
            }
            case TOOL_SMITH -> {
                InventoryHelper.getAllAsStream(inventory)
                        .filter(itemStack -> itemStack.getItem() instanceof PickaxeItem || itemStack.getItem() instanceof ShovelItem)
                        .forEach(itemStack -> {
                            int value = switch ((Tiers) ((TieredItem) itemStack.getItem()).getTier()) {
                                case STONE -> 1;
                                case IRON, GOLD -> 8;
                                case DIAMOND -> 16;
                                case NETHERITE -> 32;
                                default -> 0;
                            };
                            if (value == 0) {
                                return;
                            }
                            var b = new SelfTradeOnceButton(itemStack.copy(), new ItemStack(Items.EMERALD, value));
                            b.from.setShowTooltip(true);
                            add(b);
                        });
                add(new SelfTradeButton(new ItemStack(Items.EMERALD, 12), new ItemStack(ModItems.IRON_TOOL.get())));
                add(new SelfTradeButton(new ItemStack(Items.EMERALD, 24), new ItemStack(ModItems.DIAMOND_TOOL.get())));
                add(new SelfTradeButton(new ItemStack(Items.EMERALD, 48), new ItemStack(ModItems.NETHERITE_TOOL.get())));
                if (HXYAHelper.isHeiMao(player)) {
                    add(new SelfTradeButton(new ItemStack(Items.EMERALD, 64), GeneralForgeBusListener.MAO_SHOVEL.copy())
                            .setTooltip(Tooltip.create(Component.literal("钻石铲子！\n只有你能进行此交易。")))
                    );
                }
            }
            case WEAPON_SMITH -> {
                InventoryHelper.getAllAsStream(inventory)
                        .filter(itemStack -> itemStack.getItem() instanceof AxeItem || itemStack.getItem() instanceof SwordItem)
                        .forEach(itemStack -> {
                            int value = switch ((Tiers) ((TieredItem) itemStack.getItem()).getTier()) {
                                case STONE -> 1;
                                case IRON, GOLD -> 8;
                                case DIAMOND -> 16;
                                case NETHERITE -> 32;
                                default -> 0;
                            };
                            if (value == 0) {
                                return;
                            }
                            var b = new SelfTradeOnceButton(itemStack.copy(), new ItemStack(Items.EMERALD, value));
                            b.from.setShowTooltip(true);
                            add(b);
                        });
                add(new SelfTradeButton(new ItemStack(Items.EMERALD, 12), new ItemStack(ModItems.IRON_WEAPON.get())));
                add(new SelfTradeButton(new ItemStack(Items.EMERALD, 24), new ItemStack(ModItems.DIAMOND_WEAPON.get())));
                add(new SelfTradeButton(new ItemStack(Items.EMERALD, 48), new ItemStack(ModItems.NETHERITE_WEAPON.get())));
                if (HXYAHelper.isCen(player)) {
                    add(new SelfTradeButton(new ItemStack(Items.EMERALD, 64), GeneralForgeBusListener.CEN_AXE.copy())
                            .setTooltip(Tooltip.create(Component.literal("钻石斧杀人魔！\n只有你能进行此交易。")))
                    );
                }
            }
        }
    }

    @NotNull
    private static Tuple<Integer, Integer> getNeedAndValue(FoodProperties food) {
        return new Tuple<>(2, food.getNutrition());
    }

    @Override
    public void layout() {
        for (int i = 0; i < children.size(); i++) {
            if (i == 0) {
                children.get(i).setBounds(0, 0, BUTTON_WIDTH, 20);
            } else {
                LayoutHelper.BBottomOfA(children.get(i), 0, children.get(i - 1));
            }
        }
        super.layout();
    }

    @Override
    protected void renderBackground(GuiGraphics guigraphics, int pMouseX, int pMouseY, float pPartialTick) {
        //super.renderBackground(guigraphics, pMouseX, pMouseY, pPartialTick);
    }

    public static class SelfTradeButton extends THoverSensitiveImageButton {
        protected final TItem from, to;

        public SelfTradeButton(ItemStack from, ItemStack to) {
            super(Component.literal("→"), button -> NetworkHelper.sendToServer(new SelfTradePacket(Minecraft.getInstance().player.getUUID(), from, to)),
                    new ResourceLocation(Villager.MOD_ID, "textures/gui/button.png"),
                    new ResourceLocation(Villager.MOD_ID, "textures/gui/button_highlighted.png"));
            this.from = new TItem(from);
            this.to = new TItem(to);
            this.add(this.from);
            this.add(this.to);
            this.text.setHorizontalAlignment(HorizontalAlignment.CENTER);
        }

        @Override
        public void layout() {
            from.setBounds(2, 2, from.getPreferredSize());
            to.setBounds(width - to.getPreferredSize().x - 2, 2, to.getPreferredSize());
            super.layout();
        }
    }

    public static class SelfTradeOnceButton extends SelfTradeButton {

        public SelfTradeOnceButton(ItemStack from, ItemStack to) {
            super(from, to);
            this.button.setOnPress(button -> {
                NetworkHelper.sendToServer(new SelfTradePacket(Minecraft.getInstance().player.getUUID(), from, to));
                getParentInstanceOf(SelfTradePanel.class).remove(this);
                getParentInstanceOf(SelfTradePanel.class).layout();
            });
        }
    }
}
