package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.carver;

import com.mojang.serialization.Codec;
import dev.behindthescenery.btsengineconcurrent.common.chunk_generation.carver.CaveWorldCarverSkipChecker;
import dev.behindthescenery.btsengineconcurrent.common.utils.FastRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Function;

@Mixin(CaveWorldCarver.class)
public abstract class MixinCaveWorldCarver extends WorldCarver<CaveCarverConfiguration> {

    @Shadow
    protected abstract int getCaveBound();

    @Shadow
    protected abstract void createRoom(CarvingContext context, CaveCarverConfiguration config, ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeAccessor, Aquifer aquifer, double x, double y, double z, float radius, double horizontalVerticalRatio, CarvingMask carvingMask, CarveSkipChecker skipChecker);

    @Shadow
    protected abstract float getThickness(RandomSource random);

    @Shadow
    protected abstract double getYScale();

    @Unique
    private static final float TWO_PI = Mth.PI * 2F;
    @Unique
    private static final float DIVIDE_PI = Mth.PI / 2;

    private final CaveWorldCarverSkipChecker skipChecker = new CaveWorldCarverSkipChecker();

    public MixinCaveWorldCarver(Codec<CaveCarverConfiguration> codec) {
        super(codec);
    }

    /**
     * @author Sixik
     * @reason Optimize CaveWorldCarverSkipChecker and operation with Mth.PI
     */
    @Overwrite
    public boolean carve(CarvingContext context, CaveCarverConfiguration config, ChunkAccess chunk,
                         Function<BlockPos, Holder<Biome>> biomeAccessor, RandomSource random,
                         Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask) {

        final FastRandom fastRandom = FastRandom.current();
        final int i = SectionPos.sectionToBlockCoord(this.getRange() * 2 - 1);
        final int j = fastRandom.nextInt(fastRandom.nextInt(fastRandom.nextInt(this.getCaveBound()) + 1) + 1);

        for(int k = 0; k < j; ++k) {
            final double d = chunkPos.getBlockX(fastRandom.nextInt(16));
            final double e = config.y.sample(random, context);
            final double f = chunkPos.getBlockZ(fastRandom.nextInt(16));
            final double g = config.horizontalRadiusMultiplier.sample(random);
            final double h = config.verticalRadiusMultiplier.sample(random);
            final double l = config.floorLevel.sample(random);
            final CaveWorldCarverSkipChecker checker = skipChecker;
            checker.updateY(l);
            int m = 1;
            if (fastRandom.nextInt(4) == 0) {
                final double n = config.yScale.sample(random);
                final float o = 1.0F + fastRandom.nextFloat() * 6.0F;
                this.createRoom(context, config, chunk, biomeAccessor, aquifer, d, e, f, o, n, carvingMask, checker);
                m += fastRandom.nextInt(4);
            }

            for(int p = 0; p < m; ++p) {
                final float q = fastRandom.nextFloat() * TWO_PI;
                final float o = (fastRandom.nextFloat() - 0.5F) / 4.0F;
                final float r = this.getThickness(random);
                final int s = i - fastRandom.nextInt(i / 4);
                this.createTunnel(context, config, chunk, biomeAccessor, fastRandom.nextLong(),
                        aquifer, d, e, f, g, h, r, q, o, 0, s,
                        this.getYScale(), carvingMask, checker);
            }
        }

        return true;
    }

    /**
     * @author Sixik
     * @reason Optimize, random
     */
    @Overwrite
    protected void createTunnel(CarvingContext context, CaveCarverConfiguration config, ChunkAccess chunk,
                                Function<BlockPos, Holder<Biome>> biomeAccessor, long seed, Aquifer aquifer,
                                double x, double y, double z, double horizontalRadiusMultiplier,
                                double verticalRadiusMultiplier, float thickness, float yaw, float pitch,
                                int branchIndex, int branchCount, double horizontalVerticalRatio,
                                CarvingMask carvingMask, WorldCarver.CarveSkipChecker skipChecker) {
        final FastRandom fastRandom = FastRandom.currentWithSeed(seed);
        final int i = fastRandom.nextInt(branchCount / 2) + branchCount / 4;
        final boolean bl = fastRandom.nextInt(6) == 0;
        final float multiplyBl = bl ? 0.92F : 0.7F;

        float f = 0.0F;
        float g = 0.0F;

        final float pitchCos = Mth.cos(pitch);
        final float pitchSin = Mth.sin(pitch);
        final float yawCos = Mth.cos(yaw);
        final float yawSin = Mth.sin(yaw);

        for(int j = branchIndex; j < branchCount; ++j) {
            final double d = 1.5 + Mth.sin(3.141592653589793f * j / branchCount) * thickness;
            final double e = d * horizontalVerticalRatio;
            x += yawCos * pitchCos;
            y += pitchSin;
            z += yawSin * pitchCos;
            pitch *= multiplyBl;
            pitch += g * 0.1F;
            yaw += f * 0.1F;
            g *= 0.9F;
            f *= 0.75F;
            g += (fastRandom.nextFloat() - fastRandom.nextFloat()) * fastRandom.nextFloat() * 2.0F;
            f += (fastRandom.nextFloat() - fastRandom.nextFloat()) * fastRandom.nextFloat() * 4.0F;
            if (j == i && thickness > 1.0F) {
                this.createTunnel(context, config, chunk, biomeAccessor,
                        fastRandom.nextLong(), aquifer, x, y, z, horizontalRadiusMultiplier,
                        verticalRadiusMultiplier, fastRandom.nextFloat() * 0.5F + 0.5F,
                        yaw - DIVIDE_PI, pitch / 3.0F, j, branchCount,
                        1.0F, carvingMask, skipChecker);

                this.createTunnel(context, config, chunk, biomeAccessor,
                        fastRandom.nextLong(), aquifer, x, y, z, horizontalRadiusMultiplier,
                        verticalRadiusMultiplier, fastRandom.nextFloat() * 0.5F + 0.5F,
                        yaw + DIVIDE_PI, pitch / 3.0F, j, branchCount,
                        1.0F, carvingMask, skipChecker);

                return;
            }

            if (fastRandom.nextInt(4) != 0) {
                if (!canReach(chunk.getPos(), x, z, j, branchCount, thickness)) {
                    return;
                }

                this.carveEllipsoid(context, config, chunk, biomeAccessor,
                        aquifer, x, y, z, d * horizontalRadiusMultiplier,
                        e * verticalRadiusMultiplier, carvingMask, skipChecker);
            }
        }
    }
}
