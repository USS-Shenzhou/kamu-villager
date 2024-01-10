package cn.ussshenzhou.villager.network;

import cn.ussshenzhou.t88.network.annotation.Consumer;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import cn.ussshenzhou.villager.VillagerManager;
import cn.ussshenzhou.villager.entity.VillagerVillager;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.PlayNetworkDirection;

/**
 * @author USS_Shenzhou
 */
@NetPacket
public class CommandVillagerPacket {
    public final VillagerVillager.Command command;

    public CommandVillagerPacket(VillagerVillager.Command command) {
        this.command = command;
    }

    @Decoder
    public CommandVillagerPacket(FriendlyByteBuf buf) {
        command = buf.readEnum(VillagerVillager.Command.class);
    }

    @Encoder
    public void writeToNet(FriendlyByteBuf buf) {
        buf.writeEnum(command);
    }

    @Consumer
    public void handler(NetworkEvent.Context context) {
        if (context.getDirection().equals(PlayNetworkDirection.PLAY_TO_SERVER)) {
            VillagerManager.command(context.getSender(), command);
        }
        context.setPacketHandled(true);
    }
}
