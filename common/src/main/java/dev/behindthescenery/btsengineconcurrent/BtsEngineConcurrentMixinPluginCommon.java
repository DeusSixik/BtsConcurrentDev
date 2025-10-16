package dev.behindthescenery.btsengineconcurrent;

import dev.behindthescenery.btsengineconcurrent.common.MixinApplier;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BtsEngineConcurrentMixinPluginCommon implements IMixinConfigPlugin {

    public static List<MixinApplier> mixinAppliers = new ArrayList<>();

    @Override
    public void onLoad(String s) {
        mixinAppliers.add(new MixinApplier("team.creative.enhancedvisuals.EnhancedVisuals", new MixinApplier.Param[] {
                new MixinApplier.Param(
                        "dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.enhancedvisuals.EnhancedVisuals$ExplosionMixin",
                        "team.creative.enhancedvisuals.mixin.ExplosionMixin"
                )
        }));
        mixinAppliers.add(new MixinApplier("", new MixinApplier.Param[]{
                new MixinApplier.Param(
                        "dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.moonrise.LevelMixin",
                        "ca.spottedleaf.moonrise.mixin.chunk_system.LevelMixin"
                )
        }));
        mixinAppliers.add(new MixinApplier("com.copycatsplus.copycats.Copycats", new MixinApplier.Param[]{
            new MixinApplier.Param(
                    "dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.copycat.CopyCats$LiquidBlockRendererMixin$Fix",
                    "com.copycatsplus.copycats.mixin.foundation.copycat.LiquidBlockRendererMixin"
            )
        }));
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {

        for (MixinApplier mixinApplier : mixinAppliers) {
            if(mixinApplier.hasMixin(mixinClassName) && !mixinApplier.isModLoaded())
                return false;
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }

    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }
}
