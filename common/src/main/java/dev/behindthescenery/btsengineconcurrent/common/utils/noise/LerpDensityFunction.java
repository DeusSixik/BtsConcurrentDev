package dev.behindthescenery.btsengineconcurrent.common.utils.noise;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.NotNull;

public record LerpDensityFunction(DensityFunction delta, DensityFunction start, DensityFunction end, double minValue, double maxValue) implements DensityFunction {
    public static final KeyDispatchDataCodec<LerpDensityFunction> CODEC_HOLDER =
            CustomDensityFunctionTypes.holderOf(LerpDensityFunction::create,
                    LerpDensityFunction::delta, LerpDensityFunction::start,
                    LerpDensityFunction::end);

    public static LerpDensityFunction create(DensityFunction argument1, DensityFunction argument2, DensityFunction argument3) {
        final double min1 = argument1.minValue();
        final double min2 = argument2.minValue();
        final double min3 = argument3.minValue();
        final double max1 = argument1.maxValue();
        final double max2 = argument2.maxValue();
        final double max3 = argument3.maxValue();
        double minResult, maxResult;

        minResult = Math.min(min1, min2);
        minResult = Math.min(minResult, min1 + (min2 - min1) * min3);
        minResult = Math.min(minResult, min1 + (min2 - min1) * max3);

        maxResult = Math.max(max1, max2);
        maxResult = Math.max(maxResult, max1 + (max2 - max1) * min3);
        maxResult = Math.max(maxResult, max1 + (max2 - max1) * max3);

        return new LerpDensityFunction(argument1, argument2, argument3, minResult, maxResult);
    }

    @Override
    public double compute(FunctionContext pos) {
        final double s = start.compute(pos);
        final double e = end.compute(pos);
        final double p = delta.compute(pos);

        return s + (e - s) * p;
    }

    @Override
    public void fillArray(double[] densities, ContextProvider applier) {
        final double[] s = new double[densities.length];
        final double[] e = new double[densities.length];
        final double[] p = new double[densities.length];

        start.fillArray(s, applier);
        end.fillArray(e, applier);
        delta.fillArray(p, applier);

        for (int i = 0; i < densities.length; i++) {
            final double v = s[i];
            densities[i] = v + (e[i] - v) * p[i];
        }
    }

    @Override
    public @NotNull DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new LerpDensityFunction(delta.mapAll(visitor), start.mapAll(visitor), end.mapAll(visitor), minValue, maxValue));
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC_HOLDER;
    }
}
