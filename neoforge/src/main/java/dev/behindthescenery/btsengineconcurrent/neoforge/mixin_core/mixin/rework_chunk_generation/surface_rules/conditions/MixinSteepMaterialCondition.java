package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.surface_rules.conditions;

import dev.behindthescenery.btsengineconcurrent.common.path.rework_chunk_generation.surface_rules.SurfaceRulesContextPath;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceRules$Context$SteepMaterialCondition")
public abstract class MixinSteepMaterialCondition extends SurfaceRules.LazyXZCondition {

    protected MixinSteepMaterialCondition(SurfaceRules.Context arg) {
        super(arg);
    }

    /**
     * @author Sixik
     * @reason Use cache for generation. Before that, we called out several {@code chunkAccess.getHeight} what was not effective
     */
    @Overwrite
    protected boolean compute() {

        final int i = this.context.blockX & 15;
        final int j = this.context.blockZ & 15;

        final int k = Math.max(j - 1, 0);
        final int l = Math.min(j + 1, 15);
        final int o = Math.max(i - 1, 0);
        final int p = Math.min(i + 1, 15);

        final SurfaceRulesContextPath path = (SurfaceRulesContextPath) this.context;
        final int[][] heights = path.bts$getHeightCache();

        final int m = heights[i][k];
        final int n = heights[i][l];
        if (n >= m + 4) return true;

        final int q = heights[o][j];
        final int r = heights[p][j];

        return q >= r + 4;
    }
}
