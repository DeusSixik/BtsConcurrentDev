package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.carver;

import dev.behindthescenery.btsengineconcurrent.ReflectionsUtils;
import dev.behindthescenery.btsengineconcurrent.common.chunk_generation.carver.PooledWorldCarverContext;
import dev.behindthescenery.btsengineconcurrent.common.utils.SimpleObjectPool;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class MixinNoiseBasedChunkGenerator$CarverPool extends ChunkGenerator {

    public MixinNoiseBasedChunkGenerator$CarverPool(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Shadow
    protected abstract NoiseChunk createNoiseChunk(ChunkAccess chunk, StructureManager structureManager, Blender blender, RandomState random);

    @Shadow
    @Final
    private Holder<NoiseGeneratorSettings> settings;

    /**
     * @author Sixik
     * @reason Use Carving object pool
     */
    @Overwrite
    public void applyCarvers(WorldGenRegion level,
                             long seed, RandomState random,
                             BiomeManager biomeManager,
                             StructureManager structureManager,
                             ChunkAccess chunk,
                             GenerationStep.Carving step
    ) {
        final BiomeManager biomeManager2 = biomeManager.withDifferentSource((i, j, k) -> this.biomeSource.getNoiseBiome(i, j, k, random.sampler()));
        final WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        final ChunkPos chunkPos = chunk.getPos();
        final NoiseChunk noiseChunk = chunk.getOrCreateNoiseChunk(arg4 -> this.createNoiseChunk((ChunkAccess)arg4, structureManager, Blender.of(level), random));
        final Aquifer aquifer = noiseChunk.aquifer();
        final SimpleObjectPool<PooledWorldCarverContext> objectPool = PooledWorldCarverContext.POOL.get();
        final PooledWorldCarverContext carvingContext = objectPool.alloc();

        try {
            carvingContext.reInit(ReflectionsUtils.cast(this), level.registryAccess(), chunk.getHeightAccessorForGeneration(), noiseChunk, random, this.settings.value().surfaceRule());
            final CarvingMask carvingMask = ((ProtoChunk) chunk).getOrCreateCarvingMask(step);
            for (int j2 = -8; j2 <= 8; ++j2) {
                for (int k2 = -8; k2 <= 8; ++k2) {
                    final ChunkPos chunkPos2 = new ChunkPos(chunkPos.x + j2, chunkPos.z + k2);
                    final ChunkAccess chunkAccess = level.getChunk(chunkPos2.x, chunkPos2.z);
                    final BiomeGenerationSettings biomeGenerationSettings = chunkAccess.carverBiome(() -> this.getBiomeGenerationSettings(this.biomeSource.getNoiseBiome(QuartPos.fromBlock(chunkPos2.getMinBlockX()), 0, QuartPos.fromBlock(chunkPos2.getMinBlockZ()), random.sampler())));
                    final Iterable<Holder<ConfiguredWorldCarver<?>>> iterable = biomeGenerationSettings.getCarvers(step);
                    int l = 0;
                    for (Holder<ConfiguredWorldCarver<?>> holder : iterable) {
                        final ConfiguredWorldCarver<?> configuredWorldCarver = holder.value();
                        worldgenRandom.setLargeFeatureSeed(seed + (long) l, chunkPos2.x, chunkPos2.z);
                        if (configuredWorldCarver.isStartChunk(worldgenRandom)) {
                            configuredWorldCarver.carve(carvingContext, chunk, biomeManager2::getBiome, worldgenRandom, aquifer, chunkPos2, carvingMask);
                        }
                        ++l;
                    }
                }
            }
        } finally {
            objectPool.release(carvingContext);
        }
    }
}
