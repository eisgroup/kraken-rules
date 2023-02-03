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
package kraken.el.functionregistry.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import kraken.el.functionregistry.Example;
import kraken.el.functionregistry.ExpressionFunction;
import kraken.el.functionregistry.FunctionDocumentation;
import kraken.el.functionregistry.FunctionLibrary;
import kraken.el.functionregistry.Iterable;
import kraken.el.functionregistry.LibraryDocumentation;
import kraken.el.functionregistry.Native;
import kraken.el.functionregistry.ParameterDocumentation;

/**
 * @author mulevicius
 * @author avasiliauskas
 */
@SuppressWarnings("squid:S1118")
@Native
@LibraryDocumentation(
    name = "Collection",
    description = "Functions for operating with collections.",
    since = "1.0.28"
)
public class CollectionFunctions implements FunctionLibrary {

    @FunctionDocumentation(
        description = "Flatten collection of collections to a one-dimensional collection.",
        example = {
            @Example(value = "Flat({{'a', 'b'}, {'c', 'd'}})", result = "{'a', 'b', 'c', 'd'}"),
            @Example(value = "Flat(null)", result = "null"),
        }
    )
    @ExpressionFunction("Flat")
    public static <T> Collection<T> flat(
        @ParameterDocumentation(
            name = "collection",
            description = "collection of collections"
        )
            Collection<Collection<T>> collectionOfCollections
    ) {
        if (Objects.isNull(collectionOfCollections)) {
            return null;
        }
        final ArrayList<T> flattenedItems = new ArrayList<>();
        for (Collection<T> collection : collectionOfCollections) {
            flattenedItems.addAll(collection);
        }
        return flattenedItems;
    }

    @FunctionDocumentation(
        description = "Resolve size of collection. If collection is `null`, returns 0. "
            + "If argument is not a collection, returns 1.",
        example = {
            @Example("Count(coverages)"),
            @Example(value = "Count(null)", result = "0"),
            @Example(value = "Count('abc')", result = "1"),
        }
    )
    @ExpressionFunction("Count")
    public static Number count(
        @Iterable(false)
        @ParameterDocumentation(
            name = "collection",
            description = "collection if items to count"
        ) Object collection
    ) {
        if (collection == null) {
            return 0;
        }
        return collection instanceof Collection ? ((Collection) collection).size() : 1;
    }

    @FunctionDocumentation(
        description = "Join two collections into single collection by preserving order from "
            + "the original collections and by preserving duplicates. Parameter types in both "
            + "collections must be the same. Joining collections of different types is not supported. "
            + "`null` value in collection is treated as a valid value and is preserved. "
            + "`null` collection parameter is treated as an empty collection.",
        example = {
            @Example("Join(RiskItem.coverages.coverageCd, Policy.coverages.coverageCd)")
        },
        since = "1.0.30"
    )
    @ExpressionFunction("Join")
    public static <T> Collection<T> join(
        @ParameterDocumentation(
            name = "collection1",
            description = "First collection to join"
        ) Collection<T> a,
        @ParameterDocumentation(
            name = "collection2",
            description = "Second collection to join"
        )Collection<T> b
    ) {
        Collection<T> joinedCollection = new ArrayList<>();
        joinedCollection.addAll(nullToEmpty(a));
        joinedCollection.addAll(nullToEmpty(b));
        return joinedCollection;
    }

    private static <T> Collection<T> nullToEmpty(Collection<T> collection) {
        return collection == null ? Collections.emptyList() : collection;
    }

}
