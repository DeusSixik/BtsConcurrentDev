package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_chunk_generation.structures;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(ChunkAccess.class)
public class MixinChunkAccess$SynchronizeStructureData {


    @Shadow
    @Mutable
    @Final
    private Map<Structure, StructureStart> structureStarts = Maps.newConcurrentMap();

    @Shadow
    @Mutable
    @Final
    private Map<Structure, LongSet> structuresRefences = Maps.newConcurrentMap();
}
