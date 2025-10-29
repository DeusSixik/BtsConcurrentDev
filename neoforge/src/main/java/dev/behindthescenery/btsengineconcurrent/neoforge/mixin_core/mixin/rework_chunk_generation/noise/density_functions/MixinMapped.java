package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.noise.density_functions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions.Mapped")
public class MixinMapped {

    @Redirect(method = "transform(Lnet/minecraft/world/level/levelgen/DensityFunctions$Mapped$Type;D)D", at = @At(value = "INVOKE", target = "Ljava/lang/Math;abs(D)D"))
    private static double redirect$abs(double a) {
        return (a <= 0.0D) ? 0.0D - a : a;
    }

    @Redirect(method = "transform(Lnet/minecraft/world/level/levelgen/DensityFunctions$Mapped$Type;D)D", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(DDD)D"))
    private static double redirect$clamp(double value, double min, double max) {
        return value < min ? min : (value > max ? max : value);
    }
}
