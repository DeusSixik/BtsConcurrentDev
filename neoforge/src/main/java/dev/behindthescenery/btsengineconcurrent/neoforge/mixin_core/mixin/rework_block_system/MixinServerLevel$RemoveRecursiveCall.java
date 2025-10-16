package dev.behindthescenery.btsengineconcurrent.neoforge.mixin_core.mixin.rework_block_system;

import dev.behindthescenery.btsengineconcurrent.ReflectionsUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathTypeCache;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.*;

import java.util.List;
import java.util.Set;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel$RemoveRecursiveCall {

    @Unique
    private final ThreadLocal<Boolean> bts$thread_recursive_listener =
            ThreadLocal.withInitial(() -> false);

    @Shadow
    @Final
    Set<Mob> navigatingMobs;

    @Shadow
    public abstract ServerChunkCache getChunkSource();

    @Shadow
    @Final
    private PathTypeCache pathTypesByPosCache;

    /**
     * @author Sixik
     * @reason
     */
    @Overwrite
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        if (bts$thread_recursive_listener.get()) {
            Util.logAndPauseIfInIde("recursive call to sendBlockUpdated", new IllegalStateException("recursive call to sendBlockUpdated"));
        }

        final ServerLevel level = ReflectionsUtils.cast(this);

        this.getChunkSource().blockChanged(pos);
        this.pathTypesByPosCache.invalidate(pos);
        VoxelShape voxelshape1 = oldState.getCollisionShape(level, pos);
        VoxelShape voxelshape = newState.getCollisionShape(level, pos);
        if (Shapes.joinIsNotEmpty(voxelshape1, voxelshape, BooleanOp.NOT_SAME)) {
            final List<PathNavigation> list = new ObjectArrayList<>();

            for(Mob mob : this.navigatingMobs) {
                PathNavigation pathnavigation = mob.getNavigation();
                if (pathnavigation.shouldRecomputePath(pos)) {
                    list.add(pathnavigation);
                }
            }

            try {
                bts$thread_recursive_listener.set(true);

                for(PathNavigation pathnavigation1 : list) {
                    pathnavigation1.recomputePath();
                }
            } finally {
                bts$thread_recursive_listener.set(false);
            }
        }
    }
}
