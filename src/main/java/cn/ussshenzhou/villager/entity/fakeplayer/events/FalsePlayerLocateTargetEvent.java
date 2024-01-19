package cn.ussshenzhou.villager.entity.fakeplayer.events;

import cn.ussshenzhou.villager.entity.fakeplayer.FalsePlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * @author USS_Shenzhou
 * <br/>This file is modified from <a href="https://github.com/HorseNuggets/TerminatorPlus">TerminatorPlus</a> under EPL-2.0 license, and can be distributed under EPL-2.0 license only.
 */
public class FalsePlayerLocateTargetEvent extends Event implements ICancellableEvent {

    //private static final HandlerList handlerList = new HandlerList();
    private FalsePlayer falsePlayer;
    private LivingEntity target;
    private boolean cancelled;

    public FalsePlayerLocateTargetEvent(FalsePlayer falsePlayer, LivingEntity target) {
        this.falsePlayer = falsePlayer;
        this.target = target;
    }

    /*public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }*/


    public FalsePlayer getFalsePlayer() {
        return falsePlayer;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
    }
}
