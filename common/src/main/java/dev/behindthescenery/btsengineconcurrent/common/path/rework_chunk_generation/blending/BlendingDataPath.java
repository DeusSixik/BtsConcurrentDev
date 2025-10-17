package dev.behindthescenery.btsengineconcurrent.common.path.rework_chunk_generation.blending;

import net.minecraft.world.level.chunk.ChunkAccess;

public interface BlendingDataPath {

    void bts$calculateData(ChunkAccess chunk, int bits);
}
