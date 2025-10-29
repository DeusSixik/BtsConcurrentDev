package dev.behindthescenery.btsengineconcurrent.common.utils;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.IntFunction;

public class MinecraftObjectsCache {

    public static final Generator GENERATOR = new Generator();
    public static final MobSpawn MOB_SPAWN = new MobSpawn();
    public static final OreGeneration ORE_GENERATION = new OreGeneration();

    public static class Generator {

        protected boolean ready = false;
        protected List<List<Structure>> structures_cached;
        protected List<FeatureSorter.StepFeatureData> feature_per_step_cached;
        protected int feature_per_step_size;

        private BiomeSource lastBiomeSource;
        private RegistryAccess lastRegistryAccess;

        protected Generator() { }


        public void resetCache() {
            ready = false;
        }

        public Generator updateRegistryCache(BiomeSource bs, Function<Holder<Biome>, BiomeGenerationSettings> g, RegistryAccess ra) {
            if (!ready || this.lastBiomeSource != bs || this.lastRegistryAccess != ra) {
                synchronized (this) {
                    rebuild(bs, g, ra);
                }
            }
            return this;
        }

        protected void rebuild(BiomeSource biomeSource, Function<Holder<Biome>, BiomeGenerationSettings> generationSettingsGetter, RegistryAccess registryAccess) {
            this.lastBiomeSource = biomeSource;
            this.lastRegistryAccess = registryAccess;

            final Registry<Structure> structure = registryAccess.registryOrThrow(Registries.STRUCTURE);
            final int decorationSize = GenerationStep.Decoration.values().length;
            final List<List<Structure>> structures_by_step = Lists.newArrayListWithCapacity(decorationSize);

            for (int i = 0; i < decorationSize; i++)
                structures_by_step.add(new ArrayList<>());

            structure.stream().forEach(arg -> structures_by_step.get(arg.step().ordinal()).add(arg));
            structures_cached = structures_by_step;
            ready = true;

            createFeatureCache(biomeSource, generationSettingsGetter);
        }

        protected void createFeatureCache(BiomeSource biomeSource, Function<Holder<Biome>, BiomeGenerationSettings> generationSettingsGetter) {
            feature_per_step_cached = FeatureSorter.buildFeaturesPerStep(List.copyOf(biomeSource.possibleBiomes()),
                    arg -> generationSettingsGetter.apply(arg)
                            .features(), true);
            feature_per_step_size = feature_per_step_cached.size();
        }

        public List<List<Structure>> getStructuresByStep() {
            return structures_cached;
        }

        public List<FeatureSorter.StepFeatureData> getFeaturePerStep() {
            return feature_per_step_cached;
        }

        public int getFeaturePerStepSize() {
            return feature_per_step_size;
        }
    }

    public static class MobSpawn {
        protected final Map<MobCacheKey, WeightedRandomList<MobSpawnSettings.SpawnerData>> mobCache =
                new ConcurrentHashMap<>();

        protected MobSpawn() {

        }

        public WeightedRandomList<MobSpawnSettings.SpawnerData> getOrCreate(Holder<Biome> biome, MobCategory category, @Nullable Structure structure,
                                                                            Function<MobCacheKey, WeightedRandomList<MobSpawnSettings.SpawnerData>> function) {
            return mobCache.computeIfAbsent(new MinecraftObjectsCache.MobCacheKey(biome, category, structure), function);
        }
    }

    public record MobCacheKey(Holder<Biome> biome, MobCategory category, @Nullable Structure structure) {}

    public static class OreGeneration {

        protected static final IntFunction<BitSet> BIT_SET_CONSTRUCTOR = BitSet::new;

        protected ThreadLocal<Int2ObjectOpenHashMap<BitSet>> BITSETS = ThreadLocal.withInitial(Int2ObjectOpenHashMap::new);

        protected OreGeneration() {}

        public BitSet getOrCreate(int bits) {
            final BitSet bitSet = BITSETS.get().computeIfAbsent(bits, BIT_SET_CONSTRUCTOR);
            bitSet.clear();
            return bitSet;
        }
    }
}
