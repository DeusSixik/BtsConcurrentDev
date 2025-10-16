package dev.behindthescenery.btsengineconcurrent.fabric;

import dev.behindthescenery.btsengineconcurrent.BtsEngineConcurrent;
import dev.behindthescenery.btsengineconcurrent.ModLoaderHook;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class BtsEngineConcurrentFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        BtsEngineConcurrent.init();
    }
}
