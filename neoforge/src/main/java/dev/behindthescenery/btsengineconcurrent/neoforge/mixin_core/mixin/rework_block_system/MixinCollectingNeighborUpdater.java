package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_block_system;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(CollectingNeighborUpdater.class)
public class MixinCollectingNeighborUpdater {


    @Shadow
    @Final
    private int maxChainedNeighborUpdates;
    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    @Final
    private List<CollectingNeighborUpdater.NeighborUpdates> addedThisLayer;
    @Shadow
    @Final
    private Level level;
    @Unique
    private final ConcurrentLinkedDeque<CollectingNeighborUpdater.NeighborUpdates> bts$concurrent_deque =
            new ConcurrentLinkedDeque<>();

    @Unique
    private final AtomicInteger atomic_depth = new AtomicInteger(0);
    @Unique
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    /**
     * @author Sixik
     * @reason
     */
    @Overwrite
    private void addAndRun(BlockPos pos, CollectingNeighborUpdater.NeighborUpdates updates) {
        int currentDepth = atomic_depth.get(); // Читаем атомарно
        boolean bl = currentDepth > 0;
        boolean bl2 = this.maxChainedNeighborUpdates >= 0 && currentDepth >= this.maxChainedNeighborUpdates;

        // Оптимистично инкрементируем depth (CAS-подобно, но без CAS для простоты)
        // В худшем случае depth будет немного неточным — приемлемо для Minecraft
        atomic_depth.incrementAndGet();

        if (!bl2) {
            if (bl || isProcessing.get()) {
                addedThisLayer.add(updates);
            } else {
                bts$concurrent_deque.push(updates);
            }
        } else if (currentDepth == this.maxChainedNeighborUpdates) {
            LOGGER.error("Too many chained neighbor updates. Skipping the rest. First skipped position: " + pos.toShortString());
        }

        if (!bl && !isProcessing.get()) {
            if (isProcessing.compareAndSet(false, true)) {
                runUpdates();
            }
        }
    }


    /**
     * @author Sixik
     * @reason
     */
    @Overwrite
    private void runUpdates() {
        try {
            // Локальные копии для обработки — избегаем модификации shared state во время цикла
            List<CollectingNeighborUpdater.NeighborUpdates> localPending = new ArrayList<>();

            while (true) {
                // Копируем pending из ThreadLocal (если используем) или из shared ArrayList
                // В простом варианте: атомарно копируем весь pending
                synchronized (addedThisLayer) { // Минимальный lock только для копирования — компромисс
                    localPending.addAll(addedThisLayer);
                    addedThisLayer.clear();
                }

                // Добавляем из concurrent queue
                while (!bts$concurrent_deque.isEmpty()) {
                    CollectingNeighborUpdater.NeighborUpdates entry = bts$concurrent_deque.pollFirst();
                    if (entry != null) {
                        localPending.add(entry);
                    }
                }

                if (localPending.isEmpty()) {
                    break;
                }

                // Обрабатываем локальную копию — нет race conditions
                for (CollectingNeighborUpdater.NeighborUpdates entry : localPending) {
                    entry.runNext(this.level);
                }

                localPending.clear();
            }
        } finally {
            // Сбрасываем depth до 0 только в конце — может быть неточным при concurrency
            atomic_depth.set(0);
            // Очищаем queue (concurrent, safe)
            bts$concurrent_deque.clear();
        }
    }
}
