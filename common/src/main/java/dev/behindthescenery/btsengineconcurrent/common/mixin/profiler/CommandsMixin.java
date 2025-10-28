package dev.behindthescenery.btsengineconcurrent.common.mixin.profiler;

import com.mojang.brigadier.CommandDispatcher;
import dev.behindthescenery.btsengineconcurrent.BtsEngineConcurrentCommands;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandsMixin {

    @Shadow
    @Final
    private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void bts$init(Commands.CommandSelection selection, CommandBuildContext context, CallbackInfo ci) {
        this.dispatcher.register(BtsEngineConcurrentCommands.registerCommand());
    }
}
