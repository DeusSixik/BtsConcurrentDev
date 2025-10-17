package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.carver;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldGenerationContext.class)
public class MixinWorldGenerationContext$FixNullCrash {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelHeightAccessor;getMinBuildHeight()I"))
    public int bts$init$getMinBuildHeight(LevelHeightAccessor instance) {
        if(instance == null) return 0;
        return instance.getMinBuildHeight();
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelHeightAccessor;getHeight()I"))
    public int bts$init$getHeight(LevelHeightAccessor instance) {
        if(instance == null) return 0;
        return instance.getHeight();
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;getMinY()I"))
    public int bts$init$getMinY(ChunkGenerator instance) {
        if(instance == null) return 0;
        return instance.getMinY();
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;getGenDepth()I"))
    public int bts$init$getGenDepth(ChunkGenerator instance) {
        if(instance == null) return 0;
        return instance.getGenDepth();
    }
}
