package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.noise.density_functions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions.WeirdScaledSampler")
public class MixinWeirdScaledSampler {

    @Redirect(method = "transform", at = @At(value = "INVOKE", target = "Ljava/lang/Math;abs(D)D"))
    private static double redirect$abs(double a) {
        return (a <= 0.0D) ? 0.0D - a : a;
    }
}
