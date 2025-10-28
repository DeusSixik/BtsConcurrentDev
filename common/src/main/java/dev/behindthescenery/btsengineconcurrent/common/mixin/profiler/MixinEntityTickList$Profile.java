package dev.behindthescenery.btsengineconcurrent.common.mixin.profiler;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.behindthescenery.btsengineconcurrent.common.profiler.BtsProfilerSettings;
import dev.behindthescenery.btsengineconcurrent.common.profiler.BtsProfilerUtils;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTickList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(EntityTickList.class)
public class MixinEntityTickList$Profile {

    @WrapOperation(method = "forEach", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    public <T> void bts$forEach(Consumer<T> instance, T t, Operation<Void> original) {
        final String name = ( (Entity) t).getType().toString();
        BtsProfilerUtils.startZone(name, BtsProfilerSettings.Type.EntityTick);
        original.call(instance, t);
        BtsProfilerUtils.endZone(name);
    }

}
