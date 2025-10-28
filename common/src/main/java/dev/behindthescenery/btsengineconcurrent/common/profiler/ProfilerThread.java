package dev.behindthescenery.btsengineconcurrent.common.profiler;

import dev.behindthescenery.btsengineconcurrent.BtsEngineConcurrent;
import dev.sdm.profiler.TracyProfiler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ProfilerThread extends Thread {

    private static ProfilerThread profilerThread;

    public static void startThread() {
        startThread(null);
    }

    public static void startThread(@Nullable CommandSourceStack source) {
        profilerThread = new ProfilerThread(source);
        profilerThread.start();
    }

    public static void stopThread() {
        if(profilerThread != null) {
            profilerThread.interrupt();
        }

        if(BtsProfilerUtils.isConnected()) {
            try {
                TracyProfiler.disconnect();
            } catch (Exception e) {
                BtsEngineConcurrent.LOGGER.error(e.getMessage(), e);
            }
        }
    }

    protected ProfilerThread(@Nullable CommandSourceStack source) {
        super(() -> {
            if(BtsProfilerUtils.isConnected()) {

                if(source != null) source.sendFailure(Component.literal("Can't start profiler. Profiler already started!"));
                else BtsEngineConcurrent.LOGGER.error("Can't start profiler. Profiler already started!");
                return;
            }

            try {
                TracyProfiler.startConnection();
            } catch (Exception e) {
                if(source != null) source.sendFailure(Component.literal("Can't start profiler. May be [C++] TracySocketServer not started!"));
                else BtsEngineConcurrent.LOGGER.error("Can't start profiler. May be [C++] TracySocketServer not started!", e);
            }
        });
    }
}
