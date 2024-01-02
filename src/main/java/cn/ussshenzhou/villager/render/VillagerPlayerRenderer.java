package cn.ussshenzhou.villager.render;

import cn.ussshenzhou.villager.HXYAHelper;
import cn.ussshenzhou.villager.Villager;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * @author USS_Shenzhou
 */
public class VillagerPlayerRenderer extends LivingEntityRenderer<Player, VillagerModel<Player>> {

    public VillagerPlayerRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5f);
        this.addLayer(new CrossedArmsItemLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new VillagerArmorLayer(this, new VillagerArmorModel(context.bakeLayer(VillagerArmorLayer.LAYER_LOCATION))));
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
        if (HXYAHelper.isHeiMao(player)) {
            return MAO;
        } else if (HXYAHelper.isUncle(player)) {
            return UNCLE;
        } else if (HXYAHelper.isCen(player)) {
            return CEN;
        } else if (HXYAHelper.isMelor(player)) {
            return MELOR;
        }
        return KAMU;
    }
}
