package cn.ussshenzhou.villager.network;

import cn.ussshenzhou.t88.network.annotation.Consumer;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import cn.ussshenzhou.t88.task.TaskHelper;
import cn.ussshenzhou.villager.ModDataAttachments;
import cn.ussshenzhou.villager.ProfessionContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.PlayNetworkDirection;

import java.util.UUID;

/**
 * @author USS_Shenzhou
 */
@NetPacket
public class ChooseProfessionPacket {
    public final UUID player;
    public final ProfessionContainer.Profession profession;

    public ChooseProfessionPacket(Player player, ProfessionContainer.Profession profession) {
        this.player = player.getUUID();
        this.profession = profession;
    }

    @Decoder
    public ChooseProfessionPacket(FriendlyByteBuf buf) {
        player = buf.readUUID();
        profession = buf.readEnum(ProfessionContainer.Profession.class);
    }

    @Encoder
    public void writeToNet(FriendlyByteBuf buf) {
        buf.writeUUID(player);
        buf.writeEnum(profession);
    }

    @Consumer
    public void handler(NetworkEvent.Context context) {
        if (context.getDirection().equals(PlayNetworkDirection.PLAY_TO_SERVER)) {
            serverHandler();
        } else {
            clientHandler();
        }
        context.setPacketHandled(true);
    }

    public void serverHandler() {
        MinecraftServer minecraftServer = (MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER);
        minecraftServer.getPlayerList().getPlayer(player).getData(ModDataAttachments.PROFESSION).set(profession);
    }

    @OnlyIn(Dist.CLIENT)
    public void clientHandler() {
        TaskHelper.addClientTask(()->{
            var player = Minecraft.getInstance().player;
            if (player.getUUID().equals(this.player)) {
                player.getData(ModDataAttachments.PROFESSION).set(profession);
            }
        },5);
    }
}
