package dev.behindthescenery.btsengineconcurrent.common.utils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ShardList<T> {

    protected final int shardCount;
    protected final T[] shards;
    protected final ThreadLocal<T> localShart;

    public ShardList(int shardCount, Supplier<T> constructor) {
        this.shardCount = shardCount;
        this.shards = (T[])(new Object[shardCount]);
        for (int i = 0; i < shardCount; i++)
            shards[i] = constructor.get();

        AtomicInteger next = new AtomicInteger();
        this.localShart = ThreadLocal.withInitial(() -> {
            int id = next.getAndIncrement() % shardCount;
            return shards[id];
        });
    }

    public abstract void add(T element);

    public abstract void remove(T element);

    public abstract boolean contains(T element);

    public abstract void forEach(Consumer<T> action);

    public T[] getShards() {
        return shards;
    }

    public int getShardCount() {
        return shardCount;
    }
}
