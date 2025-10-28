package dev.behindthescenery.btsengineconcurrent.common.mixin.profiler;

import dev.behindthescenery.btsengineconcurrent.common.profiler.BtsProfilerUtils;
import dev.behindthescenery.btsengineconcurrent.common.profiler.ProfilerThread;
import dev.sdm.profiler.TracyProfiler;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer$Profiler {

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;startMetricsRecordingTick()V"))
    public void bts$runServer$startZone(CallbackInfo ci) {
        if(BtsProfilerUtils.isStarted) {
            TracyProfiler.startZone("ServerTick");
        }
    }

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/jfr/JvmProfiler;onServerTick(F)V", shift = At.Shift.AFTER))
    public void bts$runServer$makeFrame(CallbackInfo ci) {
        if(BtsProfilerUtils.isConnected()) {
            TracyProfiler.endZone("ServerTick");
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
