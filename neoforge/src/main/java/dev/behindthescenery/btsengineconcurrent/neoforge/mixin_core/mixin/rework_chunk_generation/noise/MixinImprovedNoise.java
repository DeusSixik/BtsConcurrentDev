package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.noise;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ImprovedNoise.class)
public abstract class MixinImprovedNoise {

    @Unique
    private int[] bts_p;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void bts$init(RandomSource random, CallbackInfo ci) {
        this.bts_p = new int[256];
        for (int i = 0; i < 256; ++i) {
            this.bts_p[i] = i;
        }

        for (int i = 0; i < 256; ++i) {
            int j = random.nextInt(256 - i);
            int temp = this.bts_p[i];
            this.bts_p[i] = this.bts_p[i + j];
            this.bts_p[i + j] = temp;
        }
    }

    /**
     * @author Sixik
     * @reason Optimize operation
     */
    @Overwrite
    private double sampleAndLerp(int gridX, int gridY, int gridZ, double deltaX, double weirdDeltaY, double deltaZ, double deltaY) {
        final int i = bts$p(gridX);
        final int j = bts$p(gridX + 1);
        final int k = bts$p(i + gridY);
        final int l = bts$p(i + gridY + 1);
        final int m = bts$p(j + gridY);
        final int n = bts$p(j + gridY + 1);

        final int gradIdx0 = bts$p(k + gridZ) & 15;
        final int gradIdx1 = bts$p(m + gridZ) & 15;
        final int gradIdx2 = bts$p(l + gridZ) & 15;
        final int gradIdx3 = bts$p(n + gridZ) & 15;
        final int gradIdx4 = bts$p(k + gridZ + 1) & 15;
        final int gradIdx5 = bts$p(m + gridZ + 1) & 15;
        final int gradIdx6 = bts$p(l + gridZ + 1) & 15;
        final int gradIdx7 = bts$p(n + gridZ + 1) & 15;

        final int[] grad0 = SimplexNoise.GRADIENT[gradIdx0];
        final int[] grad1 = SimplexNoise.GRADIENT[gradIdx1];
        final int[] grad2 = SimplexNoise.GRADIENT[gradIdx2];
        final int[] grad3 = SimplexNoise.GRADIENT[gradIdx3];
        final int[] grad4 = SimplexNoise.GRADIENT[gradIdx4];
        final int[] grad5 = SimplexNoise.GRADIENT[gradIdx5];
        final int[] grad6 = SimplexNoise.GRADIENT[gradIdx6];
        final int[] grad7 = SimplexNoise.GRADIENT[gradIdx7];

        final double d = grad0[0] * deltaX + grad0[1] * weirdDeltaY + grad0[2] * deltaZ;
        final double e = grad1[0] * (deltaX - 1.0) + grad1[1] * weirdDeltaY + grad1[2] * deltaZ;
        final double f = grad2[0] * deltaX + grad2[1] * (weirdDeltaY - 1.0) + grad2[2] * deltaZ;
        final double g = grad3[0] * (deltaX - 1.0) + grad3[1] * (weirdDeltaY - 1.0) + grad3[2] * deltaZ;
        final double h = grad4[0] * deltaX + grad4[1] * weirdDeltaY + grad4[2] * (deltaZ - 1.0);
        final double o = grad5[0] * (deltaX - 1.0) + grad5[1] * weirdDeltaY + grad5[2] * (deltaZ - 1.0);
        final double p = grad6[0] * deltaX + grad6[1] * (weirdDeltaY - 1.0) + grad6[2] * (deltaZ - 1.0);
        final double q = grad7[0] * (deltaX - 1.0) + grad7[1] * (weirdDeltaY - 1.0) + grad7[2] * (deltaZ - 1.0);

        final double r = Mth.smoothstep(deltaX);
        final double s = Mth.smoothstep(deltaY);
        final double t = Mth.smoothstep(deltaZ);
        return Mth.lerp3(r, s, t, d, e, f, g, h, o, p, q);
    }

    /**
     * @author Sixik
     * @reason Optimize operation
     */
    @Overwrite
    private double sampleWithDerivative(int gridX, int gridY, int gridZ, double deltaX, double deltaY, double deltaZ, double[] noiseValues) {
        final int i = bts$p(gridX);
        final int j = bts$p(gridX + 1);
        final int k = bts$p(i + gridY);
        final int l = bts$p(i + gridY + 1);
        final int m = bts$p(j + gridY);
        final int n = bts$p(j + gridY + 1);

        final int o = bts$p(k + gridZ) & 15;
        final int pIdx = bts$p(m + gridZ) & 15;
        final int q = bts$p(l + gridZ) & 15;
        final int r = bts$p(n + gridZ) & 15;
        final int s = bts$p(k + gridZ + 1) & 15;
        final int t = bts$p(m + gridZ + 1) & 15;
        final int u = bts$p(l + gridZ + 1) & 15;
        final int v = bts$p(n + gridZ + 1) & 15;

        final int[] grad0 = SimplexNoise.GRADIENT[o];
        final int[] grad1 = SimplexNoise.GRADIENT[pIdx];
        final int[] grad2 = SimplexNoise.GRADIENT[q];
        final int[] grad3 = SimplexNoise.GRADIENT[r];
        final int[] grad4 = SimplexNoise.GRADIENT[s];
        final int[] grad5 = SimplexNoise.GRADIENT[t];
        final int[] grad6 = SimplexNoise.GRADIENT[u];
        final int[] grad7 = SimplexNoise.GRADIENT[v];

        final double d = grad0[0] * deltaX + grad0[1] * deltaY + grad0[2] * deltaZ;
        final double e = grad1[0] * (deltaX - 1.0) + grad1[1] * deltaY + grad1[2] * deltaZ;
        final double f = grad2[0] * deltaX + grad2[1] * (deltaY - 1.0) + grad2[2] * deltaZ;
        final double g = grad3[0] * (deltaX - 1.0) + grad3[1] * (deltaY - 1.0) + grad3[2] * deltaZ;
        final double h = grad4[0] * deltaX + grad4[1] * deltaY + grad4[2] * (deltaZ - 1.0);
        final double w = grad5[0] * (deltaX - 1.0) + grad5[1] * deltaY + grad5[2] * (deltaZ - 1.0);
        final double x = grad6[0] * deltaX + grad6[1] * (deltaY - 1.0) + grad6[2] * (deltaZ - 1.0);
        final double y = grad7[0] * (deltaX - 1.0) + grad7[1] * (deltaY - 1.0) + grad7[2] * (deltaZ - 1.0);

        final double zSmooth = Mth.smoothstep(deltaX);
        final double aa = Mth.smoothstep(deltaY);
        final double ab = Mth.smoothstep(deltaZ);

        final double ac = Mth.lerp3(zSmooth, aa, ab, grad0[0], grad1[0], grad2[0], grad3[0], grad4[0], grad5[0], grad6[0], grad7[0]);
        final double ad = Mth.lerp3(zSmooth, aa, ab, grad0[1], grad1[1], grad2[1], grad3[1], grad4[1], grad5[1], grad6[1], grad7[1]);
        final double ae = Mth.lerp3(zSmooth, aa, ab, grad0[2], grad1[2], grad2[2], grad3[2], grad4[2], grad5[2], grad6[2], grad7[2]);

        final double af = Mth.lerp2(aa, ab, e - d, g - f, w - h, y - x);
        final double ag = Mth.lerp2(ab, zSmooth, f - d, x - h, g - e, y - w);
        final double ah = Mth.lerp2(zSmooth, aa, h - d, w - e, x - f, y - g);

        final double ai = Mth.smoothstepDerivative(deltaX);
        final double aj = Mth.smoothstepDerivative(deltaY);
        final double ak = Mth.smoothstepDerivative(deltaZ);

        noiseValues[0] += ac + ai * af;
        noiseValues[1] += ad + aj * ag;
        noiseValues[2] += ae + ak * ah;

        return Mth.lerp3(zSmooth, aa, ab, d, e, f, g, h, w, x, y);
    }

    @Unique
    private int bts$p(int index) {
        return this.bts_p[index & 255];
    }

}
