package cn.ussshenzhou.villager.entity;

import cn.ussshenzhou.villager.Villager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPE = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Villager.MOD_ID);

    public static final Supplier<EntityType<VillagerVillager>> VILLAGER_VILLAGER = ENTITY_TYPE.register("villager_villager",
            () -> EntityType.Builder.of(VillagerVillager::new, MobCategory.MISC)
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(10)
                    .build("villager_villager")
    );
}
