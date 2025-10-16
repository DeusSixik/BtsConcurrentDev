package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core;

import com.bawnorton.mixinsquared.api.MixinCanceller;
import dev.behindthescenery.btsengineconcurrent.BtsEngineConcurrentMixinPluginCommon;
import dev.behindthescenery.btsengineconcurrent.common.MixinApplier;

import java.util.List;

public class NeoForgeConcurrentMixinCanceller implements MixinCanceller {

    @Override
    public boolean shouldCancel(List<String> list, String s) {
        for (MixinApplier mixinApplier : BtsEngineConcurrentMixinPluginCommon.mixinAppliers) {
            if(mixinApplier.hasDisableMixin(s) && mixinApplier.isModLoaded())
                return true;
        }

        return false;
    }
}
