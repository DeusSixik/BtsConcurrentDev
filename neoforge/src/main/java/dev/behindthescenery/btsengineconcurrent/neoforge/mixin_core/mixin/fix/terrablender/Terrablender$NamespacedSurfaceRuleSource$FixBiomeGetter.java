package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.fix.terrablender;


import dev.behindthescenery.btsengineconcurrent.common.path.rework_chunk_generation.surface_rules.SurfaceRulesContextPath;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(targets = "terrablender.worldgen.surface.NamespacedSurfaceRuleSource$NamespacedRule")
public class Terrablender$NamespacedSurfaceRuleSource$FixBiomeGetter {

    @Shadow
    @Final
    private SurfaceRules.Context context;

    @Shadow
    @Final
    private Map<String, SurfaceRules.SurfaceRule> rules;

    @Shadow
    @Final
    private SurfaceRules.SurfaceRule baseRule;

    /**
     * @author Sixik
     * @reason Redirect biome Getter to Cached
     */
    @Overwrite
    public BlockState tryApply(int x, int y, int z) {
        final Holder<Biome> biome = ((SurfaceRulesContextPath)this.context).bts$getBiome();

        BlockState state = null;
        if (biome.is(key -> this.rules.containsKey(key.location().getNamespace())))
            state = this.rules.get(biome.unwrapKey().get().location().getNamespace()).tryApply(x, y, z);

        if (state == null)
            state = this.baseRule.tryApply(x, y, z);

        return state;
    }
}
