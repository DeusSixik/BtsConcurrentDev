package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.blending;

import dev.behindthescenery.btsengineconcurrent.common.chunk_generation.blending.BlendingMasks;
import dev.behindthescenery.btsengineconcurrent.common.path.rework_chunk_generation.blending.BlendingDataPath;
import net.minecraft.core.Direction8;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.*;

import javax.annotation.Nullable;

@Mixin(BlendingData.class)
public abstract class MixinBlendingData implements BlendingDataPath {

    @Shadow
    private boolean hasCalculatedData;

    @Shadow
    protected abstract void addValuesForColumn(int index, ChunkAccess chunk, int x, int z);

    @Shadow
    @Final
    private static int CELL_HORIZONTAL_MAX_INDEX_OUTSIDE;

    @Shadow
    private static int getInsideIndex(int x, int z) {
        throw new NotImplementedException();
    }

    @Shadow
    private static int getOutsideIndex(int x, int z) {
        throw new NotImplementedException();
    }

    /**
     * @author Sixik
     * @reason Use fast data calculator
     */
    @Overwrite
    public static @Nullable BlendingData getOrUpdateBlendingData(WorldGenRegion region, int chunkX, int chunkZ) {
        final ChunkAccess chunkAccess = region.getChunk(chunkX, chunkZ);
        final BlendingData blendingData = chunkAccess.getBlendingData();
        if (blendingData != null && !chunkAccess.getHighestGeneratedStatus().isBefore(ChunkStatus.BIOMES)) {
            ((BlendingDataPath)blendingData).bts$calculateData(chunkAccess, bts$sideByGenerationAge(region, chunkX, chunkZ, false));
            return blendingData;
        } else {
            return null;
        }
    }

    @Unique
    private static int bts$sideByGenerationAge(WorldGenLevel level, int chunkX, int chunkZ, boolean oldNoiseGeneration) {
        int bits = 0;

        for(Direction8 direction8 : Direction8.values()) {
            final int i = chunkX + direction8.getStepX();
            final int j = chunkZ + direction8.getStepZ();
            if (level.getChunk(i, j).isOldNoiseGeneration() == oldNoiseGeneration) {
                bits |= BlendingMasks.getBits(direction8);
            }
        }

        return bits;
    }

    /**
     * Added quick masks for checks. Cycles have been replaced with manual calls. Some of the branches have been merged
     * @param bits Directions
     */
    @Override
    public void bts$calculateData(ChunkAccess chunk, int bits) {
        if (this.hasCalculatedData) return;

        //Fast masks
        final boolean n  = (bits & BlendingMasks.NORTH) != 0;
        final boolean s  = (bits & BlendingMasks.SOUTH) != 0;
        final boolean e  = (bits & BlendingMasks.EAST) != 0;
        final boolean w  = (bits & BlendingMasks.WEST) != 0;
        final boolean ne = (bits & BlendingMasks.NORTH_EAST) != 0;
        final boolean se = (bits & BlendingMasks.SOUTH_EAST) != 0;
        final boolean nw = (bits & BlendingMasks.NORTH_WEST) != 0;

        if (n || w || nw) {
            addValuesForColumn(getInsideIndex(0, 0), chunk, 0, 0);

            if (n) {
                addValuesForColumn(getInsideIndex(1, 0), chunk, 4, 0);
                addValuesForColumn(getInsideIndex(2, 0), chunk, 8, 0);
                addValuesForColumn(getInsideIndex(3, 0), chunk, 12, 0);
            }

            if (w) {
                addValuesForColumn(getInsideIndex(0, 1), chunk, 0, 4);
                addValuesForColumn(getInsideIndex(0, 2), chunk, 0, 8);
                addValuesForColumn(getInsideIndex(0, 3), chunk, 0, 12);
            }
        }

        if (e) {
            final int max = CELL_HORIZONTAL_MAX_INDEX_OUTSIDE;
            addValuesForColumn(getOutsideIndex(max, 1), chunk, 15, 4);
            addValuesForColumn(getOutsideIndex(max, 2), chunk, 15, 8);
            addValuesForColumn(getOutsideIndex(max, 3), chunk, 15, 12);

            if (e && ne) {
                addValuesForColumn(getOutsideIndex(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE, 0), chunk, 15, 0);
            }
        }

        if (s) {
            final int max = CELL_HORIZONTAL_MAX_INDEX_OUTSIDE;
            addValuesForColumn(getOutsideIndex(0, max), chunk, 0, 15);
            addValuesForColumn(getOutsideIndex(1, max), chunk, 4, 15);
            addValuesForColumn(getOutsideIndex(2, max), chunk, 8, 15);
            addValuesForColumn(getOutsideIndex(3, max), chunk, 12, 15);
        }



        if (e && s && se) {
            final int max = CELL_HORIZONTAL_MAX_INDEX_OUTSIDE;
            addValuesForColumn(getOutsideIndex(max, max), chunk, 15, 15);
        }

        this.hasCalculatedData = true;
    }
}
