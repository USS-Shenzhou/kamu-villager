package cn.ussshenzhou.villager;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * @author USS_Shenzhou
 */
public class ModDataAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Villager.MOD_ID);

    public static final Supplier<AttachmentType<ProfessionContainer>> PROFESSION = ATTACHMENT_TYPES.register("profession",
            () -> AttachmentType.serializable(() -> new ProfessionContainer(ProfessionContainer.Profession.NITWIT)).build()
    );
}
