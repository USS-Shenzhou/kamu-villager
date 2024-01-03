package cn.ussshenzhou.villager.render;

import cn.ussshenzhou.villager.Villager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * @author USS_Shenzhou
 */
public class VillagerArmorLayer extends RenderLayer<Player, VillagerModel<Player>> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(Villager.MOD_ID, "villager_armor_layer"), "main");
    private final VillagerArmorModel armorModel;

    public VillagerArmorLayer(RenderLayerParent<Player, VillagerModel<Player>> pRenderer, VillagerArmorModel armorModel) {
        super(pRenderer);
        this.armorModel = armorModel;
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, Player pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        this.renderArmorPiece(pPartialTick, pPoseStack, pBuffer, pLivingEntity, EquipmentSlot.CHEST, pPackedLight);
        this.renderArmorPiece(pPartialTick, pPoseStack, pBuffer, pLivingEntity, EquipmentSlot.LEGS, pPackedLight);
        this.renderArmorPiece(pPartialTick, pPoseStack, pBuffer, pLivingEntity, EquipmentSlot.FEET, pPackedLight);
        this.renderArmorPiece(pPartialTick, pPoseStack, pBuffer, pLivingEntity, EquipmentSlot.HEAD, pPackedLight);
    }

    private void renderArmorPiece(float pPartialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, Player player, EquipmentSlot slot, int light) {
        ItemStack itemstack = player.getItemBySlot(slot);
        Item item = itemstack.getItem();
        if (item instanceof ArmorItem armoritem) {
            if (armoritem.getEquipmentSlot() == slot) {
                var armorResource = getTexture(armoritem);
                if (armorResource == null) {
                    return;
                }
                VertexConsumer vertexconsumer = multiBufferSource.getBuffer(RenderType.armorCutoutNoCull(armorResource));
                armorModel.render(pPartialTick, player, slot, poseStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1.0F);
                if (itemstack.hasFoil()) {
                    this.renderGlint(pPartialTick, player, slot, poseStack, multiBufferSource, light);
                }
            }
        }
    }

    public static final ResourceLocation IRON_ARMOR = new ResourceLocation(Villager.MOD_ID, "textures/entity/iron_armor.png");
    public static final ResourceLocation DIAMOND_ARMOR = new ResourceLocation(Villager.MOD_ID, "textures/entity/diamond_armor.png");
    public static final ResourceLocation NETHERITE_ARMOR = new ResourceLocation(Villager.MOD_ID, "textures/entity/netherite_armor.png");

    private static ResourceLocation getTexture(ArmorItem item) {
        return switch ((ArmorMaterials) item.getMaterial()) {
            case IRON -> IRON_ARMOR;
            case DIAMOND -> DIAMOND_ARMOR;
            case NETHERITE -> NETHERITE_ARMOR;
            default -> null;
        };
    }

    private void renderGlint(float pPartialTick, Player player, EquipmentSlot slot, PoseStack poseStack, MultiBufferSource multiBufferSource, int light) {
        armorModel.render(pPartialTick, player, slot, poseStack, multiBufferSource.getBuffer(RenderType.armorEntityGlint()), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
