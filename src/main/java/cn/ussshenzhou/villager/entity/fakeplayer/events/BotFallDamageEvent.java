package cn.ussshenzhou.villager.entity.fakeplayer.events;

import cn.ussshenzhou.villager.entity.fakeplayer.FalsePlayer;
import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import java.util.List;

public class BotFallDamageEvent extends Event implements ICancellableEvent {

    private final FalsePlayer falsePlayer;
    private final List<BlockPos> standingOn;

    public BotFallDamageEvent(FalsePlayer falsePlayer, List<BlockPos> standingOn) {
        this.falsePlayer = falsePlayer;
        this.standingOn = standingOn;
    }

    public FalsePlayer getFalsePlayer() {
        return falsePlayer;
    }
    
    public List<BlockPos> getStandingOn() {
    	return standingOn;
    }
}
