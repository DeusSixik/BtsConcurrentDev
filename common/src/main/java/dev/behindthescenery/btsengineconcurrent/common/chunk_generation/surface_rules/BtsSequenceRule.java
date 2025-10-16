package dev.behindthescenery.btsengineconcurrent.common.chunk_generation.surface_rules;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record BtsSequenceRule(List<SurfaceRules.SurfaceRule> rules) implements SurfaceRules.SurfaceRule {
    @Nullable
    public BlockState tryApply(int i, int j, int k) {
        for(SurfaceRules.SurfaceRule surfaceRule : this.rules) {
            final BlockState blockState = surfaceRule.tryApply(i, j, k);
            if (blockState != null) {
                return blockState;
            }
        }

        return null;
    }
}
