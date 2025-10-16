package dev.behindthescenery.btsengineconcurrent.neoforge;

import dev.behindthescenery.btsengineconcurrent.BtsEngineConcurrent;
import dev.behindthescenery.btsengineconcurrent.ModLoaderHook;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;

@Mod(BtsEngineConcurrent.MOD_ID)
public final class BtsEngineConcurrentNeoForge {
    public BtsEngineConcurrentNeoForge() {
        ModLoaderHook.DefaultHook = new ModLoaderHook() {
            @Override
            public boolean isModLoaded(String modId) {
                return ModList.get().isLoaded(modId);
            }
        };

        BtsEngineConcurrent.init();
    }
}
