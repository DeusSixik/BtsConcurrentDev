package dev.behindthescenery.btsengineconcurrent.common.chunk_generation.surface_system;

import dev.behindthescenery.btsengineconcurrent.common.mixin.SurfaceRulesContextAccess;
import dev.behindthescenery.btsengineconcurrent.common.utils.SimpleObjectPool;
import dev.behindthescenery.btsengineconcurrent.common.utils.SimpleObjectPoolUnSynchronize;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.*;

import java.util.function.Function;

public class SurfaceContextPool extends SurfaceRules.Context {

    private final long DEFAULT = -9223372036854775807L;

    public static final ThreadLocal<SimpleObjectPool<SurfaceContextPool>> POOL = ThreadLocal.withInitial(() ->
            new SimpleObjectPoolUnSynchronize<>(
                    unused -> new SurfaceContextPool(),
                    SurfaceContextPool::updateData,
                    SurfaceContextPool::updateData,
                    512
            ));

    public SurfaceContextPool() {
        super(null, null, null, null, null, null, null);
    }

    public SurfaceContextPool(SurfaceSystem system, RandomState randomState,
                              ChunkAccess chunk, NoiseChunk noiseChunk, Function<BlockPos,
                    Holder<Biome>> biomeGetter, Registry<Biome> registry, WorldGenerationContext context) {
        super(system, randomState, chunk, noiseChunk, biomeGetter, registry, context);
    }

    public void updateData() {
        SurfaceRulesContextAccess access = (SurfaceRulesContextAccess)this;
        access.setChunk(null);
        access.setNoiseChunk(null);
        access.setBiomeGetter(null);
        access.setContext(null);

    }

    public void updateData(SurfaceSystem system, RandomState randomState,
                           ChunkAccess chunk, NoiseChunk noiseChunk, Function<BlockPos,
                    Holder<Biome>> biomeGetter, Registry<Biome> registry, WorldGenerationContext context) {
        SurfaceRulesContextAccess access = (SurfaceRulesContextAccess)this;
        access.setLastMinSurfaceLevelUpdate(DEFAULT - 1L);
        access.setLastSurfaceDepth2Update(DEFAULT - 1L);
        this.lastUpdateY = DEFAULT;
        access.setSystem(system);
        access.setRandomState(randomState);
        access.setChunk(chunk);
        access.setNoiseChunk(noiseChunk);
        access.setBiomeGetter(biomeGetter);
        access.setContext(context);
    }
}
