package dev.behindthescenery.btsengineconcurrent.common.chunk_generation.surface_rules;

import dev.behindthescenery.btsengineconcurrent.common.path.rework_chunk_generation.surface_rules.LazyConditionPath;
import dev.behindthescenery.btsengineconcurrent.common.path.rework_chunk_generation.surface_rules.SurfaceRulesContextPath;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public class SurfaceRuleConditions {

    protected interface ConditionUpdater<T> {
        default void updateData(T source, SurfaceRules.Context context) {
            ((LazyConditionPath)this).bts$updateContext(context);
            updateData(source);
        }

        void updateData(T source);
    }

    public static class StoneDepthCondition extends SurfaceRules.LazyYCondition implements ConditionUpdater<SurfaceRules.StoneDepthCheck> {

        private SurfaceRules.StoneDepthCheck stoneDepthCheck;

        public StoneDepthCondition(SurfaceRules.StoneDepthCheck stoneDepthCheck, SurfaceRules.Context context) {
            super(context);
            this.stoneDepthCheck = stoneDepthCheck;
        }

        public void updateData(SurfaceRules.StoneDepthCheck check) {
            this.stoneDepthCheck = check;
        }

        protected boolean compute() {
            final boolean bl = stoneDepthCheck.surfaceType() == CaveSurface.CEILING;

            final int i = bl ? this.context.stoneDepthBelow : this.context.stoneDepthAbove;
            final int j = stoneDepthCheck.addSurfaceDepth ? this.context.surfaceDepth : 0;

            final double sec = this.context.getSurfaceSecondary();
            final int k = stoneDepthCheck.secondaryDepthRange == 0
                    ? 0
                    : (int)((sec + 1.0) * 0.5 * stoneDepthCheck.secondaryDepthRange);

            return i <= 1 + stoneDepthCheck.offset + j + k;
        }
    }

    public static class YCondition extends SurfaceRules.LazyYCondition implements ConditionUpdater<SurfaceRules.YConditionSource> {
        private SurfaceRules.YConditionSource source;

        public YCondition(SurfaceRules.YConditionSource source, SurfaceRules.Context context) {
            super(context);
            this.source = source;
        }

        protected boolean compute() {
            return this.context.blockY + (source.addStoneDepth ? this.context.stoneDepthAbove : 0) >= source.anchor.resolveY(this.context.context) + this.context.surfaceDepth * source.surfaceDepthMultiplier;
        }

        @Override
        public void updateData(SurfaceRules.YConditionSource source) {
            this.source = source;
        }
    }

    public static class WaterCondition extends SurfaceRules.LazyYCondition implements ConditionUpdater<SurfaceRules.WaterConditionSource> {
        private SurfaceRules.WaterConditionSource source;

        public WaterCondition(SurfaceRules.WaterConditionSource source, SurfaceRules.Context context) {
            super(context);
            this.source = source;
        }

        protected boolean compute() {
            return this.context.waterHeight == Integer.MIN_VALUE || this.context.blockY + (source.addStoneDepth() ? this.context.stoneDepthAbove : 0) >= this.context.waterHeight + source.offset() + this.context.surfaceDepth * source.surfaceDepthMultiplier();
        }

        @Override
        public void updateData(SurfaceRules.WaterConditionSource source) {
            this.source = source;
        }
    }

    public static class NoiseThresholdCondition extends SurfaceRules.LazyXZCondition implements ConditionUpdater<SurfaceRules.NoiseThresholdConditionSource> {
        private SurfaceRules.NoiseThresholdConditionSource source;

        public NoiseThresholdCondition(SurfaceRules.NoiseThresholdConditionSource source, SurfaceRules.Context context) {
            super(context);
            this.source = source;
        }

        protected boolean compute() {
            final double d = context.randomState.getOrCreateNoise(source.noise()).getValue(this.context.blockX, 0.0F, this.context.blockZ);
            return d >= source.minThreshold() && d <= source.maxThreshold();
        }

        @Override
        public void updateData(SurfaceRules.NoiseThresholdConditionSource source) {
            this.source = source;
        }
    }

    public static class VerticalGradientCondition extends SurfaceRules.LazyYCondition implements ConditionUpdater<SurfaceRules.VerticalGradientConditionSource> {
        private SurfaceRules.VerticalGradientConditionSource source;

        public VerticalGradientCondition(SurfaceRules.VerticalGradientConditionSource source, SurfaceRules.Context context) {
            super(context);
            this.source = source;
        }

        protected boolean compute() {
            final PositionalRandomFactory positionalRandomFactory = context.randomState.getOrCreateRandomFactory(source.randomName());

            final int y = this.context.blockY;
            final int yTrue = source.trueAtAndBelow().resolveY(this.context.context);
            final int yFalse = source.falseAtAndAbove().resolveY(this.context.context);

            if (y <= yTrue) return true;
            if (y >= yFalse) return false;

            final double gradient = 1.0 - ((double)(y - yTrue) / (double)(yFalse - yTrue));

            final RandomSource rand = positionalRandomFactory.at(this.context.blockX, y, this.context.blockZ);
            return rand.nextFloat() < gradient;
        }

        @Override
        public void updateData(SurfaceRules.VerticalGradientConditionSource source) {
            this.source = source;
        }
    }

    public static class BiomeCondition extends SurfaceRules.LazyYCondition implements ConditionUpdater<SurfaceRules.BiomeConditionSource> {
        private SurfaceRules.BiomeConditionSource source;

        public BiomeCondition(SurfaceRules.BiomeConditionSource source, SurfaceRules.Context context) {
            super(context);
            this.source = source;
        }

        protected boolean compute() {
            return ((SurfaceRulesContextPath)this.context).bts$getBiome().is(source.biomeNameTest);
        }

        @Override
        public void updateData(SurfaceRules.BiomeConditionSource source) {
            this.source = source;
        }
    }
}
