package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.noise.density_functions;

import com.mojang.serialization.MapCodec;
import dev.behindthescenery.btsengineconcurrent.common.utils.noise.LerpDensityFunction;
import net.minecraft.core.Registry;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DensityFunctions.class)
public abstract class MixinDensityFunctionTypes {

    @Shadow
    private static MapCodec<? extends DensityFunction> register(Registry<MapCodec<? extends DensityFunction>> registry, String name, KeyDispatchDataCodec<? extends DensityFunction> codec) {
        throw new RuntimeException();
    }

    @Inject(method = "bootstrap", at = @At("RETURN"))
    private static void bts$bootstrap(Registry<MapCodec<? extends DensityFunction>> registry, CallbackInfoReturnable<MapCodec<? extends DensityFunction>> cir) {
        register(registry, "lerp", LerpDensityFunction.CODEC_HOLDER);
    }

    /**
     * @author Sixik
     * @reason Redirect to better implementation
     */
    @Overwrite
    public static DensityFunction lerp(DensityFunction deltaFunction, DensityFunction minFunction, DensityFunction maxFunction) {
        return LerpDensityFunction.create(deltaFunction, minFunction, maxFunction);
    }
}
