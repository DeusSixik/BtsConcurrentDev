package dev.behindthescenery.btsengineconcurrent.common.mixin.profiler;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.behindthescenery.btsengineconcurrent.common.profiler.BtsProfilerSettings;
import dev.behindthescenery.btsengineconcurrent.common.profiler.BtsProfilerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ticks.LevelTicks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.BiConsumer;

@Mixin(ServerLevel.class)
public class MixinServerLevel$Profiler {

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/ticks/LevelTicks;tick(JILjava/util/function/BiConsumer;)V", ordinal = 0))
    public <T> void  bts$tick$blockTicks(LevelTicks<T> instance, long gameTime, int maxAllowedTicks, BiConsumer<BlockPos, T> ticker, Operation<Void> original) {
        final String name = "BlockTicks";

        BtsProfilerUtils.startZone(name, BtsProfilerSettings.Type.BlockTick);
        original.call(instance, gameTime, maxAllowedTicks, ticker);
        BtsProfilerUtils.endZone(name, BtsProfilerSettings.Type.BlockTick);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/ticks/LevelTicks;tick(JILjava/util/function/BiConsumer;)V", ordinal = 1))
    public <T> void  bts$tick$fluidTicks(LevelTicks<T> instance, long gameTime, int maxAllowedTicks, BiConsumer<BlockPos, T> ticker, Operation<Void> original) {
        final String name = "FluidTicks";

        BtsProfilerUtils.startZone(name, BtsProfilerSettings.Type.BlockTick);
        original.call(instance, gameTime, maxAllowedTicks, ticker);
        BtsProfilerUtils.endZone(name, BtsProfilerSettings.Type.BlockTick);
    }
}
