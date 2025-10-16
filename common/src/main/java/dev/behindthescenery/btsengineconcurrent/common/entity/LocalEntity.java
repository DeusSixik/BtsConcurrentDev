package dev.behindthescenery.btsengineconcurrent.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class LocalEntity {

    protected Entity originalEntity;
    protected Entity copyOfOriginal;

    public LocalEntity(Entity original) {
        this.originalEntity = original;

        if(original instanceof Player) {
            copyOfOriginal = original;
            return;
        }

        this.copyOfOriginal = createCopy(originalEntity);
    }

    public Entity getOriginal() {
        return originalEntity;
    }

    public Entity getLocal() {
        return copyOfOriginal;
    }

    public boolean synchronizeDataFromOriginal() {
        if(!originalEntity.isAlive())
            return false;

        copyOfOriginal.load(getEntityNbt(originalEntity));
        return true;
    }

    public boolean synchronizeDataFromLocal() {
        if(!originalEntity.isAlive())
            return false;

        originalEntity.load(getEntityNbt(copyOfOriginal));
        return true;
    }

    private static Entity createCopy(Entity original) {
        final Level level = original.level();
        final Entity entity = original.getType().create(level);
        if(entity == null)
            throw new RuntimeException("Can't create copy of entity. " + original);

        entity.load(getEntityNbt(original));
        return entity;
    }

    private static CompoundTag getEntityNbt(Entity original) {
        CompoundTag nbt = new CompoundTag();
        if(!original.save(nbt))
            throw new RuntimeException("Error when try copy nbt from Entity. " + original);
        return nbt;
    }
}
