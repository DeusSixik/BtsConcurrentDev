package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.path;


import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelCallback;

public interface EntityLookupPath {

    Entity[] bts$getAllEntities();

    LevelCallback<Entity> bts$getCallBack();
}
