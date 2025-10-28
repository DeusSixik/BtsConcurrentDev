package dev.behindthescenery.btsengineconcurrent.neoforge;

import dev.behindthescenery.btsengineconcurrent.BtsEngineConcurrent;
import dev.behindthescenery.btsengineconcurrent.ModLoaderHook;
import dev.behindthescenery.btsengineconcurrent.common.tests.BlocksBenchmark;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mod(BtsEngineConcurrent.MOD_ID)
public final class BtsEngineConcurrentNeoForge {

    private static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public BtsEngineConcurrentNeoForge() {
        ModLoaderHook.DefaultHook = new ModLoaderHook() {
            @Override
            public boolean isModLoaded(String modId) {
                return ModList.get().isLoaded(modId);
            }
        };
        BtsEngineConcurrent.init();

        if(FMLEnvironment.production) return;
        NeoForge.EVENT_BUS.addListener(BtsEngineConcurrentNeoForge::onPlayerDropItem);
    }

    private static void onPlayerDropItem(ItemTossEvent event) {

        var player = event.getPlayer();
        if(player.level().isClientSide) return;

        BlocksBenchmark.placeCubesV3((ServerLevel) player.level(),
                player.blockPosition(),
                16,
                10,
                executorService).whenComplete((s1, s2) -> {
                    if(s2 != null) {
                        BtsEngineConcurrent.LOGGER.error(s2.getMessage(), s2);
                    } else {
                        BtsEngineConcurrent.LOGGER.info("Cubes placed!");
                    }
        });

//        ConcurrentBlockPlacer.placeBlocks(
//                (ServerLevel) player.level(),
//                player.blockPosition(),
//                32,
//                6,
//                executorService);
    }
}
