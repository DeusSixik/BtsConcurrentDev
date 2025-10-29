package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.noise.density_functions;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions.Clamp")
public abstract class MixinClampFunction implements DensityFunctions.PureTransformer {

    @Shadow
    @Final
    private DensityFunction input;

    @Shadow
    @Final
    private double minValue;

    @Shadow
    @Final
    private double maxValue;

    @Override
    public double compute(FunctionContext context) {
        final double v = input.compute(context);
        if (v < minValue) return minValue;
        if (v > maxValue) return maxValue;
        return v;
    }

    @Override
    public void fillArray(double[] a, DensityFunction.ContextProvider applier) {
        input.fillArray(a, applier);
        final int n = a.length;
        final double lo = minValue, hi = maxValue;
        for (int i = 0; i < n; i++) {
            final double v = a[i];
            a[i] = v < lo ? lo : (v > hi ? hi : v);
        }
    }
}
