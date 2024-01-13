package cn.ussshenzhou.villager.entity.fakeplayer.events;


import cn.ussshenzhou.villager.entity.fakeplayer.FalsePlayer;
import net.minecraft.world.entity.player.Player;

public class BotKilledByPlayerEvent {

    // eventually also call this event for deaths from other damage causes within combat time
    // (like hitting the ground too hard)

    private final FalsePlayer bot;
    private final Player player;

    public BotKilledByPlayerEvent(FalsePlayer bot, Player player) {
        this.bot = bot;
        this.player = player;
    }

    public FalsePlayer getBot() {
        return bot;
    }

    public Player getPlayer() {
        return player;
    }
}
