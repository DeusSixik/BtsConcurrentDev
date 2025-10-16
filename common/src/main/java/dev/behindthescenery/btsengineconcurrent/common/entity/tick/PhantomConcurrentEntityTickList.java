package dev.behindthescenery.btsengineconcurrent.common.entity.tick;

import dev.behindthescenery.btsengineconcurrent.common.entity.LocalEntity;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class PhantomConcurrentEntityTickList extends AbstractEntityTickList {

    private final int shardCount;
    private final EntityList[] shards;
    private final ThreadLocal<EntityList> localShart;

    public PhantomConcurrentEntityTickList(int shardCount) {
        this.shardCount = shardCount;
        this.shards = new EntityList[shardCount];
        for (int i = 0; i < shardCount; i++)
            shards[i] = new EntityList();

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
        for (EntityList list : shards)
            if (list.contains(entity))
                return true;

        return false;
    }

    @Override
    public void forEach(Consumer<Entity> action) {
        Arrays.stream(shards).forEach(s -> s.forEach(action));
    }

    public EntityList[] getShards() {
        return shards;
    }

    public int getShardCount() {
        return shardCount;
    }

    public static class EntityList {
        public ConcurrentMap<Integer, LocalEntity> entities =
                new ConcurrentHashMap<>();

        public void add(Entity entity) {
            entities.put(entity.getId(), new LocalEntity(entity));
        }

        public void remove(Entity entity) {
            entities.remove(entity.getId());
        }

        public boolean contains(Entity entity) {
            return entities.containsKey(entity.getId());
        }

        public void forEach(Consumer<Entity> entity) {
            for (LocalEntity entity2 : entities.values()) {
                entity.accept(entity2.getOriginal());
            }
        }
    }
}
