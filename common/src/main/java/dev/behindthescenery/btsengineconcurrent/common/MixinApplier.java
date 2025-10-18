package dev.behindthescenery.btsengineconcurrent.common;

public record MixinApplier(String modClassPath, Param[] mixins) {

    public boolean hasDisableMixin(String mixin) {
        for (Param param : mixins) {
            if(param.mixinDisable.equals(mixin))
                return true;
        }

        return false;
    }

    public boolean hasMixin(String mixin) {
        for (Param param : mixins) {
            if(param.mixinClass.equals(mixin))
                return true;
        }

        return false;
    }

    public boolean isModLoaded() {
        if(modClassPath.isEmpty()) return true;

        try {
            Class.forName(modClassPath, false, getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (LinkageError e) {
            return true;
        }
    }

    public record Param(String mixinClass, String mixinDisable) {}
}
