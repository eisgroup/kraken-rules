/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package kraken.utils.cache;

import kraken.utils.dto.Pair;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Cache that accepts function and returns function with cache.
 *
 * @author psurinin
 */
public class Memoizer<T, U> {

    private final Map<T, U> cache = new ConcurrentHashMap<>();

    private Memoizer() {

    }

    /**
     * Example<br/>
     * <code>Memoizer.memoize(Integer::bitCount)</code>
     * @return one parameter function with cache.
     */
    public static <T, U> Function<T, U> memoize(Function<T, U> function) {
        final Memoizer<T, U> memoizer = new Memoizer<>();
        return t -> memoizer.doMemoize(t, function);
    }
    /**
     * Example<br/>
     * <code>Memoizer.memoize(Integer::compare)</code>
     * @return two parameters function with cache.
     */
    public static <T, U, K> BiFunction<K, T, U> memoize(BiFunction<K, T, U> function) {
        final Memoizer<Pair<K, T>, U> memoizer = new Memoizer<>();
        return (k, t) -> memoizer.doMemoize(new Pair<>(k, t), ktPair -> function.apply(ktPair.left, ktPair.right));
    }

    private U doMemoize(T t, Function<T, U> function) {
        return cache.computeIfAbsent(t, t1 -> function.apply(t));
    }

}