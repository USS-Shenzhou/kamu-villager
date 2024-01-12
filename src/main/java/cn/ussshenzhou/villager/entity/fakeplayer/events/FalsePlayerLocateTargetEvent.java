package cn.ussshenzhou.villager.entity.fakeplayer.events;

import cn.ussshenzhou.villager.entity.fakeplayer.FalsePlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

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
