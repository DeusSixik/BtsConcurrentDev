package dev.behindthescenery.btsengineconcurrent.common.path.rework_chunk_generation.biome;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;

public interface PathBiomeSource$BiomeGetter {

    default Holder<Biome> bts$getNoiseBiomeFast(int x, int y, int z, Climate.Sampler sampler) {
        return ((BiomeResolver)this).getNoiseBiome(x, y, z, sampler);
    }
}
