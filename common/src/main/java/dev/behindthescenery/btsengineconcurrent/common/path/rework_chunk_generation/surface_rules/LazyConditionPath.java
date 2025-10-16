package dev.behindthescenery.btsengineconcurrent.common.path.rework_chunk_generation.surface_rules;

import net.minecraft.world.level.levelgen.SurfaceRules;

public interface LazyConditionPath {

    void bts$updateContext(SurfaceRules.Context context);
}
