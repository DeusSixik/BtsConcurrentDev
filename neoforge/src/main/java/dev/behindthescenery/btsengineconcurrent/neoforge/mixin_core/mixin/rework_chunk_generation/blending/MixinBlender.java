package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.blending;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@Mixin(Blender.class)
public abstract class MixinBlender {

    @Shadow
    protected abstract double getBlendingDataValue(int x, int y, int z, Blender.CellValueGetter getter);

    @Shadow
    @Final
    private Long2ObjectOpenHashMap<BlendingData> densityBlendingData;

    @Shadow
    @Final
    private static NormalNoise SHIFT_NOISE;

    /**
     * @author Sixik
     * @reason Optimize math operation
     */
    @Overwrite
    public double blendDensity(DensityFunction.FunctionContext context, double density) {
        final int i = QuartPos.fromBlock(context.blockX());
        final int j = context.blockY() / 8;
        final int k = QuartPos.fromBlock(context.blockZ());
        final double d = this.getBlendingDataValue(i, j, k, BlendingData::getDensity);
        if (d != Double.MAX_VALUE) {
            return d;
        } else {
            final MutableDouble mutableDouble = new MutableDouble(0.0F);
            final MutableDouble mutableDouble2 = new MutableDouble(0.0F);
            final MutableDouble mutableDouble3 = new MutableDouble(Double.POSITIVE_INFINITY);

            this.densityBlendingData.forEach((key, data) ->
                    data.iterateDensities(
                            QuartPos.fromSection(ChunkPos.getX(key)),
                            QuartPos.fromSection(ChunkPos.getZ(key)),
                            j - 1, j + 1, (l, m, n, v) -> {

                            final double dx = i - l;
                            final double dz = j - l;
                            final double e2 = dx * dx + dz * dz;
                            final double e = Math.sqrt(e2);
                            if (!(e > (double)2.0F)) {
                            if (e < mutableDouble3.doubleValue()) {
                                mutableDouble3.setValue(e);
                            }

                            final double f = (double)1.0F / (e * e * e * e);
                            mutableDouble2.add(v * f);
                            mutableDouble.add(f);
                        }
                    }
                )
            );
            if (mutableDouble3.doubleValue() == Double.POSITIVE_INFINITY) {
                return density;
            } else {
                final double e = mutableDouble2.doubleValue() / mutableDouble.doubleValue();
                final double f = Mth.clamp(mutableDouble3.doubleValue() / 3.0, 0.0, 1.0);
                return Mth.lerp(f, e, density);
            }
        }
    }

    /**
     * @author Sixik
     * @reason Optimize math operation
     */
    @Overwrite
    public static void addAroundOldChunksCarvingMaskFilter(WorldGenLevel level, ProtoChunk chunk) {
        final ChunkPos chunkPos = chunk.getPos();
        final ImmutableMap.Builder<Direction8, BlendingData> builder = ImmutableMap.builder();

        for(Direction8 direction8 : Direction8.values()) {
            final int i = chunkPos.x + direction8.getStepX();
            final int j = chunkPos.z + direction8.getStepZ();
            final BlendingData blendingData = level.getChunk(i, j).getBlendingData();
            if (blendingData != null) {
                builder.put(direction8, blendingData);
            }
        }

        final ImmutableMap<Direction8, BlendingData> immutableMap = builder.build();
        if (chunk.isOldNoiseGeneration() || !immutableMap.isEmpty()) {
            final Blender.DistanceGetter distanceGetter = makeOldChunkDistanceGetter(chunk.getBlendingData(), immutableMap);
            final CarvingMask.Mask mask = (ix, jx, k) -> {
                final double d = (double)ix + 0.5 + SHIFT_NOISE.getValue(ix, jx, k) * 4.0;
                final double e = (double)jx + 0.5 + SHIFT_NOISE.getValue(jx, k, ix) * 4.0;
                final double f = (double)k + 0.5 + SHIFT_NOISE.getValue(k, ix, jx) * 4.0;
                return distanceGetter.getDistance(d, e, f) < (double)4.0F;
            };

            for (GenerationStep.Carving carving : GenerationStep.Carving.values()) {
                chunk.getOrCreateCarvingMask(carving).setAdditionalMask(mask);
            }
        }
    }

    /**
     * @author Sixik
     * @reason Optimize math operation
     */
    @Overwrite
    public static Blender.DistanceGetter makeOldChunkDistanceGetter(@Nullable BlendingData blendingData, Map<Direction8, BlendingData> surroundingBlendingData) {
        final List<Blender.DistanceGetter> list = Lists.newArrayList();
        if (blendingData != null) {
            list.add(makeOffsetOldChunkDistanceGetter(null, blendingData));
        }

        surroundingBlendingData.forEach((arg, arg2) -> list.add(makeOffsetOldChunkDistanceGetter(arg, arg2)));
        return (d, e, f) -> {
            double g = Double.POSITIVE_INFINITY;

            for(Blender.DistanceGetter distanceGetter : list) {
                final double h = distanceGetter.getDistance(d, e, f);
                if (h < g) {
                    g = h;
                }
            }

            return g;
        };
    }

    /**
     * @author Sixik
     * @reason Optimize math operation
     */
    @Overwrite
    private static Blender.DistanceGetter makeOffsetOldChunkDistanceGetter(@Nullable Direction8 direction, BlendingData blendingData) {
        double d = 0.0F;
        double e = 0.0F;
        if (direction != null) {
            for(Direction direction2 : direction.getDirections()) {
                d += direction2.getStepX() * 16;
                e += direction2.getStepZ() * 16;
            }
        }

        final double h = (double)blendingData.getAreaWithOldGeneration().getHeight() / 2.0;
        final double i = (double)blendingData.getAreaWithOldGeneration().getMinBuildHeight() + h;
        final double finalD = d;
        final double finalE = e;
        return (hx, ix, j) ->
                distanceToCube(
                        hx - 8.0 - finalD,
                        ix - i,
                        j - 8.0 - finalE,
                        8.0, h,
                        8.0);
    }

    /**
     * @author Sixik
     * @reason Optimize math operation
     */
    @Overwrite
    private static double distanceToCube(double x1, double y1, double z1,
                                         double x2, double y2, double z2) {
        final double dx = Math.abs(x1) > x2 ? Math.abs(x1) - x2 : 0.0;
        final double dy = Math.abs(y1) > y2 ? Math.abs(y1) - y2 : 0.0;
        final double dz = Math.abs(z1) > z2 ? Math.abs(z1) - z2 : 0.0;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * @author Sixik
     * @reason Optimize math operation
     */
    @Overwrite
    private static double heightToOffset(double height) {
        final double MOD = 8.0;
        final double C1 = 32.0;
        final double C2 = 128.0;
        final double C3 = 120.0;
        final double C4 = 3.0;

        final double e = height + 0.5;
        double f = e % MOD;
        if (f < 0) f += MOD;

        double num = C1 * (e - C2) - C4 * (e - C3) * f + C4 * f * f;
        return num / (128.0 * (C1 - C4 * f));
    }
}
