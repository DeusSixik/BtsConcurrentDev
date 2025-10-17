package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.surface_system;

import dev.behindthescenery.btsengineconcurrent.ReflectionsUtils;
import dev.behindthescenery.btsengineconcurrent.common.chunk_generation.surface_system.SurfaceSystemBlockColumn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SurfaceSystem.class)
public abstract class MixinSurfaceSystem {

    @Shadow
    protected abstract void erodedBadlandsExtension(BlockColumn blockColumn, int x, int z, int height, LevelHeightAccessor level);

    @Shadow
    @Final
    private BlockState defaultBlock;

    @Shadow
    protected abstract void frozenOceanExtension(int minSurfaceLevel, Biome biome, BlockColumn blockColumn, BlockPos.MutableBlockPos topWaterPos, int x, int z, int height);

    /**
     * @author Sixik
     * @reason Optimize allocate and operation with Heightmap
     */
    @Overwrite
    public void buildSurface(RandomState randomState, BiomeManager biomeManager, Registry<Biome> biomes, boolean useLegacyRandomSource, WorldGenerationContext context, final ChunkAccess chunk, NoiseChunk noiseChunk, SurfaceRules.RuleSource ruleSource) {
        final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        final ChunkPos chunkPos = chunk.getPos();

        final int startX = chunkPos.getMinBlockX();
        final int startZ = chunkPos.getMinBlockZ();
        final int bottomY = chunk.getMinBuildHeight();
        final BlockColumn blockColumn = new SurfaceSystemBlockColumn(chunk, mutableBlockPos);

        final BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();

        final SurfaceRules.Context ctx = new SurfaceRules.Context(ReflectionsUtils.cast(this),
                randomState, chunk, noiseChunk, biomeManager::getBiome, biomes, context);

        final SurfaceRules.SurfaceRule surfaceRule = ruleSource.apply(ctx);
        for (int k = 0; k < 16; ++k) {
            final int m = startX + k;

            for (int l = 0; l < 16; ++l) {
                final int n = startZ + l;
                final int o = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, k, l) + 1;

                mutableBlockPos.setX(m).setZ(n);

                int p = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, k, l) + 1;

                final Holder<Biome> holder = biomeManager.getBiome(mutableBlockPos2.set(m, useLegacyRandomSource ? 0 : o, n));
                final boolean isBadlands = holder.is(Biomes.ERODED_BADLANDS);
                final boolean isFrozen = holder.is(Biomes.FROZEN_OCEAN) || holder.is(Biomes.DEEP_FROZEN_OCEAN);

                if (isBadlands) {
                    this.erodedBadlandsExtension(blockColumn, m, n, o, chunk);
                    p = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, k, l) + 1;
                }
                ctx.updateXZ(m, n);

                int solidRun = 0;
                int firstFluidAbove = Integer.MIN_VALUE;
                int sFloor  = Integer.MAX_VALUE;

                for (int y = p; y >= bottomY; --y) {
                    final BlockState st = blockColumn.getBlock(y);

                    if (st.isAir()) {
                        solidRun = 0;
                        firstFluidAbove = Integer.MIN_VALUE;
                        continue;
                    }

                    if (!st.getFluidState().isEmpty()) {
                        if (firstFluidAbove == Integer.MIN_VALUE)
                            firstFluidAbove = y + 1;
                        continue;
                    }

                    if(sFloor >= y) {
                        int v = y - 1;
                        while (v >= bottomY - 1 && isStone(blockColumn.getBlock(v))) {
                            --v;
                        }
                        sFloor = v + 1;
                    }

                    int v = y - sFloor + 1;
                    ctx.updateY(++solidRun, v, firstFluidAbove, m, y, n);

                    if(st == this.defaultBlock) {
                        final BlockState to = surfaceRule.tryApply(m, y, n);
                        if(to != null && to != st)
                            blockColumn.setBlock(y, to);
                    }
                }

                if(isFrozen) {
                    this.frozenOceanExtension(ctx.getMinSurfaceLevel(),
                            holder.value(), blockColumn, mutableBlockPos2, m, n, o);
                }
            }
        }
    }

    /**
     * @author Sixik
     * @reason Fast check
     */
    @Overwrite
    private boolean isStone(BlockState state) {
        return state.isSolid();
    }
}
