package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.noise;

import com.mojang.datafixers.util.Pair;
import dev.behindthescenery.btsengineconcurrent.common.utils.MinecraftObjectsCache;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PerlinNoise.class)
public abstract class PerlinNoiseMixin {

    @Shadow
    @Final
    private double lowestFreqValueFactor;

    @Shadow
    @Final
    private ImprovedNoise[] noiseLevels;

    @Unique private double[] bts_frequencies_cache;
    @Unique private double[] bts_amplitudes_input_scaled_cache;
    @Unique private double[] bts_amplitudes_value_scaled_cache;


    @Shadow
    public static double wrap(double value) {
        throw new RuntimeException();
    }

    @Shadow
    @Final
    private DoubleList amplitudes;

    @Shadow
    @Final
    private double lowestFreqInputFactor;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/synth/PerlinNoise;edgeValue(D)D"))
    public double bts$init$redirect$edgeValue(PerlinNoise instance, double multiplier) {
        return bts$original$edgeValue(multiplier);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void bts$init(RandomSource random, Pair octavesAndAmplitudes, boolean useNewFactory, CallbackInfo ci) {
        bts_frequencies_cache = new double[this.noiseLevels.length];
        bts_amplitudes_input_scaled_cache = new double[this.noiseLevels.length];
        bts_amplitudes_value_scaled_cache = new double[this.noiseLevels.length];

        double e = this.lowestFreqInputFactor;
        double f = this.lowestFreqValueFactor;
        for (int i = 0; i < this.noiseLevels.length; i++) {

            final double d = this.amplitudes.getDouble(i);

            bts_frequencies_cache[i] = e;
            bts_amplitudes_input_scaled_cache[i] = d * f;
            bts_amplitudes_value_scaled_cache[i] = d * e;
            e *= 2d;
            f /= 2f;
        }
    }

    /**
     * @author Sixik
     * @reason Using cached values
     */
    @Deprecated
    @Overwrite
    public double getValue(double x, double y, double z, double yScale, double yMax, boolean useFixedY) {
        double d = 0.0;
        for (int i = 0; i < this.noiseLevels.length; i++) {
            final ImprovedNoise improvedNoise = this.noiseLevels[i];
            if(improvedNoise == null) continue;

            final double e = bts_frequencies_cache[i];
            final double g = improvedNoise.noise(wrap(x * e),
                    useFixedY ? -improvedNoise.yo
                            : wrap(y * e), wrap(z * e), yScale * e, yMax * e);
            d += bts_amplitudes_input_scaled_cache[i] * g;
        }

        return d;
    }

    /**
     * @author Sixik
     * @reason Using cached values
     */
    @Overwrite
    private double edgeValue(double multiplier) {
        double d = 0.0d;

        for(int i = 0; i < this.noiseLevels.length; ++i) {
            final ImprovedNoise improvedNoise = this.noiseLevels[i];
            if(improvedNoise == null) continue;

            d += bts_amplitudes_value_scaled_cache[i] * multiplier;
        }

        return d;
    }


    @Unique
    private double bts$original$edgeValue(double multiplier) {
        double d = 0.0;
        double e = this.lowestFreqValueFactor;

        for(int i = 0; i < this.noiseLevels.length; ++i) {
            ImprovedNoise improvedNoise = this.noiseLevels[i];
            if (improvedNoise != null) {
                d += this.amplitudes.getDouble(i) * multiplier * e;
            }

            e /= 2.0;
        }

        return d;
    }
}
