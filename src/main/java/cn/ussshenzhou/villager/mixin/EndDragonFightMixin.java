package cn.ussshenzhou.villager.mixin;

import cn.ussshenzhou.villager.entity.fakeplayer.FalsePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.objectweb.asm.Opcodes;
import org.openjdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

/**
 * @author USS_Shenzhou
 */
@Mixin(EndDragonFight.class)
public class EndDragonFightMixin {

    @Redirect(method = "<init>(Lnet/minecraft/server/level/ServerLevel;JLnet/minecraft/world/level/dimension/end/EndDragonFight$Data;Lnet/minecraft/core/BlockPos;)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;validPlayer:Ljava/util/function/Predicate;", opcode = Opcodes.PUTFIELD))
    private void ignoreFalsePlayer(EndDragonFight instance, Predicate<Entity> value) {
        instance.validPlayer = value.and(entity -> {
            if (entity instanceof Player player) {
                return FalsePlayer.isRealPlayer(player);
            } else {
                return true;
            }
        });
    }
}
