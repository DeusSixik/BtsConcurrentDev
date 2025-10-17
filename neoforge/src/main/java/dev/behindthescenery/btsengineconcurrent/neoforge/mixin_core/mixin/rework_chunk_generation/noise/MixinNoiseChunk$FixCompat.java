//package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.noise;
//
//import dev.behindthescenery.btsengineconcurrent.common.path.rework_chunk_generation.surface_rules.NoiseInterpolatorPath;
//import net.minecraft.world.level.levelgen.DensityFunction;
//import net.minecraft.world.level.levelgen.NoiseChunk;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Overwrite;
//import org.spongepowered.asm.mixin.Shadow;
//
//import java.util.List;
//
//@Mixin(NoiseChunk.class)
//public class MixinNoiseChunk$FixCompat {
//
//    @Shadow
//    @Final
//    public int cellWidth;
//
//    @Shadow
//    private int cellStartBlockX;
//
//    @Shadow
//    public int inCellX;
//
//    @Shadow
//    @Final
//    public int cellCountXZ;
//
//    @Shadow
//    @Final
//    private int firstCellZ;
//
//    @Shadow
//    private int cellStartBlockZ;
//
//    @Shadow
//    public int inCellZ;
//
//    @Shadow
//    private long arrayInterpolationCounter;
//
//    @Shadow
//    @Final
//    private List<NoiseChunk.NoiseInterpolator> interpolators;
//
//    @Shadow
//    @Final
//    private DensityFunction.ContextProvider sliceFillingContextProvider;
//
//    /**
//     * @author Siixk
//     * @reason
//     */
//    @Overwrite
//    private void fillSlice(boolean isSlice0, int start) {
//        this.cellStartBlockX = start * this.cellWidth;
//        this.inCellX = 0;
//
//        for (int i = 0; i < this.cellCountXZ + 1; ++i) {
//            int j = this.firstCellZ + i;
//            this.cellStartBlockZ = j * this.cellWidth;
//            this.inCellZ = 0;
//            ++this.arrayInterpolationCounter;
//
//            for (NoiseChunk.NoiseInterpolator interpolator : this.interpolators) {
//                ((NoiseInterpolatorPath) interpolator).bts$setFillZ(i, isSlice0);
//                interpolator.fillArray(null, this.sliceFillingContextProvider);
//            }
//        }
//
//        ++this.arrayInterpolationCounter;
//    }
//}
