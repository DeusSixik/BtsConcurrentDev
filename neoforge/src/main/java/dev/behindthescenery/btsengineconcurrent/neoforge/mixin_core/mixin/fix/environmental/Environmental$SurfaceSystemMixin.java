package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.fix.environmental;

import com.bawnorton.mixinsquared.TargetHandler;
import com.teamabnormals.environmental.core.registry.datapack.EnvironmentalBiomes;
import com.teamabnormals.environmental.core.registry.datapack.EnvironmentalNoiseParameters;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = SurfaceSystem.class, priority = Integer.MAX_VALUE)
public abstract class Environmental$SurfaceSystemMixin {

    @Unique
    private NormalNoise bts$enviromental$pineBarrensStoneNoise;
    @Unique
    private int[][][] bts$enviromental$pineBarrensStoneRaises;
    @Unique
    private boolean bts$enviromental$raisePineBarrensStone;
    @Shadow
    @Final
    private NormalNoise surfaceNoise;

    @Inject(
            method = {"<init>"},
            at = {@At("TAIL")}
    )
    private void bts$enviromental$SurfaceSystem(RandomState randomState, BlockState defaultBlock, int seaLevel, PositionalRandomFactory noiseRandom, CallbackInfo ci) {
        this.bts$enviromental$pineBarrensStoneNoise = randomState.getOrCreateNoise(EnvironmentalNoiseParameters.PINE_BARRENS_STONE);
        this.bts$enviromental$pineBarrensStoneRaises = new int[16][16][3];
    }

    @TargetHandler(
            mixin = "dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.surface_system.MixinSurfaceSystem",
            name = "buildSurface"
    )
    @Inject(
            method = "@MixinSquared:Handler",
            at = {@At("HEAD")}
    )
    private void bts$enviromental$buildSurface(RandomState randomState, BiomeManager biomeManager, Registry<Biome> registry, boolean useLegacyRandomSource, WorldGenerationContext context, ChunkAccess chunkAccess, NoiseChunk noiseChunk, SurfaceRules.RuleSource ruleSource, CallbackInfo ci) {
        this.bts$enviromental$raisePineBarrensStone = false;
    }

    @TargetHandler(
            mixin = "dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.surface_system.MixinSurfaceSystem",
            name = "buildSurface"
    )
    @Inject(
            method = {"@MixinSquared:Handler"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/biome/BiomeManager;getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;",
                    shift = At.Shift.BEFORE,
                    ordinal = 0
            )},
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void bts$enviromental$collectRaises(RandomState randomState,
                                                BiomeManager biomeManager,
                                                Registry<Biome> registry,
                                                boolean useLegacyRandomSource,
                                                WorldGenerationContext context,
                                                ChunkAccess chunkAccess,
                                                NoiseChunk noiseChunk,
                                                SurfaceRules.RuleSource ruleSource,
                                                CallbackInfo ci,
                                                ChunkPos chunkPos,
                                                int i,
                                                int j,
                                                LevelHeightAccessor heightView,
                                                int bottomY,
                                                BlockPos.MutableBlockPos mutable,
                                                BlockPos.MutableBlockPos mutable1,
                                                BlockColumn blockColumn,
                                                SurfaceRules.Context surfaceRulesContext,
                                                SurfaceRules.SurfaceRule surfaceRule,
                                                int k,
                                                int i1,
                                                int l,
                                                int j1,
                                                int surfaceY) {
        int y = chunkAccess.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, k, l) + 1;
        Holder<Biome> biome = biomeManager.getBiome(mutable1.set(i1, y - 1, j1));
        if (!biome.is(EnvironmentalBiomes.PINE_BARRENS) && !biome.is(EnvironmentalBiomes.SNOWY_PINE_BARRENS) && !biome.is(EnvironmentalBiomes.OLD_GROWTH_PINE_BARRENS) && !biome.is(EnvironmentalBiomes.SNOWY_OLD_GROWTH_PINE_BARRENS)) {
            this.bts$enviromental$pineBarrensStoneRaises[k][l] = null;
        } else {
            double noise = this.bts$enviromental$getNoiseAt(i1, j1);
            boolean flag = noise > (double)0.0F;
            this.bts$enviromental$pineBarrensStoneRaises[k][l] = new int[]{y, y + bts$enviromental$getRaise(noise), flag ? 1 : 0};
            if (flag) {
                this.bts$enviromental$raisePineBarrensStone = true;
            }
        }

    }

    @TargetHandler(
            mixin = "dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.surface_system.MixinSurfaceSystem",
            name = "buildSurface"
    )
    @Inject(
            method = {"@MixinSquared:Handler"},
            at = {@At("TAIL")}
    )
    private void bts$enviromental$generateRaisedStone(RandomState randomState, BiomeManager biomeManager, Registry<Biome> registry, boolean useLegacyRandomSource, WorldGenerationContext context, final ChunkAccess chunk, NoiseChunk noiseChunk, SurfaceRules.RuleSource ruleSource, CallbackInfo ci) {
        final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        final ChunkPos chunkPos = chunk.getPos();
        int i = chunkPos.getMinBlockX();
        int j = chunkPos.getMinBlockZ();
        BlockColumn blockColumn = new BlockColumn() {
            public BlockState getBlock(int y) {
                return chunk.getBlockState(mutable.setY(y));
            }

            public void setBlock(int y, BlockState state) {
                LevelHeightAccessor heightAccessor = chunk.getHeightAccessorForGeneration();
                if (y >= heightAccessor.getMinBuildHeight() && y < heightAccessor.getMaxBuildHeight()) {
                    chunk.setBlockState(mutable.setY(y), state, false);
                    if (!state.getFluidState().isEmpty()) {
                        chunk.markPosForPostprocessing(mutable);
                    }
                }

            }

            public String toString() {
                return "ChunkBlockColumn " + String.valueOf(chunkPos);
            }
        };
        if (this.bts$enviromental$raisePineBarrensStone) {
            for(int x = 0; x < 16; ++x) {
                for(int z = 0; z < 16; ++z) {
                    int[] raise = this.bts$enviromental$pineBarrensStoneRaises[x][z];
                    if (raise != null && raise[2] != 0) {
                        int y = raise[0];
                        int y1 = raise[1];
                        int m = 0;
                        mutable.setX(x + i).setZ(z + j);

                        for(Direction direction : Direction.Plane.HORIZONTAL) {
                            int x1 = x + direction.getStepX();
                            int z1 = z + direction.getStepZ();
                            if (x1 >= 0 && x1 < 16 && z1 >= 0 && z1 < 16) {
                                int[] raise1 = this.bts$enviromental$pineBarrensStoneRaises[x1][z1];
                                if (raise1 == null) {
                                    m = 0;
                                    --y1;
                                    break;
                                }

                                if (raise1[1] > y1) {
                                    ++m;
                                } else if (raise1[1] < y1) {
                                    --m;
                                }
                            }
                        }

                        if (m >= 3) {
                            ++y1;
                        } else if (m <= -3) {
                            --y1;
                        }

                        for(int n = y; n < y1; ++n) {
                            blockColumn.setBlock(n, Blocks.STONE.defaultBlockState());
                        }
                    }
                }
            }
        }

    }

    @Unique
    private double bts$enviromental$getNoiseAt(int x, int y) {
        double d0 = this.surfaceNoise.getValue(x, 0.0F, y);
        double d1 = this.bts$enviromental$pineBarrensStoneNoise.getValue(x, 0.0F, y);
        double d2 = Math.max(d0 - 0.36363636363636365, 0.0F);
        double d3 = d1 < (double)0.0F ? Math.max(-d1 - 0.15151515151515152, 0.0F) : Math.max(d1 - 0.15151515151515152, 0.0F);
        return Math.min(d2, d3);
    }

    @Unique
    private static int bts$enviromental$getRaise(double noise) {
        return noise < 0.06060606060606061 ? 0 : (noise < 0.2181818181818182 ? 1 : 2);
    }
}
