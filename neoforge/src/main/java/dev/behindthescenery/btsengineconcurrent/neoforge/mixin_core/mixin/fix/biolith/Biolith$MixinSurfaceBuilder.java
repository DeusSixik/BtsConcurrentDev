package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.fix.biolith;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.terraformersmc.biolith.api.surface.BiolithSurfaceBuilder;
import com.terraformersmc.biolith.impl.surface.SurfaceBuilderCollector;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = SurfaceSystem.class, priority = Integer.MAX_VALUE)
public class Biolith$MixinSurfaceBuilder {


    @Shadow
    @Final
    private PositionalRandomFactory noiseRandom;

    @Shadow
    @Final
    private int seaLevel;

    @TargetHandler(
            mixin = "dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.surface_system.MixinSurfaceSystem",
            name = "buildSurface"
    )
    @WrapOperation(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/resources/ResourceKey;)Z",
                    ordinal = 0
            )
    )
    private boolean biolith$injectSurfaceBuilders(Holder<Biome> biome, ResourceKey<Biome> targetKey, Operation<Boolean> original, @Local(argsOnly = true) BiomeManager biomeAccess, @Local(argsOnly = true) ChunkAccess chunk, @Local BlockColumn blockColumn, @Local(ordinal = 4) int m, @Local(ordinal = 5) int n, @Local(ordinal = 6) int o) {
        RandomSource random = this.noiseRandom.at(m, o, n);

        for(BiolithSurfaceBuilder builder : SurfaceBuilderCollector.getBuilders()) {
            if (builder.filterBiome(biome)) {
                builder.generate(biomeAccess, blockColumn, random, chunk, biome.value(), m, n, o, this.seaLevel);
            }
        }

        return original.call(biome, targetKey);
    }

    @TargetHandler(
            mixin = "dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.surface_system.MixinSurfaceSystem",
            name = "buildSurface"
    )
    @WrapOperation(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/resources/ResourceKey;)Z",
                    ordinal = 1
            )
    )
    private boolean biolith$injectLateSurfaceBuilders(Holder<Biome> biome, ResourceKey<Biome> targetKey, Operation<Boolean> original, @Local(argsOnly = true) BiomeManager biomeAccess, @Local(argsOnly = true) ChunkAccess chunk, @Local BlockColumn blockColumn, @Local SurfaceRules.Context materialRuleContext, @Local(ordinal = 4) int m, @Local(ordinal = 5) int n, @Local(ordinal = 6) int o) {
        RandomSource random = this.noiseRandom.at(m, o, n);
        int surfaceMinY = materialRuleContext.getMinSurfaceLevel();

        for(BiolithSurfaceBuilder builder : SurfaceBuilderCollector.getBuilders()) {
            if (builder.filterBiome(biome)) {
                builder.generateLate(biomeAccess, blockColumn, random, chunk, biome.value(), m, n, o, this.seaLevel, surfaceMinY);
            }
        }

        return original.call(biome, targetKey);
    }
}
