package cn.ussshenzhou.villager.entity.fakeplayer.events;


import cn.ussshenzhou.villager.entity.fakeplayer.FalsePlayer;
import net.minecraft.world.entity.player.Player;

/**
 * @author USS_Shenzhou
 * <br/>This file is modified from <a href="https://github.com/HorseNuggets/TerminatorPlus">TerminatorPlus</a> under EPL-2.0 license, and can be distributed under EPL-2.0 license only.
 */
public class BotDamageByPlayerEvent {

    private final FalsePlayer bot;
    private final Player player;

    private float damage;

    private boolean cancelled;

    public BotDamageByPlayerEvent(FalsePlayer bot, Player player, float damage) {
        this.bot = bot;
        this.player = player;
        this.damage = damage;
    }

    public FalsePlayer getBot() {
        return bot;
    }

    public Player getPlayer() {
        return player;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
