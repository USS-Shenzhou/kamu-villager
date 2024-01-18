package cn.ussshenzhou.villager.network;

import cn.ussshenzhou.t88.network.annotation.Consumer;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import cn.ussshenzhou.villager.HXYAHelper;
import cn.ussshenzhou.villager.VillagerManager;
import cn.ussshenzhou.villager.entity.Command;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.PlayNetworkDirection;

/**
 * @author USS_Shenzhou
 */
@NetPacket
public class MelorTradeCarrotPacket {

    public MelorTradeCarrotPacket() {
    }

    @Decoder
    public MelorTradeCarrotPacket(FriendlyByteBuf buf) {
    }

    @Encoder
    public void writeToNet(FriendlyByteBuf buf) {
    }

    @Consumer
    public void handler(NetworkEvent.Context context) {
        if (context.getDirection().equals(PlayNetworkDirection.PLAY_TO_SERVER)) {
            var melor = context.getSender();
            if (melor != null && HXYAHelper.isMelor(melor)) {
                melor.hurt(melor.damageSources().mobAttack(new LivingEntity(EntityType.BAT, melor.level()) {
                    @Override
                    public Iterable<ItemStack> getArmorSlots() {
                        return null;
                    }

                    @Override
                    public ItemStack getItemBySlot(EquipmentSlot pSlot) {
                        return null;
                    }

                    @Override
                    public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack) {

                    }

                    @Override
                    public HumanoidArm getMainArm() {
                        return null;
                    }

                    @Override
                    public Component getDisplayName() {
                        return Component.literal("萝卜");
                    }
                }), 1);
                var a = melor.serverLevel().getServer().getAdvancements().get(new ResourceLocation("minecraft:story/melor"));
                if (a != null) {
                    melor.getAdvancements().award(a, "custom");
                }
            }
        }
        context.setPacketHandled(true);
    }
}
