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

@Mixin(value = SurfaceSystem.class, priority = Integer.MAX_VALUE - 2)
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
     * @reason Optimize using resources.
     */
    @Overwrite
    public void buildSurface(RandomState randomState, BiomeManager biomeManager, Registry<Biome> biomes, boolean useLegacyRandomSource, WorldGenerationContext context, ChunkAccess chunk, NoiseChunk noiseChunk, SurfaceRules.RuleSource ruleSource) {
        final ChunkPos chunkPos = chunk.getPos();
        final int startX = chunkPos.getMinBlockX();
        final int startZ = chunkPos.getMinBlockZ();

        final LevelHeightAccessor heightView = chunk.getHeightAccessorForGeneration();
        final int bottomY = heightView.getMinBuildHeight();

        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        final BlockPos.MutableBlockPos biomePos = new BlockPos.MutableBlockPos();

        final BlockColumn column = new SurfaceSystemBlockColumn(chunk, pos);
        final SurfaceRules.Context ctx = new SurfaceRules.Context(ReflectionsUtils.cast(this), randomState, chunk, noiseChunk,
                biomeManager::getBiome, biomes, context);
        final SurfaceRules.SurfaceRule rule = ruleSource.apply(ctx);

        for (int dx = 0; dx < 16; ++dx) {
            final int worldX = startX + dx;
            for (int dz = 0; dz < 16; ++dz) {
                final int worldZ = startZ + dz;

                int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, dx, dz) + 1;

                pos.setX(worldX).setZ(worldZ);
                final Holder<Biome> biomeHolder = biomeManager.getBiome(
                        biomePos.set(worldX, useLegacyRandomSource ? 0 : surfaceY, worldZ)
                );

                final boolean isBadlands = biomeHolder.is(Biomes.ERODED_BADLANDS);

                if (isBadlands) {
                    this.erodedBadlandsExtension(column, worldX, worldZ, surfaceY, chunk);
                    surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, dx, dz) + 1;
                }

                ctx.updateXZ(worldX, worldZ);

                int solidRun = 0;
                int firstFluidAbove = Integer.MIN_VALUE;
                int sFloor = Integer.MAX_VALUE;

                for (int y = surfaceY; y >= bottomY; --y) {
                    BlockState st = column.getBlock(y);

                    if (st.isAir()) {
                        solidRun = 0;
                        firstFluidAbove = Integer.MIN_VALUE;
                        continue;
                    }

                    if (!st.getFluidState().isEmpty()) {
                        if (firstFluidAbove == Integer.MIN_VALUE) firstFluidAbove = y + 1;
                        continue;
                    }

                    if (sFloor >= y) {
                        int scan = y - 1;
                        while (scan >= bottomY - 1 && this.isStone(column.getBlock(scan))) {
                            --scan;
                        }
                        sFloor = scan + 1;
                    }

                    ++solidRun;
                    int v = y - sFloor + 1;
                    ctx.updateY(solidRun, v, firstFluidAbove, worldX, y, worldZ);

                    if (st == this.defaultBlock) {
                        BlockState to = rule.tryApply(worldX, y, worldZ);
                        if (to != null && to != st) {
                            column.setBlock(y, to);
                        }
                    }
                }

                final boolean isFrozen = biomeHolder.is(Biomes.FROZEN_OCEAN) || biomeHolder.is(Biomes.DEEP_FROZEN_OCEAN);

                if (isFrozen) {
                    this.frozenOceanExtension(ctx.getMinSurfaceLevel(), biomeHolder.value(),
                            column, biomePos, worldX, worldZ, surfaceY);
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
