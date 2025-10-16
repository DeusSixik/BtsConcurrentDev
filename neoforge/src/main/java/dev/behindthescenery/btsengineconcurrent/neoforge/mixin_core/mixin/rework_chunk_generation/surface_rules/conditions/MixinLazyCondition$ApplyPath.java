package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.surface_rules.conditions;

import dev.behindthescenery.btsengineconcurrent.common.path.rework_chunk_generation.surface_rules.LazyConditionPath;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

@Mixin(SurfaceRules.LazyCondition.class)
public abstract class MixinLazyCondition$ApplyPath implements LazyConditionPath {
    @Mutable
    @Shadow
    @Final
    protected SurfaceRules.Context context;

    @Shadow
    protected abstract long getContextLastUpdate();

    @Shadow
    private long lastUpdate;

    @Shadow
    @Nullable Boolean result;

    @Shadow
    protected abstract boolean compute();

    @Override
    public void bts$updateContext(SurfaceRules.Context context) {
        this.context = context;
    }

    /**
     * @author Sixik
     * @reason Inlining
     */
    @Overwrite
    public boolean test() {
        final long l = this.getContextLastUpdate();
        if (l == this.lastUpdate) {
            if (this.result == null) {
                throw new IllegalStateException("Update triggered but the result is null");
            } else {
                return this.result;
            }
        } else {
            this.lastUpdate = l;
            this.result = this.compute();
            return this.result;
        }
    }
}
