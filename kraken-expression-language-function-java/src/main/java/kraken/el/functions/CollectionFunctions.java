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
package kraken.el.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import kraken.el.functionregistry.ExpressionFunction;
import kraken.el.functionregistry.FunctionLibrary;
import kraken.el.functionregistry.Iterable;
import kraken.el.functionregistry.Native;

/**
 * @author mulevicius
 * @author avasiliauskas
 */
@SuppressWarnings("squid:S1118")
@Native
public class CollectionFunctions implements FunctionLibrary {

    /**
     * Flattens collection of collections into single collection
     *
     * @param collectionOfCollections
     * @param <T>
     * @return
     */
    @ExpressionFunction("Flat")
    public static <T> Collection<T> flat(Collection<Collection<T>> collectionOfCollections) {
        if (Objects.isNull(collectionOfCollections)) {
            return null;
        }
        final ArrayList<T> flattenedItems = new ArrayList<>();
        for (Collection<T> collection : collectionOfCollections) {
            flattenedItems.addAll(collection);
        }
        return flattenedItems;
    }

    /**
     * Count items in collection
     *
     * @param collection
     * @return
     */
    @ExpressionFunction("Count")
    public static Number count(@Iterable(false) Object collection) {
        if(collection == null) {
            return 0;
        }
        return collection instanceof Collection ? ((Collection)collection).size() : 1;
    }

    /**
     *
     * @param a
     * @param b
     * @param <T>
     * @return joined collection with preserved item order and duplicate items
     */
    @ExpressionFunction("Join")
    public static <T> Collection<T> join(Collection<T> a, Collection<T> b) {
        Collection<T> joinedCollection = new ArrayList<>();
        joinedCollection.addAll(nullToEmpty(a));
        joinedCollection.addAll(nullToEmpty(b));
        return joinedCollection;
    }

    private static <T> Collection<T> nullToEmpty(Collection<T> collection) {
        return collection == null ? Collections.emptyList() : collection;
    }

}
