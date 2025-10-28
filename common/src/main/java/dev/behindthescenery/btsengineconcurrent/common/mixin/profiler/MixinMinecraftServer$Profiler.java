package dev.behindthescenery.btsengineconcurrent.common.mixin.profiler;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.behindthescenery.btsengineconcurrent.common.profiler.BtsProfilerSettings;
import dev.behindthescenery.btsengineconcurrent.common.profiler.BtsProfilerUtils;
import dev.behindthescenery.btsengineconcurrent.common.profiler.ProfilerThread;
import dev.sdm.profiler.TracyProfiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer$Profiler {

    @WrapOperation(method = "tickChildren", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;tick(Ljava/util/function/BooleanSupplier;)V"))
    public void bts$tickChildren(ServerLevel instance, BooleanSupplier hasTimeLeft, Operation<Void> original) {
        final String name = "Level: " + instance.dimension().location();

        BtsProfilerUtils.startZone(name, BtsProfilerSettings.Type.GlobalTick);
        original.call(instance, hasTimeLeft);
        BtsProfilerUtils.endZone(name, BtsProfilerSettings.Type.GlobalTick);
    }

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;startMetricsRecordingTick()V"))
    public void bts$runServer$startZone(CallbackInfo ci) {
        if(BtsProfilerUtils.isStarted) {
            BtsProfilerUtils.startZone("ServerTick", BtsProfilerSettings.Type.GlobalTick);
        }
    }

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/jfr/JvmProfiler;onServerTick(F)V", shift = At.Shift.AFTER))
    public void bts$runServer$makeFrame(CallbackInfo ci) {
        if(BtsProfilerUtils.isConnected()) {
            BtsProfilerUtils.endZone("ServerTick", BtsProfilerSettings.Type.GlobalTick);
            TracyProfiler.markFrame();
            BtsProfilerUtils.isStarted = true;
        } else BtsProfilerUtils.isStarted = false;
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    public void bts$stopServer$start(CallbackInfo ci) {
        if(BtsProfilerUtils.isStarted) {
            TracyProfiler.startZone("StopServer");
        }
    }

    @Inject(method = "stopServer", at = @At("RETURN"))
    public void bts$stopServer$end(CallbackInfo ci) {
        if(BtsProfilerUtils.isConnected()) {
            TracyProfiler.endZone("StopServer");
            ProfilerThread.stopThread();
            BtsProfilerUtils.isStarted = false;
        }
    }
}
