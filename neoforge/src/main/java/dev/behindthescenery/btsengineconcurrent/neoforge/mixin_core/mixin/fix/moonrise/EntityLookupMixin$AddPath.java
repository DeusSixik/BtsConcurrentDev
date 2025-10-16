package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.fix.moonrise;


import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.EntityLookup;
import dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.path.EntityLookupPath;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelCallback;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityLookup.class)
public abstract class EntityLookupMixin$AddPath implements EntityLookupPath {

    @Shadow
    public abstract Entity[] getAllCopy();

    @Shadow
    @Final
    protected LevelCallback<Entity> worldCallback;

    @Override
    public Entity[] bts$getAllEntities() {
        return getAllCopy();
    }

    @Override
    public LevelCallback<Entity> bts$getCallBack() {
        return worldCallback;
    }
}
