package dev.behindthescenery.btsengineconcurrent;

public abstract class ModLoaderHook {

    public static ModLoaderHook DefaultHook = null;

    public abstract boolean isModLoaded(String modId);
}
