package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.noise.density_functions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions.Ap2")
public class MixinBinaryOperation {

    @Redirect(method = "compute", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(DD)D"))
    private static double redirect$min(double a, double b) {
        return (a <= b) ? a : b;
    }

    @Redirect(method = "compute", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(DD)D"))
    private static double redirect$max(double a, double b) {
        return (a >= b) ? a : b;
    }

    @Redirect(method = "compute", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(DD)D"))
    private static double redirect$fill$min(double a, double b) {
        return (a <= b) ? a : b;
    }


    @Redirect(method = "compute", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(DD)D"))
    private static double redirect$fill$max(double a, double b) {
        return (a >= b) ? a : b;
    }
}
