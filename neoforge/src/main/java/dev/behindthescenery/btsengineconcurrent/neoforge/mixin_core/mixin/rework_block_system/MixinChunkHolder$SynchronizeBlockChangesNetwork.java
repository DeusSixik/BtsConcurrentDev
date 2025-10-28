package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_block_system;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.behindthescenery.btsengineconcurrent.BtsEngineConcurrent;
import dev.behindthescenery.btsengineconcurrent.common.profiler.BtsProfilerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Mixin(ChunkHolder.class)
public class MixinChunkHolder$SynchronizeBlockChangesNetwork {

    @Unique
    private final Lock bts$synchronize_lock = new ReentrantLock();

    @WrapMethod(method = "blockChanged")
    public void wrapLock$blockChanged(BlockPos pos, Operation<Void> original) {
        bts$synchronize_lock.lock();
        try {
            final String name = "Invoke BlockChanged";
            BtsProfilerUtils.startZone(name);
            original.call(pos);
            BtsProfilerUtils.endZone(name);
        } catch (Exception e) {
            BtsEngineConcurrent.LOGGER.error(e.getMessage(), e);
        } finally {
            bts$synchronize_lock.unlock();
        }
    }

    @WrapMethod(method = "broadcastChanges")
    public void wrapLock$broadcastChanges(LevelChunk chunk, Operation<Void> original) {
        bts$synchronize_lock.lock();
        try {
            final String name = "Broadcast Chunk Changes";
            BtsProfilerUtils.startZone(name);
            original.call(chunk);
            BtsProfilerUtils.endZone(name);
        } catch (Exception e) {
            BtsEngineConcurrent.LOGGER.error(e.getMessage(), e);
        } finally {
            bts$synchronize_lock.unlock();
        }
    }
}
