package dev.behindthescenery.btsengineconcurrent;

import dev.behindthescenery.btsengineconcurrent.common.MixinApplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
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
        create("team.creative.enhancedvisuals.EnhancedVisuals", new MixinApplier.Param(
                "dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.fix.enhancedvisuals.EnhancedVisuals$ExplosionMixin",
                "team.creative.enhancedvisuals.mixin.ExplosionMixin"
        ));
        create("", new MixinApplier.Param(
                "dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.fix.moonrise.LevelMixin",
                "ca.spottedleaf.moonrise.mixin.chunk_system.LevelMixin"
        ));
        create("com.copycatsplus.copycats.Copycats", new MixinApplier.Param(
                "dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.fix.copycat.CopyCats$LiquidBlockRendererMixin$Fix",
                "com.copycatsplus.copycats.mixin.foundation.copycat.LiquidBlockRendererMixin"
        ));
        create("com.cupboard.Cupboard", new MixinApplier.Param(
                "",
                "com.cupboard.mixin.ChunkLoadDebug"
        ));
        create("com.terraformersmc.biolith.impl.Biolith", new MixinApplier.Param(
                "dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.fix.biolith.Biolith$MixinSurfaceBuilder",
                "com.terraformersmc.biolith.impl.mixin.MixinSurfaceBuilder"
        ));
        create("net.hibiscus.naturespirit.NatureSpirit", new MixinApplier.Param(
                "dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.fix.naturespirit.NaturesSpirit$SurfaceBuilderMixin",
                "net.hibiscus.naturespirit.mixin.SurfaceBuilderMixin"
        ));
        create("net.potionstudios.biomeswevegone.BiomesWeveGone", new MixinApplier.Param(
                "dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.fix.biomewegone.BiomeWeGone$SurfaceSystemMixin",
                "net.potionstudios.biomeswevegone.mixin.SurfaceSystemMixin"
        ));
        create("com.teamabnormals.environmental.core.Environmental", new MixinApplier.Param(
                "dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.fix.environmental.Environmental$SurfaceSystemMixin",
                "com.teamabnormals.environmental.core.mixin.SurfaceSystemMixin"
        ));
    }

    public void create(String modClass, MixinApplier.Param... params) {
        mixinAppliers.add(new MixinApplier(modClass, params));
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
