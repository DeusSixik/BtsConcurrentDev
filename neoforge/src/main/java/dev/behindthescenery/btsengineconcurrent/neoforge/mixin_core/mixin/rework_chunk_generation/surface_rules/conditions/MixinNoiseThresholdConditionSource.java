package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.surface_rules.conditions;

import dev.behindthescenery.btsengineconcurrent.ReflectionsUtils;
import dev.behindthescenery.btsengineconcurrent.common.chunk_generation.surface_rules.SurfaceRuleConditions;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SurfaceRules.NoiseThresholdConditionSource.class)
public class MixinNoiseThresholdConditionSource {

    @Unique
    private final ThreadLocal<SurfaceRuleConditions.NoiseThresholdCondition> bts$local_condition = new ThreadLocal<>();

    /**
     * @author Sixik
     * @reason Cached Condition
     */
    @Overwrite
    public SurfaceRules.Condition apply(final SurfaceRules.Context context) {
        SurfaceRuleConditions.NoiseThresholdCondition condition = bts$local_condition.get();

        if(condition == null)
            bts$local_condition.set(condition = new SurfaceRuleConditions.NoiseThresholdCondition(ReflectionsUtils.cast(this), context));
        else {
            condition.updateData(ReflectionsUtils.cast(this), context);
        }

        return condition;
    }
}
