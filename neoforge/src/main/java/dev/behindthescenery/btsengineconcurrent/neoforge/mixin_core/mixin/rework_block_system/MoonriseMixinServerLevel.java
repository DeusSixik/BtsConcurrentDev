package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_block_system;

import ca.spottedleaf.moonrise.common.list.ReferenceList;
import ca.spottedleaf.moonrise.patches.chunk_system.level.ChunkSystemServerLevel;
import ca.spottedleaf.moonrise.patches.chunk_system.level.chunk.ChunkSystemChunkHolder;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(value = ServerLevel.class, priority = -1000)
public abstract class MoonriseMixinServerLevel implements ChunkSystemServerLevel {


    @Inject(method = "moonrise$clearUnsyncedChunks",
            at = @At("HEAD"), cancellable = true)
    public void bts$clearUnsyncedChunks(CallbackInfo ci) {
        ci.cancel();
        final ReferenceList<ChunkHolder> un = moonrise$getUnsyncedChunks();

        ChunkHolder[] chunkHolders = un.getRawDataUnchecked();
        int totalUnsyncedChunks = un.size();
        Objects.checkFromToIndex(0, totalUnsyncedChunks, chunkHolders.length);

        for(int i = 0; i < totalUnsyncedChunks; ++i) {
            ChunkHolder chunkHolder = chunkHolders[i];

            if(chunkHolder == null) continue;

            ((ChunkSystemChunkHolder)chunkHolder).moonrise$markDirtyForPlayers(false);
        }

        this.moonrise$getUnsyncedChunks().clear();
    }
}
