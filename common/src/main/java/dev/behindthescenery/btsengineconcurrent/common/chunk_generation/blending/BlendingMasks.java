package dev.behindthescenery.btsengineconcurrent.common.chunk_generation.blending;

import net.minecraft.core.Direction8;

public class BlendingMasks {

    public static final int NORTH = 1;
    public static final int EAST = 1 << 1;
    public static final int SOUTH = 1 << 2;
    public static final int WEST = 1 << 3;

    public static final int NORTH_EAST = NORTH | EAST;
    public static final int NORTH_WEST = NORTH | WEST;
    public static final int SOUTH_EAST = SOUTH | EAST;
    public static final int SOUTH_WEST = SOUTH | WEST;

    public static int getBits(Direction8 direction8) {
        return switch (direction8) {
            case EAST -> EAST;
            case WEST -> WEST;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case NORTH_EAST -> NORTH_EAST;
            case NORTH_WEST -> NORTH_WEST;
            case SOUTH_EAST -> SOUTH_EAST;
            case SOUTH_WEST -> SOUTH_WEST;
        };
    }
}
