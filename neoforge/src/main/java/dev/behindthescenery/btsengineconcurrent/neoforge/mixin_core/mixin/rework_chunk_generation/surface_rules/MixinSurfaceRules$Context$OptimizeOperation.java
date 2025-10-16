package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.surface_rules;

import dev.behindthescenery.btsengineconcurrent.common.path.rework_chunk_generation.surface_rules.SurfaceRulesContextPath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.*;

import java.util.function.Function;

@Mixin(SurfaceRules.Context.class)
public abstract class MixinSurfaceRules$Context$OptimizeOperation implements SurfaceRulesContextPath {

    @Shadow
    public long lastUpdateY;

    @Shadow
    @Final
    private Function<BlockPos, Holder<Biome>> biomeGetter;

    @Shadow
    @Final
    BlockPos.MutableBlockPos pos;

    @Shadow
    public int blockY;

    @Shadow
    public int waterHeight;

    @Shadow
    public int stoneDepthBelow;

    @Shadow
    public int stoneDepthAbove;

    @Shadow
    private static int blockCoordToSurfaceCell(int blockCoord) {
        throw new NotImplementedException();
    }

    @Shadow
    public int blockZ;
    @Shadow
    public int blockX;
    @Shadow
    @Final
    private NoiseChunk noiseChunk;

    @Shadow
    private static int surfaceCellToBlockCoord(int surfaceCell) {
        throw new NotImplementedException();
    }

    @Shadow
    private int minSurfaceLevel;
    @Shadow
    public int surfaceDepth;
    @Shadow
    @Final
    private static int HOW_FAR_BELOW_PRELIMINARY_SURFACE_LEVEL_TO_BUILD_SURFACE;

    @Shadow
    public long lastUpdateXZ;
    @Shadow
    @Final
    private SurfaceSystem system;
    @Shadow
    @Final
    public ChunkAccess chunk;
    @Unique
    private Holder<Biome> bts$cached_biome;
    @Unique private boolean bts$hasCachedBiome;

    @Unique private static final int CELL_SIZE = 16;
    @Unique private static final int SURFACE_CELL_MASK = CELL_SIZE - 1;

    @Unique private long bts$cached_surface_cell_key = Long.MIN_VALUE;
    @Unique private final int[][] bts$cached_heights = new int[2][2];
    @Unique private final double[][] bts$cached_lerped = new double[CELL_SIZE][CELL_SIZE];
    @Unique private boolean bts$learp_ready = false;

    @Unique private final int[][] bts$height_cache = new int[16][16];
    @Unique private long bts$lastHeightCacheUpdate = Long.MIN_VALUE;

    /**
     * @author Sixik
     * @reason Remove using {@code Supplier<Holder<Biome>>}. Now we cached biome how {@code Holder<Biome>} without {@code Supplier}
     */
    @Overwrite
    protected void updateY(int stoneDepthAbove, int stoneDepthBelow, int waterHeight, int blockX, int blockY, int blockZ) {
        ++this.lastUpdateY;

        this.pos.set(blockX, blockY, blockZ);
        this.blockY = blockY;
        this.waterHeight = waterHeight;
        this.stoneDepthBelow = stoneDepthBelow;
        this.stoneDepthAbove = stoneDepthAbove;

        this.bts$cached_biome = this.biomeGetter.apply(this.pos);
        this.bts$hasCachedBiome = bts$cached_biome != null;
    }

    /**
     * @author Sixik
     * @reason
     */
    @Overwrite
    protected void updateXZ(int blockX, int blockZ) {
        ++this.lastUpdateXZ;
        ++this.lastUpdateY;
        this.blockX = blockX;
        this.blockZ = blockZ;
        this.surfaceDepth = this.system.getSurfaceDepth(blockX, blockZ);

        if (this.bts$lastHeightCacheUpdate != this.lastUpdateXZ) {
            this.bts$lastHeightCacheUpdate = this.lastUpdateXZ;
            final ChunkAccess chunk = this.chunk;

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    bts$height_cache[x][z] = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                }
            }
        }
    }

    @Override
    public Holder<Biome> bts$getBiome() {
        if(bts$hasCachedBiome) return bts$cached_biome;
        throw new IllegalStateException("Biome not set yet!");
    }

    @Override
    public int[][] bts$getHeightCache() {
        return bts$height_cache;
    }


    /**
     * @author Sixik
     * @reason Use inline math operation and cache
     */
    @Overwrite
    protected int getMinSurfaceLevel() {
        final int cellX = blockCoordToSurfaceCell(this.blockX);
        final int cellZ = blockCoordToSurfaceCell(this.blockZ);
        final long cellKey = ChunkPos.asLong(cellX, cellZ);

        if(cellKey != bts$cached_surface_cell_key) {
            bts$cached_surface_cell_key = cellKey;
            bts$learp_ready = false;
        }

        if(!bts$learp_ready) {

            final int h00 = bts$cached_heights[0][0] = noiseChunk.preliminarySurfaceLevel(surfaceCellToBlockCoord(cellX),     surfaceCellToBlockCoord(cellZ));
            final int h10 = bts$cached_heights[1][0] = noiseChunk.preliminarySurfaceLevel(surfaceCellToBlockCoord(cellX + 1), surfaceCellToBlockCoord(cellZ));
            final int h01 = bts$cached_heights[0][1] = noiseChunk.preliminarySurfaceLevel(surfaceCellToBlockCoord(cellX),     surfaceCellToBlockCoord(cellZ + 1));
            final int h11 = bts$cached_heights[1][1] = noiseChunk.preliminarySurfaceLevel(surfaceCellToBlockCoord(cellX + 1), surfaceCellToBlockCoord(cellZ + 1));

            final double dx10 = h10 - h00;
            final double dz01 = h01 - h00;
            final double dCross = h00 - h10 - h01 + h11;

            for (int x = 0; x < CELL_SIZE; x++) {
                final double fx = (double) x / CELL_SIZE;
                for (int z = 0; z < CELL_SIZE; z++) {
                    final double fz = (double) z / CELL_SIZE;
                    bts$cached_lerped[x][z] = h00 + dx10 * fx + dz01 * fz + dCross * fx * fz;
                }
            }
            bts$learp_ready = true;
        }

        final int localX = this.blockX & SURFACE_CELL_MASK;
        final int localZ = this.blockZ & SURFACE_CELL_MASK;
        final int k = (int) Math.floor(bts$cached_lerped[localX][localZ]);
        return this.minSurfaceLevel = k + this.surfaceDepth - HOW_FAR_BELOW_PRELIMINARY_SURFACE_LEVEL_TO_BUILD_SURFACE;
    }
}
