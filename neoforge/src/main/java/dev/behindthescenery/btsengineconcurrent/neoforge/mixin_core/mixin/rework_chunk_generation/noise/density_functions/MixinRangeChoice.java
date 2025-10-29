package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.noise.density_functions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions.RangeChoice")
public class MixinRangeChoice {

    @Redirect(method = "minValue", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(DD)D"))
    private static double redirect$min(double a, double b) {
        return (a <= b) ? a : b;
    }

    @Redirect(method = "maxValue", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(DD)D"))
    private static double redirect$max(double a, double b) {
        return (a >= b) ? a : b;
    }
}
