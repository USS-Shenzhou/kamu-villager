package cn.ussshenzhou.villager;

import cn.ussshenzhou.villager.entity.ModEntityTypes;
import cn.ussshenzhou.villager.item.ModItems;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;


/**
 * @author USS_Shenzhou
 */
@Mod(Villager.MOD_ID)
public class Villager {
    public static final String MOD_ID = "villager";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Villager(IEventBus modEventBus) {
        ModDataAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntityTypes.ENTITY_TYPE.register(modEventBus);
    }
}
