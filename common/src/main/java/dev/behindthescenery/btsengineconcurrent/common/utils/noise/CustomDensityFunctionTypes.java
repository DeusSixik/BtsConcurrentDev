package dev.behindthescenery.btsengineconcurrent.common.utils.noise;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.function.BiFunction;
import java.util.function.Function;

public class CustomDensityFunctionTypes {

    static <A, O> KeyDispatchDataCodec<O> holderOf(Codec<A> codec, Function<A, O> fromFunction, Function<O, A> toFunction) {
        return KeyDispatchDataCodec.of(codec.fieldOf("argument").xmap(fromFunction, toFunction));
    }

    static <O> KeyDispatchDataCodec<O> holderOf(Function<DensityFunction, O> fromFunction, Function<O, DensityFunction> toFunction) {
        return holderOf(DensityFunction.HOLDER_HELPER_CODEC, fromFunction, toFunction);
    }

    static <O> KeyDispatchDataCodec<O> holderOf(BiFunction<DensityFunction, DensityFunction, O> fromFunction, Function<O, DensityFunction> primary, Function<O, DensityFunction> secondary) {
        return KeyDispatchDataCodec.of(RecordCodecBuilder.mapCodec((instance) -> instance.group(DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument1").forGetter(primary), DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument2").forGetter(secondary)).apply(instance, fromFunction)));
    }

    static <O> KeyDispatchDataCodec<O> holderOf(MapCodec<O> mapCodec) {
        return KeyDispatchDataCodec.of(mapCodec);
    }

    public static <O> KeyDispatchDataCodec<O> holderOf(Function3<DensityFunction, DensityFunction, DensityFunction, O> creator,
                                              Function<O, DensityFunction> argument1Getter,
                                              Function<O, DensityFunction> argument2Getter,
                                              Function<O, DensityFunction> argument3Getter) {
        return KeyDispatchDataCodec.of(RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                                (DensityFunction.DIRECT_CODEC.fieldOf("argument1"))
                                        .forGetter(argument1Getter),
                                (DensityFunction.DIRECT_CODEC.fieldOf("argument2"))
                                        .forGetter(argument2Getter),
                                (DensityFunction.DIRECT_CODEC.fieldOf("argument3"))
                                        .forGetter(argument3Getter))
                        .apply(instance, creator)));
    }
}
