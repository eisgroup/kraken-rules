/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.model.project.validator;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import kraken.model.KrakenModelItem;

/**
 * Utility that detects duplicates in a collection of items by identity and then invokes a consumer
 * that can execute custom action on each set of duplicates
 *
 * @author mulevicius
 */
public class Duplicates {

    public static <T extends KrakenModelItem> void findAndDo(Stream<T> items, Consumer<List<T>> consumer) {
        findAndDo(items, KrakenModelItem::getName, consumer);
    }

    public static <T extends KrakenModelItem> void findAndDo(Collection<T> items, Consumer<List<T>> consumer) {
        findAndDo(items, KrakenModelItem::getName, consumer);
    }

    public static <T, I> void findAndDo(Collection<T> items, Function<T, I> toIdentifier, Consumer<List<T>> consumer) {
        findAndDo(items.stream(), toIdentifier, consumer);
    }

    public static <T, I> void findAndDo(Stream<T> items, Function<T, I> toIdentifier, Consumer<List<T>> consumer) {
        items.collect(Collectors.groupingBy(toIdentifier,
            Collectors.collectingAndThen(Collectors.toList(), duplicates -> {
                if(duplicates.size() > 1) {
                    consumer.accept(duplicates);
                }
                return duplicates;
            })
        ));
    }



}
