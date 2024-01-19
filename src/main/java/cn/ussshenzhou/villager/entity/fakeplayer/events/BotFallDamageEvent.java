package cn.ussshenzhou.villager.entity.fakeplayer.events;

import cn.ussshenzhou.villager.entity.fakeplayer.FalsePlayer;
import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import java.util.List;

/**
 * @author USS_Shenzhou
 * <br/>This file is modified from <a href="https://github.com/HorseNuggets/TerminatorPlus">TerminatorPlus</a> under EPL-2.0 license, and can be distributed under EPL-2.0 license only.
 */
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
