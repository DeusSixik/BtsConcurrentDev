package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.structures;

import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Consumer;

@Mixin(ChunkGenerator.class)
public abstract class MixinChunkGenerator$StructuresCreate {

    /**
     * @author Sixik
     * @reason Using parallel stream
     */
    @Redirect(
            method = { "createStructures", "createReferences" },
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V")
    )
    public <T> void bts$methods$parallelStream(List<T> instance, Consumer<? super T> consumer) {
        instance.parallelStream().forEach(consumer);
    }
}
