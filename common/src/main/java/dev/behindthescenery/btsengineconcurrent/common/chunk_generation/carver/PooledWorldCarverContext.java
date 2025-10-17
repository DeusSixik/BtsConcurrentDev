package dev.behindthescenery.btsengineconcurrent.common.chunk_generation.carver;

import dev.behindthescenery.btsengineconcurrent.common.chunk_generation.features.PooledFeatureContext;
import dev.behindthescenery.btsengineconcurrent.common.utils.SimpleObjectPool;
import dev.behindthescenery.btsengineconcurrent.common.utils.SimpleObjectPoolUnSynchronize;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.carver.CarvingContext;

import java.util.Optional;
import java.util.function.Function;

public class PooledWorldCarverContext extends CarvingContext {

    public static final ThreadLocal<SimpleObjectPool<PooledWorldCarverContext>> POOL = ThreadLocal.withInitial(() ->
            new SimpleObjectPoolUnSynchronize<>(
                    unused -> new PooledWorldCarverContext(),
                    PooledWorldCarverContext::reInit,
                    PooledWorldCarverContext::reInit,
                    2048
            ));

    private RegistryAccess registryAccess;
    private LevelHeightAccessor level;
    private NoiseChunk noiseChunk;
    private RandomState randomState;
    private SurfaceRules.RuleSource surfaceRule;
    private int minY;
    private int height;

    public PooledWorldCarverContext() {
        super(null, null, null, null, null, null);
    }

    public void reInit(NoiseBasedChunkGenerator generator,
                        RegistryAccess registryAccess,
                        LevelHeightAccessor level,
                        NoiseChunk noiseChunk,
                        RandomState randomState,
                        SurfaceRules.RuleSource surfaceRule) {
        this.registryAccess = registryAccess;
        this.level = level;
        this.noiseChunk = noiseChunk;
        this.randomState = randomState;
        this.surfaceRule = surfaceRule;
        this.minY = Math.max(level.getMinBuildHeight(), generator.getMinY());
        this.height = Math.min(level.getHeight(), generator.getGenDepth());
    }

    public void reInit() {
        this.registryAccess = null;
        this.level = null;
        this.noiseChunk = null;
        this.randomState = null;
        this.surfaceRule = null;
    }

    @Override
    public RandomState randomState() {
        return randomState;
    }

    @Override
    public RegistryAccess registryAccess() {
        return registryAccess;
    }

    @Override
    public Optional<BlockState> topMaterial(Function<BlockPos, Holder<Biome>> biomeMapper, ChunkAccess access, BlockPos pos, boolean hasFluid) {
        return this.randomState.surfaceSystem().topMaterial(this.surfaceRule, this, biomeMapper, access, this.noiseChunk, pos, hasFluid);
    }

    @Override
    public int getMinGenY() {
        return this.minY;
    }

    @Override
    public int getGenDepth() {
        return height;
    }
}
