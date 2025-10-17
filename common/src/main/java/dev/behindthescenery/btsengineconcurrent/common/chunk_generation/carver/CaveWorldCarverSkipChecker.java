package dev.behindthescenery.btsengineconcurrent.common.chunk_generation.carver;

import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

public class CaveWorldCarverSkipChecker implements WorldCarver.CarveSkipChecker {

    protected double minrelativeY;

    public CaveWorldCarverSkipChecker() {}

    public void updateY(double minrelativeY) {
        this.minrelativeY = minrelativeY;
    }

    @Override
    public boolean shouldSkip(CarvingContext carvingContext, double d, double e, double f, int i) {
        return shouldSkip(d, e, f, minrelativeY);
    }

    private static boolean shouldSkip(double relative, double relativeY, double relativeZ, double minrelativeY) {
        if (relativeY <= minrelativeY) {
            return true;
        } else {
            return relative * relative + relativeY * relativeY + relativeZ * relativeZ >= (double)1.0F;
        }
    }
}
