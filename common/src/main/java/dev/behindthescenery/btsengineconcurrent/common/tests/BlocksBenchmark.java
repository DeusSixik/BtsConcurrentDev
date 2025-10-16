package dev.behindthescenery.btsengineconcurrent.common.tests;

import dev.behindthescenery.btsengineconcurrent.BtsEngineConcurrent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class BlocksBenchmark {
    /**
     * Асинхронное построение сетки кубов (cubeCount³ штук)
     */
    public static CompletableFuture<Void> placeCubesV3(ServerLevel level, BlockPos pos, int cubeSize,
                                                       int cubeCount, ExecutorService executorService) {
        final BlockPos startPos = pos.immutable();
        final int totalCubes = cubeCount * cubeCount * cubeCount;
        final int spacing = 1; // расстояние между кубами

        final List<CompletableFuture<Void>> futures = new ArrayList<>(totalCubes);

        for (int i = 0; i < totalCubes; i++) {
            int index = i;

            final int col = index % cubeCount;   // X
            index /= cubeCount;
            final int row = index % cubeCount;   // Y
            final int layer = index / cubeCount; // Z

            final int xOffset = col * (cubeSize + spacing);
            final int yOffset = row * (cubeSize + spacing);
            final int zOffset = layer * (cubeSize + spacing);

            final BlockPos cubePos = startPos.offset(xOffset, yOffset, zOffset);

            // каждая задача строит отдельный куб
            futures.add(placeSingleCubeFuture(level, cubePos, cubeSize, executorService));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * Параллельное построение кубов без ожидания
     */
    public static void placeCubesV2(ServerLevel level, BlockPos pos, int cubeSize,
                                    int cubeCount, ExecutorService executorService) {
        final BlockPos startPos = pos.immutable();
        final int totalCubes = cubeCount * cubeCount * cubeCount;
        final int spacing = 1;

        for (int i = 0; i < totalCubes; i++) {
            int index = i;

            final int col = index % cubeCount;
            index /= cubeCount;
            final int row = index % cubeCount;
            final int layer = index / cubeCount;

            final int xOffset = col * (cubeSize + spacing);
            final int yOffset = row * (cubeSize + spacing);
            final int zOffset = layer * (cubeSize + spacing);

            final BlockPos cubePos = startPos.offset(xOffset, yOffset, zOffset);

            executorService.submit(() -> placeSingleCube(level, cubePos, cubeSize));
        }
    }

    /**
     * Асинхронный вариант placeSingleCube
     */
    public static CompletableFuture<Void> placeSingleCubeFuture(ServerLevel level, BlockPos origin,
                                                                int cubeSize, ExecutorService executorService) {
        return CompletableFuture.runAsync(() ->
                placeSingleCube(level, origin, cubeSize), executorService);
    }

    /**
     * Ставит куб cubeSize³ начиная от origin.
     * Важно: ставим блоки через server.execute(), чтобы не ломать потокобезопасность Minecraft.
     */
    public static void placeSingleCube(ServerLevel level, BlockPos origin, int cubeSize) {
        try {
            final BlockState block = Blocks.BEDROCK.defaultBlockState();
            final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

            final int startX = origin.getX();
            final int startY = origin.getY();
            final int startZ = origin.getZ();

            for (int x = 0; x < cubeSize; x++) {
                for (int y = 0; y < cubeSize; y++) {
                    for (int z = 0; z < cubeSize; z++) {
                        final int bx = startX + x;
                        final int by = startY + y;
                        final int bz = startZ + z;

                        level.setBlock(mutable.set(bx, by, bz), block, 0);
                    }
                }
            }
        } catch (Exception e) {
            BtsEngineConcurrent.LOGGER.error(e.getMessage(), e);
        }
    }
}
