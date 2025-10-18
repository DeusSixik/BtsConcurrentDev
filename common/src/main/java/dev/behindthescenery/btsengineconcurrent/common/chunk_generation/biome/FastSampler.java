package dev.behindthescenery.btsengineconcurrent.common.chunk_generation.biome;

import dev.behindthescenery.btsengineconcurrent.common.utils.BtsSinglePointContext;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

public final class FastSampler {

    private final DensityFunction[] funcs;
    private final BtsSinglePointContext ctx;

    public FastSampler(Climate.Sampler sampler) {
        this.funcs = new DensityFunction[]{
                sampler.temperature(),
                sampler.humidity(),
                sampler.continentalness(),
                sampler.erosion(),
                sampler.depth(),
                sampler.weirdness()
        };
        this.ctx = new BtsSinglePointContext(0, 0, 0);
    }

    public void sample(int x, int y, int z, float[] out) {
        ctx.update(x, y, z);
        for (int i = 0; i < funcs.length; i++) {
            out[i] = (float) funcs[i].compute(ctx);
        }
    }
}
