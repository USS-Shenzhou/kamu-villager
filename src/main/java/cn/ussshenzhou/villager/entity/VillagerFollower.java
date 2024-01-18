package cn.ussshenzhou.villager.entity;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;

/**
 * @author USS_Shenzhou
 */
public interface VillagerFollower {

    void setCommand(Command command);

    Command getCommand();

    Player getMaster();

    default PathfinderMob getThis() {
        return (PathfinderMob) this;
    }
}
