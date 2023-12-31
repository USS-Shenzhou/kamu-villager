package cn.ussshenzhou.villager;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * @author USS_Shenzhou
 */
public class ModDataAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Villager.MOD_ID);

    public static final Supplier<AttachmentType<Profession>> PROFESSION = ATTACHMENT_TYPES.register("profession",
            () -> AttachmentType.builder(() -> Profession.NITWIT)
                    .serialize(new Codec<>() {
                        @Override
                        public <T> DataResult<Pair<Profession, T>> decode(DynamicOps<T> ops, T input) {
                            return DataResult.success(new Pair<>(Profession.values()[ops.getNumberValue(input).get().orThrow().intValue()], input));
                        }

                        @Override
                        public <T> DataResult<T> encode(Profession input, DynamicOps<T> ops, T prefix) {
                            return DataResult.success(ops.createInt(input.ordinal()));
                        }
                    })
                    .build()
    );
}
