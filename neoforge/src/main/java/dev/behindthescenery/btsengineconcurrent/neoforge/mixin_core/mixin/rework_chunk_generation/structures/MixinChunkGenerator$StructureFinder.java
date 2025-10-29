package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.structures;

import ca.spottedleaf.moonrise.patches.chunk_system.level.ChunkSystemLevelReader;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mixin(ChunkGenerator.class)
public abstract class MixinChunkGenerator$StructureFinder {

    @Shadow
    private static boolean tryAddReference(StructureManager structureManager, StructureStart structureStart) {
        throw new RuntimeException();
    }

    @Unique
    private static final ThreadLocal<Map<StructurePlacement, Set<Holder<Structure>>>> BTS_TL_PLACEMENTS =
            ThreadLocal.withInitial(Object2ObjectArrayMap::new);
    @Unique
    private static final ThreadLocal<List<Map.Entry<StructurePlacement, Set<Holder<Structure>>>>> BTS_TL_RANDOM =
            ThreadLocal.withInitial(ArrayList::new);

    @Unique
    private static final ExecutorService BTS_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * @author Sixik
     * @reason Use async operation
     */
    @Nullable
    @Overwrite
    public Pair<BlockPos, Holder<Structure>> findNearestMapStructure(
            ServerLevel level,
            HolderSet<Structure> structure,
            BlockPos pos,
            int searchRadius,
            boolean skipKnownStructures
    ) {
        final ChunkGeneratorStructureState state = level.getChunkSource().getGeneratorState();
        final StructureManager manager = level.structureManager();

        final Map<StructurePlacement, Set<Holder<Structure>>> map = BTS_TL_PLACEMENTS.get();
        map.clear();

        for (Holder<Structure> holder : structure) {
            for (StructurePlacement placement : state.getPlacementsForStructure(holder)) {
                map.computeIfAbsent(placement, k -> new ObjectArraySet<>()).add(holder);
            }
        }

        if (map.isEmpty()) return null;

        final List<Map.Entry<StructurePlacement, Set<Holder<Structure>>>> randoms = BTS_TL_RANDOM.get();
        randoms.clear();
        final List<Map.Entry<StructurePlacement, Set<Holder<Structure>>>> rings = new ArrayList<>();

        for (final var entry : map.entrySet()) {
            if (entry.getKey() instanceof ConcentricRingsStructurePlacement)
                rings.add(entry);
            else if (entry.getKey() instanceof RandomSpreadStructurePlacement)
                randoms.add(entry);
        }

        final CompletableFuture<Pair<BlockPos, Holder<Structure>>> fRings = CompletableFuture.supplyAsync(
                () -> bts$findRings(level, manager, pos, skipKnownStructures, rings), BTS_EXECUTOR);
        final CompletableFuture<Pair<BlockPos, Holder<Structure>>> fRandoms = CompletableFuture.supplyAsync(
                () -> bts$findRandom(level, manager, pos, searchRadius, skipKnownStructures, state.getLevelSeed(), randoms), BTS_EXECUTOR);

        return CompletableFuture.allOf(fRings, fRandoms).thenApply(v -> {
            Pair<BlockPos, Holder<Structure>> a = fRings.join();
            Pair<BlockPos, Holder<Structure>> b = fRandoms.join();
            if (a == null) return b;
            if (b == null) return a;
            double da = pos.distSqr(a.getFirst());
            double db = pos.distSqr(b.getFirst());
            return da < db ? a : b;
        }).join();
    }

    @Unique
    @Nullable
    private Pair<BlockPos, Holder<Structure>> bts$findRings(
            final ServerLevel level,
            final StructureManager manager,
            final BlockPos pos,
            final boolean skipKnownStructures,
            final List<Map.Entry<StructurePlacement, Set<Holder<Structure>>>> entries
    ) {
        Pair<BlockPos, Holder<Structure>> best = null;
        double bestDist = Double.MAX_VALUE;

        for (final var e : entries) {
            final ConcentricRingsStructurePlacement placement = (ConcentricRingsStructurePlacement) e.getKey();
            final Pair<BlockPos, Holder<Structure>> pair =
                    getNearestGeneratedStructure(e.getValue(), level, manager, pos, skipKnownStructures, placement);
            if (pair != null) {
                final double dist = pos.distSqr(pair.getFirst());
                if (dist < bestDist) {
                    best = pair;
                    bestDist = dist;
                }
            }
        }
        return best;
    }

    @Unique
    @Nullable
    private Pair<BlockPos, Holder<Structure>> bts$findRandom(
            final ServerLevel level,
            final StructureManager manager,
            final BlockPos pos,
            final int radius,
            final boolean skipKnownStructures,
            final long seed,
            final List<Map.Entry<StructurePlacement, Set<Holder<Structure>>>> entries
    ) {
        if (entries.isEmpty()) return null;

        int i = SectionPos.blockToSectionCoord(pos.getX());
        int j = SectionPos.blockToSectionCoord(pos.getZ());
        Map<ChunkPos, StructureCheckResult> localCache = new Object2ObjectOpenHashMap<>();

        Pair<BlockPos, Holder<Structure>> best = null;
        double bestDist = Double.MAX_VALUE;

        for (int k = 0; k <= radius; ++k) {
            boolean found = false;

            for (var e : entries) {
                RandomSpreadStructurePlacement placement = (RandomSpreadStructurePlacement) e.getKey();
                Pair<BlockPos, Holder<Structure>> pair =
                        bts$getNearestGeneratedStructure(e.getValue(), level, manager, i, j, k, skipKnownStructures, seed, placement, localCache);
                if (pair != null) {
                    found = true;
                    double dist = pos.distSqr(pair.getFirst());
                    if (dist < bestDist) {
                        best = pair;
                        bestDist = dist;
                    }
                }
            }

            if (found) break;
        }

        return best;
    }

    /**
     * @author Sixik
     * @reason Optimize locate
     */
    @Nullable
    @Overwrite
    private Pair<BlockPos, Holder<Structure>> getNearestGeneratedStructure(
            final Set<Holder<Structure>> structureHoldersSet,
            final ServerLevel level,
            final StructureManager structureManager,
            final BlockPos pos,
            final boolean skipKnownStructures,
            final ConcentricRingsStructurePlacement placement
    ) {
        final List<ChunkPos> list = level.getChunkSource().getGeneratorState().getRingPositionsFor(placement);
        if (list == null) throw new IllegalStateException("No ring positions for placement: " + placement);

        Pair<BlockPos, Holder<Structure>> best = null;
        double bestDist = Double.MAX_VALUE;
        final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (ChunkPos chunkPos : list) {
            mutable.set(SectionPos.sectionToBlockCoord(chunkPos.x, 8), 32, SectionPos.sectionToBlockCoord(chunkPos.z, 8));
            final double dist = mutable.distSqr(pos);
            if (dist >= bestDist) continue;

            final Pair<BlockPos, Holder<Structure>> pair =
                    getStructureGeneratingAt(structureHoldersSet, level, structureManager, skipKnownStructures, placement, chunkPos);
            if (pair != null) {
                best = pair;
                bestDist = dist;
            }
        }

        return best;
    }

    @Nullable
    @Unique
    private static Pair<BlockPos, Holder<Structure>> bts$getNearestGeneratedStructure(
            final Set<Holder<Structure>> structureHoldersSet,
            final LevelReader level,
            final StructureManager structureManager,
            final int x, int z, int radius,
            final boolean skipKnownStructures,
            final long seed,
            final RandomSpreadStructurePlacement placement,
            final Map<ChunkPos, StructureCheckResult> localCache
    ) {
        final int spacing = placement.spacing();

        for (int dz = -radius; dz <= radius; ++dz) {
            final boolean edgeZ = dz == -radius || dz == radius;

            for (int dx = -radius; dx <= radius; ++dx) {
                final boolean edgeX = dx == -radius || dx == radius;
                if (!edgeX && !edgeZ) continue;

                final int cx = x + spacing * dx;
                final int cz = z + spacing * dz;
                final ChunkPos candidate = placement.getPotentialStructureChunk(seed, cx, cz);

                final Pair<BlockPos, Holder<Structure>> result =
                        bts$getStructureGeneratingAt(structureHoldersSet, level, structureManager, skipKnownStructures, placement, candidate, localCache);
                if (result != null) return result;
            }
        }
        return null;
    }

    /**
     * @author Sixik
     * @reason Optimize to Async
     */
    @Nullable
    @Overwrite
    private static Pair<BlockPos, Holder<Structure>> getStructureGeneratingAt(
            final Set<Holder<Structure>> structureHoldersSet,
            final LevelReader level,
            final StructureManager structureManager,
            final boolean skipKnownStructures,
            final StructurePlacement placement,
            final ChunkPos chunkPos
    ) {
        return bts$getStructureGeneratingAt(structureHoldersSet, level, structureManager, skipKnownStructures, placement, chunkPos, null);
    }

    @Unique
    @Nullable
    private static Pair<BlockPos, Holder<Structure>> bts$getStructureGeneratingAt(
            final Set<Holder<Structure>> structureHoldersSet,
            final LevelReader level,
            final StructureManager structureManager,
            final boolean skipKnownStructures,
            final StructurePlacement placement,
            final ChunkPos chunkPos,
            final @Nullable Map<ChunkPos, StructureCheckResult> localCache
    ) {
        for (final Holder<Structure> holder : structureHoldersSet) {
            final Structure structure = holder.value();
            final StructureCheckResult result;

            if (localCache != null)
                result = localCache.computeIfAbsent(chunkPos,
                    pos -> structureManager.checkStructurePresence(pos, structure, placement, skipKnownStructures));
            else
                result = structureManager.checkStructurePresence(chunkPos, structure, placement, skipKnownStructures);

            if (result != StructureCheckResult.START_NOT_PRESENT) {
                if (!skipKnownStructures && result == StructureCheckResult.START_PRESENT)
                    return Pair.of(placement.getLocatePos(chunkPos), holder);

                final ChunkAccess chunk = ((ChunkSystemLevelReader)level).moonrise$syncLoadNonFull(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
                final StructureStart start = structureManager.getStartForStructure(SectionPos.bottomOf(chunk), structure, chunk);
                if (start != null && start.isValid() && (!skipKnownStructures || tryAddReference(structureManager, start))) {
                    return Pair.of(placement.getLocatePos(start.getChunkPos()), holder);
                }
            }
        }
        return null;
    }
}
