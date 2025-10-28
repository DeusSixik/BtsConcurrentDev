package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.features;

import dev.behindthescenery.btsengineconcurrent.common.utils.MinecraftObjectsCache;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.BitSet;

@Mixin(OreFeature.class)
public class MixinOreFeature {

    /**
     * @author <a href="https://github.com/RelativityMC/C2ME-fabric/blob/dev/1.21.10/c2me-opts-allocs/src/main/java/com/ishland/c2me/opts/allocs/mixin/object_pooling_caching/MixinOreFeature.java">Author ishland</a>
     * @reason
     */
    @Redirect(method = "doPlace", at = @At(value = "NEW", target = "java/util/BitSet"))
    private BitSet redirectNewBitSet(int nbits) {
        return MinecraftObjectsCache.ORE_GENERATION.getOrCreate(nbits);
    }
}
