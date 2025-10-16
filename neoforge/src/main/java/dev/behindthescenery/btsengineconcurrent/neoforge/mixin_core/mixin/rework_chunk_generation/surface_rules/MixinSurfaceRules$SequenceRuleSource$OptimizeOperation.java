package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.surface_rules;

import com.google.common.collect.ImmutableList;
import dev.behindthescenery.btsengineconcurrent.common.chunk_generation.surface_rules.BtsSequenceRule;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SurfaceRules.SequenceRuleSource.class)
public class MixinSurfaceRules$SequenceRuleSource$OptimizeOperation {

    @Shadow
    @Final
    private List<SurfaceRules.RuleSource> sequence;

    @Unique private SurfaceRules.RuleSource[] bts$rules;
    @Unique private boolean bts$isOneElement;
    @Unique private SurfaceRules.RuleSource bts$firstElement;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void bts$init(List<SurfaceRules.RuleSource> sequence, CallbackInfo ci) {
        this.bts$rules = this.sequence.toArray(SurfaceRules.RuleSource[]::new);
        this.bts$isOneElement = this.bts$rules.length == 1;
        this.bts$firstElement = this.bts$rules.length == 0 ? null : this.bts$rules[0];
    }

    /**
     * @author Sixik
     * @reason
     */
    @Overwrite
    public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
        if (bts$isOneElement) {
            return this.bts$firstElement.apply(context);
        } else {
            final ImmutableList.Builder<SurfaceRules.SurfaceRule> builder = ImmutableList
                    .builderWithExpectedSize(this.bts$rules.length);

            for(SurfaceRules.RuleSource ruleSource : this.bts$rules) {
                builder.add(ruleSource.apply(context));
            }

            return new BtsSequenceRule(builder.build());
        }
    }
}
