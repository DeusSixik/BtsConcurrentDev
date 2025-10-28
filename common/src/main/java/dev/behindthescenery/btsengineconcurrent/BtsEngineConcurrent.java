package dev.behindthescenery.btsengineconcurrent;

import com.mojang.logging.LogUtils;
import dev.sdm.profiler.TracyProfiler;
import org.slf4j.Logger;

public final class BtsEngineConcurrent {

    public static final String MOD_ID = "btsengineconcurrent";
    public static final Logger LOGGER = LogUtils.getLogger();



    public static void init() {
        TracyProfiler.setLogger(LOGGER::info);
    }
}
