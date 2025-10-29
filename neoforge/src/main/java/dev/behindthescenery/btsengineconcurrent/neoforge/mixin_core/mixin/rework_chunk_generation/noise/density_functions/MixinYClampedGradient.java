package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.noise.density_functions;

import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions.YClampedGradient")
public class MixinYClampedGradient {

    @Shadow @Final private int fromY;
    @Shadow @Final private int toY;
    @Shadow @Final private double fromValue;
    @Shadow @Final private double toValue;

    @Unique
    private double bts$invDy;
    @Unique private double bts$dv;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void bts$init(CallbackInfo ci) {
        this.bts$invDy = 1.0d / (double)(this.toY - this.fromY);
        this.bts$dv = this.toValue - this.fromValue;
    }


    /**
     * @author Sixik
     * @reason Use preliminary calculations
     */
    @Overwrite
    public double compute(DensityFunction.FunctionContext pos) {
        final int y = pos.blockY();
        if (y <= this.fromY) return this.fromValue;
        if (y >= this.toY) return this.toValue;
        return this.fromValue + (y - this.fromY) * this.bts$invDy * this.bts$dv;
    }
}
