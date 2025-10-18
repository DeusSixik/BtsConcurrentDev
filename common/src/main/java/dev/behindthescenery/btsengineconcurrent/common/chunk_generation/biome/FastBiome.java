package dev.behindthescenery.btsengineconcurrent.common.chunk_generation.biome;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public record FastBiome(Holder<Biome> biome, float[] params) {
}
