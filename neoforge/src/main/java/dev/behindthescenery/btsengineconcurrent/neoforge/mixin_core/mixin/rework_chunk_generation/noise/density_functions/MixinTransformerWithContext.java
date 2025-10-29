package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.noise.density_functions;

import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions.TransformerWithContext")
public interface MixinTransformerWithContext {

    @Shadow
    DensityFunction input();

    @Shadow
    double transform(DensityFunction.FunctionContext context, double value);

    /**
     * @author Sixik
     * @reason Optimization Inlining
     */
    @Overwrite
    default void fillArray(double[] densities, DensityFunction.ContextProvider applier) {
        final DensityFunction in = this.input();
        in.fillArray(densities, applier);
        final int n = densities.length;
        for (int i = 0; i < n; i++) {
            final DensityFunction.FunctionContext p = applier.forIndex(i);
            final double v = densities[i];
            densities[i] = this.transform(p, v);
        }
    }
}
