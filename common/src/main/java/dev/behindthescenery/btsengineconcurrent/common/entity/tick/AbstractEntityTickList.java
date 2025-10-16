package dev.behindthescenery.btsengineconcurrent.common.entity.tick;

import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;

public abstract class AbstractEntityTickList {

    public abstract void add(Entity entity);

    public abstract void remove(Entity entity);

    public abstract boolean contains(Entity entity);

    public abstract void forEach(Consumer<Entity> entity);
}
