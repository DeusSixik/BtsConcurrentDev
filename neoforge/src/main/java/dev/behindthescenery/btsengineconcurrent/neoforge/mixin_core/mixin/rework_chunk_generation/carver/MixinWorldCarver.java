package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.carver;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;

@Mixin(WorldCarver.class)
public abstract class MixinWorldCarver<C extends CarverConfiguration> {

    @Shadow
    protected abstract boolean canReplaceBlock(C config, BlockState state);

    @Shadow
    private static boolean isDebugEnabled(CarverConfiguration config) {
        return false;
    }

    @Shadow
    @Nullable
    protected abstract BlockState getCarveState(CarvingContext context, C config, BlockPos pos, Aquifer aquifer);

    /**
     * @author Sixik
     * @reason Optimize operations
     */
    @Overwrite
    protected boolean carveEllipsoid(CarvingContext context, C config, ChunkAccess chunk, Function<BlockPos,
            Holder<Biome>> biomeAccessor, Aquifer aquifer, double x, double y, double z,
                                     double horizontalRadius, double verticalRadius, CarvingMask mask,
                                     WorldCarver.CarveSkipChecker skipChecker) {
        final ChunkPos pos = chunk.getPos();
        final double midX = pos.getMiddleBlockX();
        final double midZ = pos.getMiddleBlockZ();
        final double maxDist = 16.0 + horizontalRadius * 2.0;

        if (Math.abs(x - midX) > maxDist || Math.abs(z - midZ) > maxDist)
            return false;

        final int baseX = pos.getMinBlockX();
        final int baseZ = pos.getMinBlockZ();

        final double invH = 1.0 / horizontalRadius;
        final double invV = 1.0 / verticalRadius;

        final int minY = Math.max(Mth.floor(y - verticalRadius) - 1, context.getMinGenY() + 1);
        final int maxY = Math.min(Mth.floor(y + verticalRadius) + 1,
                context.getMinGenY() + context.getGenDepth() - (chunk.isUpgrading() ? 0 : 7));

        final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        final BlockPos.MutableBlockPos belowPos = new BlockPos.MutableBlockPos();

        boolean modified = false;
        for (int rx = Math.max(Mth.floor(x - horizontalRadius) - baseX - 1, 0),
             lx = Math.min(Mth.floor(x + horizontalRadius) - baseX, 15);
             rx <= lx; ++rx) {

            double dx = ((pos.getBlockX(rx) + 0.5) - x) * invH;
            double dx2 = dx * dx;

            for (int rz = Math.max(Mth.floor(z - horizontalRadius) - baseZ - 1, 0),
                 lz = Math.min(Mth.floor(z + horizontalRadius) - baseZ, 15);
                 rz <= lz; ++rz) {

                double dz = ((pos.getBlockZ(rz) + 0.5) - z) * invH;
                if (dx2 + dz * dz >= 1.0) continue;

                MutableBoolean reachedSurface = new MutableBoolean(false);
                for (int ry = maxY; ry > minY; --ry) {
                    double dy = ((ry - 0.5) - y) * invV;
                    if (skipChecker.shouldSkip(context, dx, dy, dz, ry))
                        continue;
                    if (mask.get(rx, ry, rz))
                        continue;

                    mask.set(rx, ry, rz);
                    blockPos.set(pos.getBlockX(rx), ry, pos.getBlockZ(rz));
                    modified |= carveBlock(context, config, chunk, biomeAccessor,
                            mask, blockPos, belowPos, aquifer, reachedSurface);
                }
            }
        }
        return modified;
    }

    /**
     * @author Sixik
     * @reason Optimize operations
     */
    @Overwrite
    protected boolean carveBlock(
            CarvingContext context, C config,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            CarvingMask carvingMask,
            BlockPos.MutableBlockPos pos,
            BlockPos.MutableBlockPos checkPos,
            Aquifer aquifer,
            MutableBoolean reachedSurface
    ) {
        final BlockState state = chunk.getBlockState(pos);
        final boolean debug = isDebugEnabled(config);

        if (!debug && !this.canReplaceBlock(config, state)) {
            return false;
        }

        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MYCELIUM)) {
            reachedSurface.setTrue();
        }

        final BlockState carveState = this.getCarveState(context, config, pos, aquifer);
        if (carveState == null) {
            return false;
        }

        chunk.setBlockState(pos, carveState, false);

        final FluidState fluidState = carveState.getFluidState();
        final boolean hasFluid = !fluidState.isEmpty();

        if (hasFluid && aquifer.shouldScheduleFluidUpdate()) {
            chunk.markPosForPostprocessing(pos);
        }

        if (reachedSurface.isTrue()) {
            checkPos.set(pos.getX(), pos.getY() - 1, pos.getZ());
            BlockState below = chunk.getBlockState(checkPos);

            if (below.is(Blocks.DIRT)) {
                BlockState top = context.topMaterial(biomeGetter, chunk, checkPos, hasFluid).orElse(null);
                if (top != null) {
                    chunk.setBlockState(checkPos, top, false);
                    if (!top.getFluidState().isEmpty()) {
                        chunk.markPosForPostprocessing(checkPos);
                    }
                }
            }
        }

        return true;
    }

}
