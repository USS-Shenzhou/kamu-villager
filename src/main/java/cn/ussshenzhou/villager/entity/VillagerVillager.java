package cn.ussshenzhou.villager.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;

/**
 * @author USS_Shenzhou
 */
public class VillagerVillager extends Villager {

    public VillagerVillager(EntityType<? extends Villager> pEntityType, Level pLevel) {
        //fixme
        super(pEntityType, pLevel);
    }

    @Override
    protected void registerGoals() {
        //TODO
    }

    @Override
    protected Brain.Provider<Villager> brainProvider() {
        return Brain.provider(ImmutableList.of(), ImmutableList.of());
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
        return this.brainProvider().makeBrain(pDynamic);
    }

    @Override
    public void refreshBrain(ServerLevel pServerLevel) {
    }
}
