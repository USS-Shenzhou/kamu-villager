package cn.ussshenzhou.villager.render;

import cn.ussshenzhou.villager.HXYAHelper;
import cn.ussshenzhou.villager.Villager;
import cn.ussshenzhou.villager.entity.fakeplayer.FalsePlayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author USS_Shenzhou
 */
@ParametersAreNonnullByDefault
public class VillagerPlayerRenderer extends LivingEntityRenderer<Player, VillagerModel<Player>> {
    private FalsePlayerRenderer falsePlayerRenderer;
    private static final ResourceLocation STEVE = new ResourceLocation("textures/entity/player/wide/steve.png");

    public VillagerPlayerRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5f);
        this.addLayer(new CrossedArmsItemLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new VillagerArmorLayer(this, new VillagerArmorModel(context.bakeLayer(VillagerArmorLayer.LAYER_LOCATION))));
        this.falsePlayerRenderer = new FalsePlayerRenderer(context, false);
    }

    public static final ResourceLocation UNCLE = new ResourceLocation(Villager.MOD_ID, "textures/entity/uncle.png");
    public static final ResourceLocation KAMU = new ResourceLocation(Villager.MOD_ID, "textures/entity/kamu.png");
    public static final ResourceLocation CEN = new ResourceLocation(Villager.MOD_ID, "textures/entity/cen.png");
    public static final ResourceLocation MAO = new ResourceLocation(Villager.MOD_ID, "textures/entity/mao.png");
    public static final ResourceLocation MELOR = new ResourceLocation(Villager.MOD_ID, "textures/entity/melor.png");

    /**
     * @see net.minecraft.client.renderer.entity.VillagerRenderer
     */
    @Override
    public ResourceLocation getTextureLocation(Player player) {
        if (HXYAHelper.isCen(player)) {
            return CEN;
        } else if (HXYAHelper.isUncle(player)) {
            return UNCLE;
        } else if (HXYAHelper.isHeiMao(player)) {
            return MAO;
        } else if (HXYAHelper.isMelor(player)) {
            return MELOR;
        }
        return KAMU;
    }

    @Override
    public void render(Player pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        if (isFalsePlayer(pEntity)) {
            falsePlayerRenderer.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
        } else {
            super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
        }
    }

    private static boolean isFalsePlayer(Player player) {
        return player.getGameProfile().getName().equals(FalsePlayer.NAME);
    }

    public static class FalsePlayerRenderer extends LivingEntityRenderer<Player, PlayerModel<Player>> {
        public FalsePlayerRenderer(EntityRendererProvider.Context pContext, boolean pUseSlimModel) {
            super(pContext, new PlayerModel<>(pContext.bakeLayer(pUseSlimModel ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), pUseSlimModel), 0.5F);
            this.addLayer(
                    new HumanoidArmorLayer<>(
                            this,
                            new HumanoidArmorModel(pContext.bakeLayer(pUseSlimModel ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR)),
                            new HumanoidArmorModel(pContext.bakeLayer(pUseSlimModel ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR)),
                            pContext.getModelManager()
                    )
            );
            this.addLayer(new PlayerItemInHandLayer<>(this, pContext.getItemInHandRenderer()));
            this.addLayer(new ArrowLayer<>(pContext, this));
            //this.addLayer(new Deadmau5EarsLayer(this));
            //this.addLayer(new CapeLayer(this));
            this.addLayer(new CustomHeadLayer<>(this, pContext.getModelSet(), pContext.getItemInHandRenderer()));
            this.addLayer(new ElytraLayer<>(this, pContext.getModelSet()));
            this.addLayer(new ParrotOnShoulderLayer<>(this, pContext.getModelSet()));
            this.addLayer(new SpinAttackEffectLayer<>(this, pContext.getModelSet()));
            this.addLayer(new BeeStingerLayer<>(this));
        }

        @Override
        public void render(Player pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
            this.setModelProperties(pEntity);
            //if (net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.client.event.RenderPlayerEvent.Pre(pEntity, this, pPartialTicks, pPoseStack, pBuffer, pPackedLight)).isCanceled()) {
            //    return;
            //}
            super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
            //net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.client.event.RenderPlayerEvent.Post(pEntity, this, pPartialTicks, pPoseStack, pBuffer, pPackedLight));
        }

        @Override
        public Vec3 getRenderOffset(Player pEntity, float pPartialTicks) {
            return pEntity.isCrouching() ? new Vec3(0.0, -0.125, 0.0) : super.getRenderOffset(pEntity, pPartialTicks);
        }

        private void setModelProperties(Player pClientPlayer) {
            PlayerModel<Player> playermodel = this.getModel();
            if (pClientPlayer.isSpectator()) {
                playermodel.setAllVisible(false);
                playermodel.head.visible = true;
                playermodel.hat.visible = true;
            } else {
                playermodel.setAllVisible(true);
                playermodel.hat.visible = pClientPlayer.isModelPartShown(PlayerModelPart.HAT);
                playermodel.jacket.visible = pClientPlayer.isModelPartShown(PlayerModelPart.JACKET);
                playermodel.leftPants.visible = pClientPlayer.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
                playermodel.rightPants.visible = pClientPlayer.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
                playermodel.leftSleeve.visible = pClientPlayer.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
                playermodel.rightSleeve.visible = pClientPlayer.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
                playermodel.crouching = pClientPlayer.isCrouching();
                HumanoidModel.ArmPose humanoidmodel$armpose = getArmPose(pClientPlayer, InteractionHand.MAIN_HAND);
                HumanoidModel.ArmPose humanoidmodel$armpose1 = getArmPose(pClientPlayer, InteractionHand.OFF_HAND);
                if (humanoidmodel$armpose.isTwoHanded()) {
                    humanoidmodel$armpose1 = pClientPlayer.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
                }

                if (pClientPlayer.getMainArm() == HumanoidArm.RIGHT) {
                    playermodel.rightArmPose = humanoidmodel$armpose;
                    playermodel.leftArmPose = humanoidmodel$armpose1;
                } else {
                    playermodel.rightArmPose = humanoidmodel$armpose1;
                    playermodel.leftArmPose = humanoidmodel$armpose;
                }
            }
        }

        private static HumanoidModel.ArmPose getArmPose(Player pPlayer, InteractionHand pHand) {
            ItemStack itemstack = pPlayer.getItemInHand(pHand);
            if (itemstack.isEmpty()) {
                return HumanoidModel.ArmPose.EMPTY;
            } else {
                if (pPlayer.getUsedItemHand() == pHand && pPlayer.getUseItemRemainingTicks() > 0) {
                    UseAnim useanim = itemstack.getUseAnimation();
                    if (useanim == UseAnim.BLOCK) {
                        return HumanoidModel.ArmPose.BLOCK;
                    }

                    if (useanim == UseAnim.BOW) {
                        return HumanoidModel.ArmPose.BOW_AND_ARROW;
                    }

                    if (useanim == UseAnim.SPEAR) {
                        return HumanoidModel.ArmPose.THROW_SPEAR;
                    }

                    if (useanim == UseAnim.CROSSBOW && pHand == pPlayer.getUsedItemHand()) {
                        return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                    }

                    if (useanim == UseAnim.SPYGLASS) {
                        return HumanoidModel.ArmPose.SPYGLASS;
                    }

                    if (useanim == UseAnim.TOOT_HORN) {
                        return HumanoidModel.ArmPose.TOOT_HORN;
                    }

                    if (useanim == UseAnim.BRUSH) {
                        return HumanoidModel.ArmPose.BRUSH;
                    }
                } else if (!pPlayer.swinging && itemstack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(itemstack)) {
                    return HumanoidModel.ArmPose.CROSSBOW_HOLD;
                }
                HumanoidModel.ArmPose forgeArmPose = net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.of(itemstack).getArmPose(pPlayer, pHand, itemstack);
                if (forgeArmPose != null) {
                    return forgeArmPose;
                }

                return HumanoidModel.ArmPose.ITEM;
            }
        }

        /**
         * Returns the location of an entity's texture.
         */
        @Override
        public ResourceLocation getTextureLocation(Player pEntity) {
            if (isFalsePlayer(pEntity)) {
                return STEVE;
            } else {
                return ((AbstractClientPlayer) pEntity).getSkin().texture();
            }
        }

        @Override
        protected void scale(Player pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
            float f = 0.9375F;
            pPoseStack.scale(0.9375F, 0.9375F, 0.9375F);
        }

        @Override
        protected void renderNameTag(Player pEntity, Component pDisplayName, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
            /*if (isFalsePlayer(pEntity)) {
                return;
            }*/
            double d0 = this.entityRenderDispatcher.distanceToSqr(pEntity);
            pPoseStack.pushPose();
            if (d0 < 100.0) {
                Scoreboard scoreboard = pEntity.getScoreboard();
                Objective objective = scoreboard.getDisplayObjective(DisplaySlot.BELOW_NAME);
                if (objective != null) {
                    ReadOnlyScoreInfo readonlyscoreinfo = scoreboard.getPlayerScoreInfo(pEntity, objective);
                    Component component = ReadOnlyScoreInfo.safeFormatValue(readonlyscoreinfo, objective.numberFormatOrDefault(StyledFormat.NO_STYLE));
                    super.renderNameTag(
                            pEntity,
                            Component.empty().append(component).append(CommonComponents.SPACE).append(objective.getDisplayName()),
                            pPoseStack,
                            pBuffer,
                            pPackedLight
                    );
                    pPoseStack.translate(0.0F, 9.0F * 1.15F * 0.025F, 0.0F);
                }
            }

            super.renderNameTag(pEntity, pDisplayName, pPoseStack, pBuffer, pPackedLight);
            pPoseStack.popPose();
        }

        public void renderRightHand(PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, Player pPlayer) {
            //if (!net.neoforged.neoforge.client.ClientHooks.renderSpecificFirstPersonArm(pPoseStack, pBuffer, pCombinedLight, pPlayer, HumanoidArm.RIGHT)) {
            this.renderHand(pPoseStack, pBuffer, pCombinedLight, pPlayer, this.model.rightArm, this.model.rightSleeve);
            //}
        }

        public void renderLeftHand(PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, Player pPlayer) {
            //if (!net.neoforged.neoforge.client.ClientHooks.renderSpecificFirstPersonArm(pPoseStack, pBuffer, pCombinedLight, pPlayer, HumanoidArm.LEFT)) {
            this.renderHand(pPoseStack, pBuffer, pCombinedLight, pPlayer, this.model.leftArm, this.model.leftSleeve);
            //}
        }

        private void renderHand(
                PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, Player pPlayer, ModelPart pRendererArm, ModelPart pRendererArmwear
        ) {
            PlayerModel<Player> playermodel = this.getModel();
            this.setModelProperties(pPlayer);
            playermodel.attackTime = 0.0F;
            playermodel.crouching = false;
            playermodel.swimAmount = 0.0F;
            playermodel.setupAnim(pPlayer, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            pRendererArm.xRot = 0.0F;
            ResourceLocation resourcelocation = ((AbstractClientPlayer) pPlayer).getSkin().texture();
            pRendererArm.render(pPoseStack, pBuffer.getBuffer(RenderType.entitySolid(resourcelocation)), pCombinedLight, OverlayTexture.NO_OVERLAY);
            pRendererArmwear.xRot = 0.0F;
            pRendererArmwear.render(pPoseStack, pBuffer.getBuffer(RenderType.entityTranslucent(resourcelocation)), pCombinedLight, OverlayTexture.NO_OVERLAY);
        }

        @Override
        protected void setupRotations(Player pEntityLiving, PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
            float f = pEntityLiving.getSwimAmount(pPartialTicks);
            float f1 = pEntityLiving.getViewXRot(pPartialTicks);
            if (pEntityLiving.isFallFlying()) {
                super.setupRotations(pEntityLiving, pPoseStack, pAgeInTicks, pRotationYaw, pPartialTicks);
                float f2 = (float) pEntityLiving.getFallFlyingTicks() + pPartialTicks;
                float f3 = Mth.clamp(f2 * f2 / 100.0F, 0.0F, 1.0F);
                if (!pEntityLiving.isAutoSpinAttack()) {
                    pPoseStack.mulPose(Axis.XP.rotationDegrees(f3 * (-90.0F - f1)));
                }

                Vec3 vec3 = pEntityLiving.getViewVector(pPartialTicks);
                Vec3 vec31 = ((AbstractClientPlayer) pEntityLiving).getDeltaMovementLerped(pPartialTicks);
                double d0 = vec31.horizontalDistanceSqr();
                double d1 = vec3.horizontalDistanceSqr();
                if (d0 > 0.0 && d1 > 0.0) {
                    double d2 = (vec31.x * vec3.x + vec31.z * vec3.z) / Math.sqrt(d0 * d1);
                    double d3 = vec31.x * vec3.z - vec31.z * vec3.x;
                    pPoseStack.mulPose(Axis.YP.rotation((float) (Math.signum(d3) * Math.acos(d2))));
                }
            } else if (f > 0.0F) {
                super.setupRotations(pEntityLiving, pPoseStack, pAgeInTicks, pRotationYaw, pPartialTicks);
                float f4 = pEntityLiving.isInWater() || pEntityLiving.isInFluidType((fluidType, height) -> pEntityLiving.canSwimInFluidType(fluidType)) ? -90.0F - pEntityLiving.getXRot() : -90.0F;
                float f5 = Mth.lerp(f, 0.0F, f4);
                pPoseStack.mulPose(Axis.XP.rotationDegrees(f5));
                if (pEntityLiving.isVisuallySwimming()) {
                    pPoseStack.translate(0.0F, -1.0F, 0.3F);
                }
            } else {
                super.setupRotations(pEntityLiving, pPoseStack, pAgeInTicks, pRotationYaw, pPartialTicks);
            }
        }
    }

}
