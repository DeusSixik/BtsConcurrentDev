package dev.behindthescenery.btsengineconcurrent.common.utils;

import net.minecraft.world.level.levelgen.DensityFunction;

public class BtsSinglePointContext implements DensityFunction.FunctionContext {

    protected int blockX;
    protected int blockY;
    protected int blockZ;

    public BtsSinglePointContext(int blockX, int blockY, int blockZ) {
        update(blockX, blockY, blockZ);
    }

    public void update(int blockX, int blockY, int blockZ) {
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
    }

    @Override
    public int blockX() {
        return blockX;
    }

    @Override
    public int blockY() {
        return blockY;
    }

    @Override
    public int blockZ() {
        return blockY;
    }
}
