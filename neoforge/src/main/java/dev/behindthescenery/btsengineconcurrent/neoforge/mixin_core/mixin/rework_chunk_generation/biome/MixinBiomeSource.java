package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.biome;

import dev.behindthescenery.btsengineconcurrent.common.path.rework_chunk_generation.biome.PathBiomeSource$BiomeGetter;
import net.minecraft.world.level.biome.BiomeSource;
import org.spongepowered.asm.mixin.Mixin;

@Deprecated
@Mixin(BiomeSource.class)
public class MixinBiomeSource implements PathBiomeSource$BiomeGetter { }
