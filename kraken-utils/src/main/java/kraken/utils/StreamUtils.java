/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
package kraken.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Contains utility methods for working with streams.
 *
 * @author gvisokinskas
 */
public final class StreamUtils {
    /**
     * Private constructor for utility class.
     */
    public StreamUtils() {
    }

    /**
     * Allows to define a function that consumes the provided object and returns it.
     * Mainly used to replace the {@link java.util.stream.Stream#peek(Consumer)}.
     * This method should be called from {@link java.util.stream.Stream#map(Function)}.
     *
     * @param consumer the consumer to apply on the provided object.
     * @param <T>      the type of the object coming from upstream.
     * @return an instance of {@link Function} that accepts a single argument, consumes it and returns it.
     */
    public static <T> Function<T, T> consume(Consumer<T> consumer) {
        return x -> {
            consumer.accept(x);
            return x;
        };
    }

    /**
     * A predicate that could be used to filter a stream of objects based on a single property.
     *
     * @param keyExtractor the property extractor function.
     * @param <T> the type of the upstream elements.
     * @return the predicate that filters out stream elements with duplicate properties.
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /**
     * An ordered map collector that maintains the order of the collected objects from the stream.
     *
     * @return an instance of {@link Collector} that collects items into an {@link SortedMap} implementation.
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> orderedMapCollector(Function<? super T, ? extends K> keyMapper,
                                                                           Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(keyMapper, valueMapper,
                                (v1, v2) -> {
                                    throw new IllegalArgumentException(String.format("Duplicate key for values %s and %s", v1, v2));
                                },
                                LinkedHashMap::new);
    }
}
