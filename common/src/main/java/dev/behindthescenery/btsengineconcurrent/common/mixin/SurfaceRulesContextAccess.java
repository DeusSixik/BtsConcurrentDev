package dev.behindthescenery.btsengineconcurrent.common.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@Mixin(SurfaceRules.Context.class)
public interface SurfaceRulesContextAccess {

    @Accessor
    void setPos(BlockPos.MutableBlockPos pos);
    @Accessor
    void setSystem(SurfaceSystem system);
    @Accessor
    void setRandomState(RandomState randomState);
    @Accessor
    void setChunk(ChunkAccess chunk);
    @Accessor
    void setNoiseChunk(NoiseChunk noiseChunk);
    @Accessor
    void setBiomeGetter(Function<BlockPos, Holder<Biome>> biomeGetter);
    @Accessor
    void setContext(WorldGenerationContext context);
    @Accessor
    void setLastSurfaceDepth2Update(long value);
    @Accessor
    void setLastMinSurfaceLevelUpdate(long value);
}
