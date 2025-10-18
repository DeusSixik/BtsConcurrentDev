package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.fix.biomewegone;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.potionstudios.biomeswevegone.world.level.levelgen.biome.BWGBiomes;
import net.potionstudios.biomeswevegone.world.level.levelgen.surfacerules.BandsContext;
import net.potionstudios.biomeswevegone.world.level.levelgen.surfacerules.BandsRuleSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(value = SurfaceSystem.class, priority = Integer.MAX_VALUE)
public abstract class BiomeWeGone$SurfaceSystemMixin implements BandsContext {

    @Unique
    private final Map<BandsRuleSource, BlockState[]> bts$bandsLookup = new Reference2ObjectOpenHashMap();
    @Shadow
    @Final
    private PositionalRandomFactory noiseRandom;
    @Shadow
    @Final
    private NormalNoise surfaceSecondaryNoise;

    @Shadow
    protected abstract void erodedBadlandsExtension(BlockColumn var1, int var2, int var3, int var4, LevelHeightAccessor var5);

    @TargetHandler(
            mixin = "dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.surface_system.MixinSurfaceSystem",
            name = "buildSurface"
    )
    @Inject(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/ChunkAccess;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I",
                    ordinal = 1
            )
    )
    private void bts$biomewegone$injectShatteredGlacierExtension(RandomState randomState, BiomeManager biomeManager, Registry<Biome> biomes, boolean useLegacyRandomSource, WorldGenerationContext context, ChunkAccess chunk, NoiseChunk noiseChunk, SurfaceRules.RuleSource ruleSource, CallbackInfo ci, @Local BlockColumn blockColumn, @Local(ordinal = 4) int m, @Local(ordinal = 5) int n, @Local(ordinal = 6) int o, @Local Holder<Biome> holder) {
        if (holder.is(BWGBiomes.SHATTERED_GLACIER) || holder.is(BWGBiomes.ERODED_BOREALIS)) {
            this.erodedBadlandsExtension(blockColumn, m, n, o, chunk);
        }

    }

    public BlockState getBandsState(BandsRuleSource bandsRuleSource, SimpleWeightedRandomList<BlockState> bandStates, IntProvider bandSizeProvider, IntProvider bandsCountProvider, int x, int y, int z, float frequency, int noiseScale) {
        final BlockState[] blockStates = bts$bandsLookup.computeIfAbsent(bandsRuleSource, key -> {
            final List<BlockState> states = new ArrayList<>();
            final RandomSource random = this.noiseRandom.at(BlockPos.ZERO);
            final int bandsCount = bandsCountProvider.sample(random);

            for (int bandIdx = 0; bandIdx < bandsCount; bandIdx++) {
                final int bandSize = bandSizeProvider.sample(random);
                final BlockState state = bandStates.getRandomValue(random).orElseThrow();
                for (int size = 0; size < bandSize; size++) {
                    states.add(state);
                }
            }
            return states.toArray(new BlockState[0]);


        });

        double scaledNoise = this.surfaceSecondaryNoise.getValue(x * frequency, 0, z * frequency) * noiseScale;
        int stateIndex = Math.floorMod(y + Math.round(scaledNoise), blockStates.length - 1);
        return blockStates[stateIndex];
    }
}
