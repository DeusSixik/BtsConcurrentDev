package dev.behindthescenery.btsengineconcurrent.common.path.rework_chunk_generation.surface_rules;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public interface SurfaceRulesContextPath {

    Holder<Biome> bts$getBiome();


    int[][] bts$getHeightCache();

}
