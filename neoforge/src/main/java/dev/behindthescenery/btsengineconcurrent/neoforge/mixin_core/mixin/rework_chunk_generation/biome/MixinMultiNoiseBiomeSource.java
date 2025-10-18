package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.biome;

import dev.behindthescenery.btsengineconcurrent.common.chunk_generation.biome.FastBiome;
import dev.behindthescenery.btsengineconcurrent.common.chunk_generation.biome.FastSampler;
import dev.behindthescenery.btsengineconcurrent.common.path.rework_chunk_generation.biome.PathBiomeSource$BiomeGetter;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * It requires serious processing, as in 90% of cases it only makes it worse.
 */
@Deprecated
@Mixin(MultiNoiseBiomeSource.class)
public abstract class MixinMultiNoiseBiomeSource implements PathBiomeSource$BiomeGetter {

    @Unique
    private static final float[] BTS_NOISE_BUF = new float[6];

    @Unique
    private static final ThreadLocal<FastSampler> BTS_LOCAL_SAMPLER = new ThreadLocal<>();

    @Unique
    private List<FastBiome> bts_fast_biomes;

    @Shadow
    protected abstract Climate.ParameterList<Holder<Biome>> parameters();

    /**
     * @author Sixik
     * @reason Used fast cache and sampler for effectivity search biomes
     */
    @Overwrite
    public @NotNull Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler sampler) {
        FastSampler fastSampler = BTS_LOCAL_SAMPLER.get();
        if (fastSampler == null) {
            fastSampler = new FastSampler(sampler);
            BTS_LOCAL_SAMPLER.set(fastSampler);
        }

        float[] noise = BTS_NOISE_BUF;
        fastSampler.sample(i, j, k, noise);

        final float t = noise[0];
        final float h = noise[1];
        final float c = noise[2];
        final float e = noise[3];
        final float d = noise[4];
        final float w = noise[5];

        float bestDist = Float.MAX_VALUE;
        Holder<Biome> best = null;

        for (FastBiome fb : this.bts$getFastBiomesOrCreate()) {
            float[] p = fb.params();
            float dist =
                    bts$sq(t - p[0]) +
                            bts$sq(h - p[1]) +
                            bts$sq(c - p[2]) +
                            bts$sq(e - p[3]) +
                            bts$sq(d - p[4]) +
                            bts$sq(w - p[5]);
            if (dist < bestDist) {
                bestDist = dist;
                best = fb.biome();
                if (dist == 0.0f) break;
            }
        }

        return Objects.requireNonNull(best);
    }

    /**
     * @author Sixik
     * @reason Use cached biomes data
     */
    @Overwrite
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.bts$getFastBiomesOrCreate().stream().map(FastBiome::biome);
    }

    @Unique
    private List<FastBiome> bts$getFastBiomesOrCreate() {
        if(bts_fast_biomes == null) {
            Climate.ParameterList<Holder<Biome>> paramList = this.parameters();
            this.bts_fast_biomes = paramList.values().stream()
                    .map(pair -> new FastBiome(pair.getSecond(), bts$unpackParameterPoint(pair.getFirst())))
                    .toList();
        }
        return bts_fast_biomes;
    }

    @Unique
    private static float bts$sq(float f) { return f * f; }

    @Unique
    private static float[] bts$unpackParameterPoint(Climate.ParameterPoint p) {
        return new float[]{
                Climate.unquantizeCoord(p.temperature().min()),
                Climate.unquantizeCoord(p.humidity().min()),
                Climate.unquantizeCoord(p.continentalness().min()),
                Climate.unquantizeCoord(p.erosion().min()),
                Climate.unquantizeCoord(p.depth().min()),
                Climate.unquantizeCoord(p.weirdness().min())
        };
    }
}
