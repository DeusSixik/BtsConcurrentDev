package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.async;

import com.google.common.base.Stopwatch;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(LocateCommand.class)
public abstract class MixinLocateCommand$Async {

    @Shadow
    private static Optional<? extends HolderSet.ListBacked<Structure>> getHolders(ResourceOrTagKeyArgument.Result<Structure> structure, Registry<Structure> structureRegistry) {
        throw new NotImplementedException();
    }

    @Shadow
    @Final
    private static DynamicCommandExceptionType ERROR_STRUCTURE_INVALID;

    @Shadow
    @Final
    private static DynamicCommandExceptionType ERROR_STRUCTURE_NOT_FOUND;

    @Shadow
    @Final
    private static DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND;

    /**
     * @author Sixik
     * @reason Async locate structure
     */
    @Overwrite
    private static int locateStructure(CommandSourceStack source, ResourceOrTagKeyArgument.Result<Structure> structure) {
        CompletableFuture.runAsync(() -> {
            final Registry<Structure> registry = source.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);
            final HolderSet<Structure> holderSet;
            try {
                holderSet = getHolders(structure, registry)
                        .orElseThrow(() -> ERROR_STRUCTURE_INVALID.create(structure.asPrintable()));
            } catch (CommandSyntaxException e) {
                source.sendFailure(Component.literal(e.getMessage()));
                return;
            }

            final BlockPos blockPos = BlockPos.containing(source.getPosition());
            final ServerLevel serverLevel = source.getLevel();
            final Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
            final Pair<BlockPos, Holder<Structure>> pair = serverLevel.getChunkSource()
                    .getGenerator()
                    .findNearestMapStructure(serverLevel, holderSet, blockPos, 100, false);

            stopwatch.stop();

            if (pair == null) {
                source.sendFailure(Component.literal(ERROR_STRUCTURE_NOT_FOUND.create(structure.asPrintable()).getMessage()));
                return;
            }

            LocateCommand.showLocateResult(source, structure, blockPos, pair,
                    "commands.locate.structure.success", false, stopwatch.elapsed());
        });
        return 0;
    }

    /**
     * @author Sixik
     * @reason Async locate biome
     */
    @Overwrite
    private static int locateBiome(CommandSourceStack source, ResourceOrTagArgument.Result<Biome> biome) {
        CompletableFuture.runAsync(() -> {
            final BlockPos blockPos = BlockPos.containing(source.getPosition());
            final Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
            final Pair<BlockPos, Holder<Biome>> pair = source.getLevel().findClosestBiome3d(biome, blockPos, 6400, 32, 64);
            stopwatch.stop();
            if (pair == null) {
                source.sendFailure(Component.literal(ERROR_BIOME_NOT_FOUND.create(biome.asPrintable()).getMessage()));
            } else {
                LocateCommand.showLocateResult(source, biome, blockPos, pair, "commands.locate.biome.success", true, stopwatch.elapsed());
            }
        });
        return 0;
    }
}
