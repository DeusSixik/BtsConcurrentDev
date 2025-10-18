package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_block_system;

import net.minecraft.server.level.ServerLevel;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ServerLevel.class, priority = 1500)
public abstract class MixinServerLevel$RemoveRecursiveCall {

    @Unique
    private final ThreadLocal<Boolean> bts$thread_recursive_listener =
            ThreadLocal.withInitial(() -> false);

    @Redirect(method = "sendBlockUpdated", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/server/level/ServerLevel;isUpdatingNavigations:Z"))
    public boolean bts$sendBlockUpdated$modifyListener$get(ServerLevel instance) {
        return bts$thread_recursive_listener.get();
    }

    @Redirect(method = "sendBlockUpdated", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/server/level/ServerLevel;isUpdatingNavigations:Z"))
    public void bts$sendBlockUpdated$modifyListener$set(ServerLevel instance, boolean value) {
        bts$thread_recursive_listener.set(value);
    }
}
