package dev.behindthescenery.btsengineconcurrent.common.chunk_generation.biome;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.biome.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class FastMultiNoiseBiomeSource extends BiomeSource {

    private static final MapCodec<Holder<Biome>> ENTRY_CODEC = Biome.CODEC.fieldOf("biome");
    public static final MapCodec<Climate.ParameterList<Holder<Biome>>> DIRECT_CODEC =
            Climate.ParameterList.codec(ENTRY_CODEC).fieldOf("biomes");

    private static final MapCodec<Holder<MultiNoiseBiomeSourceParameterList>> PRESET_CODEC =
            MultiNoiseBiomeSourceParameterList.CODEC.fieldOf("preset");

    public static final MapCodec<FastMultiNoiseBiomeSource> CODEC =
            Codec.mapEither(DIRECT_CODEC, PRESET_CODEC)
                    .xmap(FastMultiNoiseBiomeSource::new, src -> src.parameters);

    private final Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> parameters;
    private List<FastBiome> fastBiomes;

    private static final ThreadLocal<FastSampler> LOCAL_SAMPLER = new ThreadLocal<>();

    public static FastMultiNoiseBiomeSource createFromList(Climate.ParameterList<Holder<Biome>> parameters) {
        return new FastMultiNoiseBiomeSource(Either.left(parameters));
    }

    public static FastMultiNoiseBiomeSource createFromPreset(Holder<MultiNoiseBiomeSourceParameterList> parameters) {
        return new FastMultiNoiseBiomeSource(Either.right(parameters));
    }

    public FastMultiNoiseBiomeSource(Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> parameters) {
        this.parameters = parameters;
    }

    private Climate.ParameterList<Holder<Biome>> parameters() {
        return this.parameters.map(l -> l, r -> r.value().parameters());
    }

    private List<FastBiome> getFastBiomesOrCreate() {
        if(fastBiomes == null) {
            Climate.ParameterList<Holder<Biome>> paramList = this.parameters();
            this.fastBiomes = paramList.values().stream()
                    .map(pair -> new FastBiome(pair.getSecond(), unpackParameterPoint(pair.getFirst())))
                    .toList();
        }
        return fastBiomes;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.getFastBiomesOrCreate().stream().map(FastBiome::biome);
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    // --- Fast-path implementation ---
    private static final float[] NOISE_BUF = new float[6];

    @Override
    public @NotNull Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler sampler) {
        FastSampler fastSampler = LOCAL_SAMPLER.get();
        if (fastSampler == null) {
            fastSampler = new FastSampler(sampler);
            LOCAL_SAMPLER.set(fastSampler);
        }

        float[] noise = NOISE_BUF;
        fastSampler.sample(i, j, k, noise);

        final float t = noise[0];
        final float h = noise[1];
        final float c = noise[2];
        final float e = noise[3];
        final float d = noise[4];
        final float w = noise[5];

        float bestDist = Float.MAX_VALUE;
        Holder<Biome> best = null;

        for (FastBiome fb : this.getFastBiomesOrCreate()) {
            float[] p = fb.params();
            float dist =
                    sq(t - p[0]) +
                            sq(h - p[1]) +
                            sq(c - p[2]) +
                            sq(e - p[3]) +
                            sq(d - p[4]) +
                            sq(w - p[5]);
            if (dist < bestDist) {
                bestDist = dist;
                best = fb.biome();
                if (dist == 0.0f) break;
            }
        }

        return Objects.requireNonNull(best);
    }

    private static float sq(float f) { return f * f; }

    private static float[] unpackParameterPoint(Climate.ParameterPoint p) {
        return new float[]{
                Climate.unquantizeCoord(p.temperature().min()),
                Climate.unquantizeCoord(p.humidity().min()),
                Climate.unquantizeCoord(p.continentalness().min()),
                Climate.unquantizeCoord(p.erosion().min()),
                Climate.unquantizeCoord(p.depth().min()),
                Climate.unquantizeCoord(p.weirdness().min())
        };
    }

    public boolean stable(ResourceKey<MultiNoiseBiomeSourceParameterList> key) {
        Optional<Holder<MultiNoiseBiomeSourceParameterList>> opt = this.parameters.right();
        return opt.isPresent() && opt.get().is(key);
    }

    @VisibleForDebug
    public Holder<Biome> getNoiseBiomeDebug(Climate.TargetPoint targetPoint) {
        // обратная совместимость для дебага
        return this.getNoiseBiome(
                (int) Climate.unquantizeCoord(targetPoint.continentalness()),
                (int) Climate.unquantizeCoord(targetPoint.erosion()),
                (int) Climate.unquantizeCoord(targetPoint.weirdness()),
                Climate.empty()
        );
    }
}