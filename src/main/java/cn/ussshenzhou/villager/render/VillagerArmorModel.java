package cn.ussshenzhou.villager.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;

/**
 * @author USS_Shenzhou
 */
public class VillagerArmorModel extends EntityModel<Player> {
    private final ModelPart head, chestplate, leggings, boots;

    public VillagerArmorModel(ModelPart root) {
        this.head = root.getChild("head");
        this.chestplate = root.getChild("chestplate");
        this.leggings = root.getChild("leggings");
        this.boots = root.getChild("boots");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.75F)), PartPose.ZERO);
        partdefinition.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(0, 19).addBox(-4.0F, -23.0F, -3.0F, 8.0F, 10.0F, 6.0F, new CubeDeformation(0.75F)), PartPose.offset(0.0F, 24.0F, 0.0F));
        partdefinition.addOrReplaceChild("leggings", CubeListBuilder.create().texOffs(27, 13).addBox(-4.0F, -11.5F, -3.0F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.75F)), PartPose.offset(0.0F, 24.0F, 0.0F));
        partdefinition.addOrReplaceChild("boots", CubeListBuilder.create().texOffs(29, 24).addBox(-4.0F, -6.0F, -3.0F, 8.0F, 2.0F, 6.0F, new CubeDeformation(0.75F)), PartPose.offset(0.0F, 24.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Player entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Deprecated
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
    }

    public void render(float partialTick, Player player, EquipmentSlot slot, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (slot == EquipmentSlot.OFFHAND || slot == EquipmentSlot.MAINHAND) {
            return;
        }
        poseStack.pushPose();
        if (slot == EquipmentSlot.HEAD) {
            this.head.xRot = (float) (player.getViewXRot(partialTick) / 180 * Math.PI);
            this.head.yRot=(float) (Mth.lerp(partialTick, player.yHeadRotO - player.yBodyRotO, player.yHeadRot - player.yBodyRot) / 180 * Math.PI);
        }
        var toRender = switch (slot) {
            case CHEST -> chestplate;
            case HEAD -> head;
            case LEGS -> leggings;
            default -> boots;
        };
        toRender.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        poseStack.popPose();
    }
}
