package dev.behindthescenery.btsengineconcurrent.common.entity.tick;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTickList;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Deprecated
public class ConcurrentEntityTickList extends AbstractEntityTickList {

    private final int shardCount;
    private final EntityTickList[] shards;
    private final ThreadLocal<EntityTickList> localShart;

    public ConcurrentEntityTickList(int shardCount) {
        this.shardCount = shardCount;
        this.shards = new EntityTickList[shardCount];
        for (int i = 0; i < shardCount; i++)
            shards[i] = new EntityTickList();

        AtomicInteger next = new AtomicInteger();
        this.localShart = ThreadLocal.withInitial(() -> {
           int id = next.getAndIncrement() % shardCount;
           return shards[id];
        });
    }

    @Override
    public void add(Entity entity) {
        localShart.get().add(entity);
    }

    @Override
    public void remove(Entity entity) {
        localShart.get().remove(entity);
    }

    @Override
    public boolean contains(Entity entity) {
        for (EntityTickList list : shards)
            if (list.contains(entity))
                return true;

        return false;
    }

    @Override
    public void forEach(Consumer<Entity> action) {
        Arrays.stream(shards).parallel().forEach(s -> s.forEach(action));
    }

    public EntityTickList[] getShards() {
        return shards;
    }

    public int getShardCount() {
        return shardCount;
    }
}
