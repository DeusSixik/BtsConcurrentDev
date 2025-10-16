package dev.behindthescenery.btsengineconcurrent.common.tests;

import dev.behindthescenery.btsengineconcurrent.BtsEngineConcurrent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class ConcurrentBlockPlacer {

    public static void placeBlocks(ServerLevel level, Vec3i startPosition, int cubsCount, int cubSize, ExecutorService executorService) {
        final Block placerBlock = Blocks.BEDROCK;
        final List<CompletableFuture<?>> futures = new ArrayList<>();
        final int spacing = cubSize + 1;

        final int gridSize = (int) Math.ceil(Math.cbrt(cubsCount));

        for (int i = 0; i < cubsCount; i++) {
            final int index = i;

            final int xIndex = index % gridSize;
            final int yIndex = (index / gridSize) % gridSize;
            final int zIndex = index / (gridSize * gridSize);

            final Vec3i cubeCenter = new Vec3i(
                    startPosition.getX() + xIndex * spacing,
                    startPosition.getY() + yIndex * spacing,
                    startPosition.getZ() + zIndex * spacing
            );

            final CompletableFuture<?> task = CompletableFuture.runAsync(() -> {
                try {
                    placeCube(level, cubeCenter, cubSize, placerBlock);
                } catch (Exception e) {
                    BtsEngineConcurrent.LOGGER.error("Cube place error #{}: {}", index, e.getMessage(), e);
                }
            }, executorService);

            futures.add(task);
        }

        CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .whenComplete((c1, c2) -> {
                    if (c2 != null) {
                        return;
                    }
                    BtsEngineConcurrent.LOGGER.info("All cube placed ({})", cubsCount);
                });
    }

    public static void placeCube(ServerLevel level, Vec3i center, int size, Block placerBlock) {
        try {
            final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
            final BlockState blockState = placerBlock.defaultBlockState();

            final int sizePerDirection = size / 2;
            final int startX = center.getX() - sizePerDirection;
            final int startY = center.getY() - sizePerDirection;
            final int startZ = center.getZ() - sizePerDirection;

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    for (int z = 0; z < size; z++) {
                        level.setBlock(blockPos.set(startX + x, startY + y, startZ + z), blockState, Block.UPDATE_ALL);
                    }
                }
            }

            System.out.println("Cube placed at " + center + " size " + size);
        } catch (Exception e) {
            BtsEngineConcurrent.LOGGER.error(e.getMessage(), e);
        }
    }
}
