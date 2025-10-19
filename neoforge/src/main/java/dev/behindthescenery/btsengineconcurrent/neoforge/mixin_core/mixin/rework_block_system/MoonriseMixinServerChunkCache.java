package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_block_system;

import ca.spottedleaf.moonrise.common.list.ReferenceList;
import ca.spottedleaf.moonrise.patches.chunk_system.level.ChunkSystemServerLevel;
import com.bawnorton.mixinsquared.TargetHandler;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Mixin(value = ServerChunkCache.class, priority = 1500)
public class MoonriseMixinServerChunkCache {


    @Shadow
    @Final
    private ServerLevel level;


    /**
     * @author Sixik
     * @reason Fix null error
     */
    @TargetHandler(
            mixin = "ca.spottedleaf.moonrise.mixin.chunk_system.ServerChunkCacheMixin",
            name = "fixBroadcastChanges"
    )
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true)
    private void fixBroadcastChanges(List<ServerChunkCache.ChunkAndHolder> instance, Consumer<ServerChunkCache.ChunkAndHolder> consumer, CallbackInfo ci) {
        ci.cancel();
        final ReferenceList<ChunkHolder> unsyncedChunks = ((ChunkSystemServerLevel)this.level).moonrise$getUnsyncedChunks();
        final ChunkHolder[] chunkHolders = unsyncedChunks.getRawDataUnchecked();
        final int totalUnsyncedChunks = unsyncedChunks.size();
        Objects.checkFromToIndex(0, totalUnsyncedChunks, chunkHolders.length);

        for(int i = 0; i < totalUnsyncedChunks; ++i) {
            final ChunkHolder chunkHolder = chunkHolders[i];
            if(chunkHolder == null) continue;

            //Check null
            final LevelChunk chunk = chunkHolder.getChunkToSend();
            if(chunk == null) continue;

            chunkHolder.broadcastChanges(chunk);
        }

        ((ChunkSystemServerLevel)this.level).moonrise$clearUnsyncedChunks();
    }
}
