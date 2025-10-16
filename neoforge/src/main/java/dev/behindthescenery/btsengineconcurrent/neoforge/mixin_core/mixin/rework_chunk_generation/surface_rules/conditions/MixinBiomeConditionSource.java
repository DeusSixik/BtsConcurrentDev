package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.surface_rules.conditions;

import dev.behindthescenery.btsengineconcurrent.ReflectionsUtils;
import dev.behindthescenery.btsengineconcurrent.common.chunk_generation.surface_rules.SurfaceRuleConditions;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceRules$BiomeConditionSource")
public class MixinBiomeConditionSource {

    @Unique
    private final ThreadLocal<SurfaceRuleConditions.BiomeCondition> bts$local_condition = new ThreadLocal<>();

    /**
     * @author Sixik
     * @reason Cached Condition
     */
    @Overwrite
    public SurfaceRules.Condition apply(final SurfaceRules.Context context) {
        SurfaceRuleConditions.BiomeCondition condition = bts$local_condition.get();

        if(condition == null)
            bts$local_condition.set(condition = new SurfaceRuleConditions.BiomeCondition(ReflectionsUtils.cast(this), context));
        else {
            condition.updateData(ReflectionsUtils.cast(this), context);
        }

        return condition;
    }
}
