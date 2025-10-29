package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation;

import dev.behindthescenery.btsengineconcurrent.ReflectionsUtils;
import dev.behindthescenery.btsengineconcurrent.common.profiler.BtsProfilerSettings;
import dev.behindthescenery.btsengineconcurrent.common.profiler.BtsProfilerUtils;
import dev.behindthescenery.btsengineconcurrent.common.utils.MinecraftObjectsCache;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.SectionPos;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(ChunkGenerator.class)
public abstract class MixinChunkGenerator$BiomeDecotration {

    @Unique
    private final ChunkGenerator bts$chunkGenerator = ReflectionsUtils.cast(this);

    @Shadow
    @Final
    protected BiomeSource biomeSource;

    @Shadow
    @Final
    private Function<Holder<Biome>, BiomeGenerationSettings> generationSettingsGetter;

    @Shadow
    private static BoundingBox getWritableArea(ChunkAccess chunk) {
        throw new NotImplementedException();
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/biome/BiomeSource;Ljava/util/function/Function;)V", at = @At("RETURN"))
    public void bts$init(CallbackInfo ci) {
        MinecraftObjectsCache.GENERATOR.resetCache();
    }

    /**
     * @author Sixik
     * @reason Disabled. Not needed
     */
    @Overwrite
    public void validate() { }

    /**
     * @author Sixik
     * @reason Removed unnecessary Supplier and Lazy operations. We also use the cache for structures and features.
     */
    @Overwrite
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        final String name = "Apply Biome Decoration";

        BtsProfilerUtils.startZone(name, BtsProfilerSettings.Type.WorldGen);
        final ChunkPos chunkpos = chunk.getPos();
        if (SharedConstants.debugVoidTerrain(chunkpos)) return;

        final MinecraftObjectsCache.Generator cache = MinecraftObjectsCache.GENERATOR
                .updateRegistryCache(biomeSource, generationSettingsGetter, level.registryAccess());

        final SectionPos chunkSectionPos = SectionPos.of(chunkpos, level.getMinSection());
        final BlockPos chunkBlockPos = chunkSectionPos.origin();
        final Supplier<String> crashWhenLoadStructure = () -> "When generation structure";
        final Supplier<String> crashWhenFeaturePlaced = () -> "When generation feature";

        final List<List<Structure>> structureCache = cache.getStructuresByStep();
        final List<FeatureSorter.StepFeatureData> featurePerStepList = cache.getFeaturePerStep();

        final WorldgenRandom worldgenrandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.generateUniqueSeed()));
        final long decorationSeed = worldgenrandom.setDecorationSeed(level.getSeed(), chunkBlockPos.getX(), chunkBlockPos.getZ());

        final Set<Holder<Biome>> biomesHolderSet = new ObjectArraySet<>();

        final ChunkPos center = chunkSectionPos.chunk();
        final int chunkGetterRadius = 1;

        for (int dx = -chunkGetterRadius; dx < chunkGetterRadius; dx++) {
            for (int dz = -chunkGetterRadius; dz < chunkGetterRadius; dz++) {
                for (LevelChunkSection section : level.getChunk(center.x + dx, center.z + dz).getSections()) {
                    section.getBiomes().getAll(biomesHolderSet::add);
                }
            }
        }

        biomesHolderSet.retainAll(this.biomeSource.possibleBiomes());

        final int featurePerStepSize = cache.getFeaturePerStepSize();

        try {
            final BoundingBox writableArea = getWritableArea(chunk);
            final int maxFeaturePerStep = Math.max(GenerationStep.Decoration.values().length, featurePerStepSize);

            for (int step = 0; step < maxFeaturePerStep; ++step) {
                int featureIndex = 0;

                if (structureManager.shouldGenerateStructures()) {
                    worldgenrandom.setFeatureSeed(decorationSeed, featureIndex, step);
                    for (Structure structure : structureCache.get(step)) {
                        try {
                            level.setCurrentlyGenerating(crashWhenLoadStructure);
                            structureManager.startsForStructure(chunkSectionPos, structure).forEach((structureStart) ->
                                    structureStart.placeInChunk(level, structureManager, bts$chunkGenerator,
                                            worldgenrandom, writableArea, chunkpos));
                        } catch (Exception exception) {
                            CrashReport crashReport = CrashReport.forThrowable(exception, "Feature placement");
                            crashReport.addCategory("Feature").setDetail("Description", crashWhenLoadStructure::get);
                            throw new ReportedException(crashReport);
                        }

                        ++featureIndex;
                    }
                }

                if (step < featurePerStepSize) {
                    final IntSet intset = new IntArraySet();

                    for (Holder<Biome> holder : biomesHolderSet) {
                        final List<HolderSet<PlacedFeature>> list1 = this.generationSettingsGetter.apply(holder).features();
                        if (step < list1.size()) {
                            final HolderSet<PlacedFeature> holderset = list1.get(step);
                            final FeatureSorter.StepFeatureData featureData = featurePerStepList.get(step);
                            holderset.stream().map(Holder::value).forEach((feature) ->
                                    intset.add(featureData.indexMapping().applyAsInt(feature)));
                        }
                    }

                    final int instetSize = intset.size();
                    final int[] aint = intset.toIntArray();
                    Arrays.sort(aint);
                    final FeatureSorter.StepFeatureData featuresorter$stepfeaturedata = featurePerStepList.get(step);

                    for (int k1 = 0; k1 < instetSize; ++k1) {
                        final int l1 = aint[k1];

                        final PlacedFeature placedfeature = featuresorter$stepfeaturedata.features().get(l1);
                        worldgenrandom.setFeatureSeed(decorationSeed, l1, step);

                        try {
                            level.setCurrentlyGenerating(crashWhenFeaturePlaced);
                            placedfeature.placeWithBiomeCheck(level, bts$chunkGenerator, worldgenrandom, chunkBlockPos);
                        } catch (Exception exception1) {
                            CrashReport crashReport = CrashReport.forThrowable(exception1, "Feature placement");
                            crashReport.addCategory("Feature").setDetail("Description", crashWhenFeaturePlaced::get);
                            throw new ReportedException(crashReport);
                        }
                    }
                }
            }

            level.setCurrentlyGenerating(null);
        } catch (Exception exception2) {
            CrashReport crashreport = CrashReport.forThrowable(exception2, "Biome decoration");
            crashreport.addCategory("Generation").setDetail("CenterX", chunkpos.x).setDetail("CenterZ", chunkpos.z).setDetail("Decoration Seed", decorationSeed);
            throw new ReportedException(crashreport);
        }
        BtsProfilerUtils.endZone(name, BtsProfilerSettings.Type.WorldGen);
    }

    /**
     * @author Sixik
     * @reason Use cache for spawn entity
     */
    @Overwrite
    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(
            Holder<Biome> biome, StructureManager structureManager, MobCategory category, BlockPos pos) {

        for (Map.Entry<Structure, LongSet> entry : structureManager.getAllStructuresAt(pos).entrySet()) {
            final Structure struct = entry.getKey();
            final StructureSpawnOverride override = struct.spawnOverrides().get(category);
            if (override != null) {
                return MinecraftObjectsCache.MOB_SPAWN.getOrCreate(biome, category, struct, k -> override.spawns());
            }
        }


        return MinecraftObjectsCache.MOB_SPAWN.getOrCreate(biome, category, null, k -> biome.value().getMobSettings().getMobs(category));
    }
}
