package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core;

import dev.behindthescenery.btsengineconcurrent.BtsEngineConcurrentMixinPluginCommon;
import dev.behindthescenery.btsengineconcurrent.ModLoaderHook;
import net.neoforged.fml.ModList;

public class BtsEngineConcurrentMixinPluginNeoForge extends BtsEngineConcurrentMixinPluginCommon {

    @Override
    public void onLoad(String s) {
        ModLoaderHook.DefaultHook = new ModLoaderHook() {
            @Override
            public boolean isModLoaded(String modId) {
                return ModList.get().isLoaded(modId);
            }
        };

        super.onLoad(s);
    }
}
