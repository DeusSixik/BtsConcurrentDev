package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.noise;

import dev.behindthescenery.btsengineconcurrent.common.path.rework_chunk_generation.surface_rules.NoiseInterpolatorPath;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Deprecated
//TODO: Optimize NoiseChunk
@Mixin(NoiseChunk.NoiseInterpolator.class)
public abstract class MixinNoiseInterpolator$OptimizeOperation implements
        DensityFunctions.MarkerOrMarked, NoiseChunk.NoiseChunkDensityFunction, NoiseInterpolatorPath {

    @Shadow @Final NoiseChunk field_34622;
    @Shadow @Final private DensityFunction noiseFiller;
    @Shadow private double noise000, noise100, noise001, noise101;
    @Shadow private double noise010, noise110, noise011, noise111;
    @Shadow private double value;


//    private double[] slice0;
//    private double[] slice1;
//
//    private int sliceWidth;
//    private int sliceSize;
//
//    private int currentFillZ = -1;
//    private boolean currentIsSlice0;

//    @Redirect(method = "<init>", at = @At(value = "INVOKE",
//            target = "Lnet/minecraft/world/level/levelgen/NoiseChunk$NoiseInterpolator;allocateSlice(II)[[D"))
//    private double[][] bts$redirect$allocate(NoiseChunk.NoiseInterpolator instance, int cellCountY, int cellCountXZ) {
//        return new double[0][0];
//    }
//
//    @Inject(method = "<init>", at = @At(value = "INVOKE",
//            target = "Lnet/minecraft/world/level/levelgen/NoiseChunk$NoiseInterpolator;allocateSlice(II)[[D", ordinal = 1))
//    private void bts$init(NoiseChunk chunk, DensityFunction noiseFiller, CallbackInfo ci) {
//        this.sliceWidth = chunk.cellCountY + 1;
//        this.sliceSize = (chunk.cellCountXZ + 1) * sliceWidth;
//        this.slice0 = new double[sliceSize];
//        this.slice1 = new double[sliceSize];
//    }
//
//    private int linearIndex(int z, int y) {
//        return z * sliceWidth + y;
//    }
//
//    /**
//     * @author Sixik
//     * @reason
//     */
//    @Overwrite
//    void selectCellYZ(int y, int z) {
//        int y1 = y + 1;
//        int z1 = z + 1;
//
//        this.noise000 = slice0[linearIndex(z, y)];
//        this.noise001 = slice0[linearIndex(z1, y)];
//        this.noise100 = slice1[linearIndex(z, y)];
//        this.noise101 = slice1[linearIndex(z1, y)];
//        this.noise010 = slice0[linearIndex(z, y1)];
//        this.noise011 = slice0[linearIndex(z1, y1)];
//        this.noise110 = slice1[linearIndex(z, y1)];
//        this.noise111 = slice1[linearIndex(z1, y1)];
//    }

    /**
     * @author Sixik
     * @reason
     */
    @Overwrite
    public double compute(DensityFunction.FunctionContext context) {
        if (context != field_34622) {
            return noiseFiller.compute(context);
        }
        if (!field_34622.interpolating) {
            throw new IllegalStateException("Trying to sample interpolator outside interpolation loop");
        }
        if (field_34622.fillingCell) {
            double dx = (double) field_34622.inCellX / field_34622.cellWidth;
            double dy = (double) field_34622.inCellY / field_34622.cellHeight;
            double dz = (double) field_34622.inCellZ / field_34622.cellWidth;

            double xy00 = noise000 + (noise010 - noise000) * dy;
            double xy10 = noise100 + (noise110 - noise100) * dy;
            double xy01 = noise001 + (noise011 - noise001) * dy;
            double xy11 = noise101 + (noise111 - noise101) * dy;

            double z0 = xy00 + (xy10 - xy00) * dx;
            double z1 = xy01 + (xy11 - xy01) * dx;

            return z0 + (z1 - z0) * dz;
        }
        return value;
    }

//    @Override
//    public void bts$setFillZ(int z, boolean isSlice0) {
//        this.currentFillZ = z;
//        this.currentIsSlice0 = isSlice0;
//    }
//
//    /**
//     * @author Sixik
//     * @reason
//     */
//    @Overwrite
//    public void fillArray(double[] array, DensityFunction.ContextProvider contextProvider) {
//        if (field_34622.fillingCell) {
//            contextProvider.fillAllDirectly(array, this);
//            return;
//        }
//
//        if (currentFillZ < 0) {
//            throw new IllegalStateException("Z index not set before fillArray");
//        }
//
//        double[] target = currentIsSlice0 ? slice0 : slice1;
//        int baseIndex = currentFillZ * sliceWidth;
//
//        for (int y = 0; y < sliceWidth; ++y) {
//            DensityFunction.FunctionContext ctx = contextProvider.forIndex(y);
//            target[baseIndex + y] = noiseFiller.compute(ctx);
//        }
//
//        currentFillZ = -1;
//    }
}
