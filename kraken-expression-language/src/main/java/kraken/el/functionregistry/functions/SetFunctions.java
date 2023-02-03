/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
import java.util.HashSet;
import java.util.Set;

import kraken.el.functionregistry.Example;
import kraken.el.functionregistry.ExpressionFunction;
import kraken.el.functionregistry.FunctionDocumentation;
import kraken.el.functionregistry.FunctionLibrary;
import kraken.el.functionregistry.LibraryDocumentation;
import kraken.el.functionregistry.Native;
import kraken.el.functionregistry.ParameterDocumentation;

/**
 * @author mulevicius
 * @since 1.0.30
 */
@LibraryDocumentation(
    name = "Set",
    description = "Functions that provide Set operations.",
    since = "1.0.30"
)
@Native
public class SetFunctions implements FunctionLibrary {

    @FunctionDocumentation(
        description = "Create a set union between two collections. "
            + "Parameter types in both collections must be the same and must be primitive types. "
            + "Joining collections of different types or of complex object types is not supported. "
            + "`null` value in collection is treated as a valid value and is preserved. "
            + "`null` collection parameter is treated as an empty collection.",
        example = {
            @Example(value = "Union({1,2}, {2,3})", result = "{1,2,3}"),
            @Example(value = "Union({1,2}, null)", result = "{1,2}"),
            @Example(value = "Union(null, {1,2})", result = "{1,2}"),
            @Example("Union(RiskItem.coverages[*].coverageCd, Policy.coverages[*].coverageCd)")
        }
    )
    @ExpressionFunction("Union")
    public static <T> Collection<T> union(
        @ParameterDocumentation(
            name = "collection1",
            description = "`null` collection parameter is treated as an empty collection"
        ) Collection<T> a,
        @ParameterDocumentation(
            name = "collection2",
            description = "`null` collection parameter is treated as an empty collection"
        ) Collection<T> b
    ) {
        Set<T> firstSet = new HashSet<>(nullToEmpty(a));
        firstSet.addAll(nullToEmpty(b));
        return new ArrayList<>(firstSet);
    }

    @FunctionDocumentation(
        description = "Create a set intersection between two collections. "
            + "Parameter types in both collections must be the same and must be primitive types. "
            + "Joining collections of different types or of complex object types is not supported. "
            + "`null` value in collection is treated as a valid value and is preserved. "
            + "`null` collection parameter is treated as an empty collection.",
        example = {
            @Example(value = "Intersection({1,2}, {2,3})", result = "{2}"),
            @Example(value = "Intersection({1,2}, null)", result = "{} // empty collection"),
            @Example(value = "Intersection(null, {1,2})", result = "{} // empty collection"),
            @Example(value = "Intersection(RiskItem.coverages[*].coverageCd, Policy.coverages[*].coverageCd)")
        }
    )
    @ExpressionFunction("Intersection")
    public static <T> Collection<T> intersection(
        @ParameterDocumentation(
            name = "collection1",
            description = "`null` collection parameter is treated as an empty collection"
        ) Collection<T> a,
        @ParameterDocumentation(
            name = "collection2",
            description = "`null` collection parameter is treated as an empty collection"
        ) Collection<T> b
    ) {
        Set<T> firstSet = new HashSet<>(nullToEmpty(a));
        firstSet.retainAll(nullToEmpty(b));
        return new ArrayList<>(firstSet);
    }

    @FunctionDocumentation(
        description = "Create a set difference between two collections. "
            + "Parameter types in both collections must be the same and must be primitive types. "
            + "Joining collections of different types or of complex object types is not supported. "
            + "`null` value in collection is treated as a valid value and is preserved. "
            + "`null` collection parameter is treated as an empty collection.",
        example = {
            @Example(value = "Difference({1,2}, {2,3})", result = "{1}"),
            @Example(value = "Difference({1,2}, null)", result = "{1,2}"),
            @Example(value = "Difference(null, {1,2})", result = "{1,2}"),
            @Example(value = "Difference(RiskItem.coverages[*].coverageCd, Policy.coverages[*].coverageCd)")
        }
    )
    @ExpressionFunction("Difference")
    public static <T> Collection<T> difference(
        @ParameterDocumentation(
            name = "collection1",
            description = "`null` collection parameter is treated as an empty collection"
        ) Collection<T> a,
        @ParameterDocumentation(
            name = "collection2",
            description = "`null` collection parameter is treated as an empty collection"
        ) Collection<T> b
    ) {
        Set<T> firstSet = new HashSet<>(nullToEmpty(a));
        firstSet.removeAll(nullToEmpty(b));
        return new ArrayList<>(firstSet);
    }

    @FunctionDocumentation(
        description =
            "Create a set symmetric difference between two collections. "
                + "Parameter types in both collections must be the same and must be primitive types. "
                + "Joining collections of different types or of complex object types is not supported. "
                + "`null` value in collection is treated as a valid value and is preserved. "
                + "`null` collection parameter is treated as an empty collection.",
        example = {
            @Example(value = "SymmetricDifference({1,2}, {2,3})", result = "{1,3}"),
            @Example(value = "SymmetricDifference({1,2}, {2,3,null})", result = "{1,3,null}"),
            @Example(value = "SymmetricDifference({1,2}, null)", result = "{1,2}"),
            @Example(value = "SymmetricDifference(null, {1,2})", result = "{1,2}"),
            @Example(value = "SymmetricDifference(RiskItem.coverages[*].coverageCd, Policy.coverages[*].coverageCd)")
        }
    )
    @ExpressionFunction("SymmetricDifference")
    public static <T> Collection<T> symmetricDifference(
        @ParameterDocumentation(
            name = "collection1",
            description = "`null` collection parameter is treated as an empty collection"
        ) Collection<T> a,
        @ParameterDocumentation(
            name = "collection2",
            description = "`null` collection parameter is treated as an empty collection"
        ) Collection<T> b
    ) {
        Set<T> firstSet = new HashSet<>(nullToEmpty(a));
        firstSet.removeAll(nullToEmpty(b));
        Set<T> secondSet = new HashSet<>(nullToEmpty(b));
        secondSet.removeAll(nullToEmpty(a));
        firstSet.addAll(secondSet);
        return new ArrayList<>(firstSet);
    }

    @FunctionDocumentation(
        description = "Discard duplicates. Order of items in original collection is not preserved. "
            + "Parameter type in collection must be primitive type. "
            + "`null` value in collection is treated as a valid value and is preserved. "
            + "`null` collection parameter is treated as an empty collection.",
        example = {
            @Example(value = "Distinct({1,2,2,3,3,3})", result = "{1,2,3}"),
            @Example(value = "Distinct(null)", result = "{}"),
            @Example(value = "Distinct({null})", result = "{null}")
        }
    )
    @ExpressionFunction("Distinct")
    public static <T> Collection<T> distinct(
        @ParameterDocumentation(
            name = "collection",
            description = "`null` collection parameter is treated as an empty collection"
        ) Collection<T> a
    ) {
        return new ArrayList<>(new HashSet<>(nullToEmpty(a)));
    }

    private static <T> Collection<T> nullToEmpty(Collection<T> collection) {
        return collection == null ? Collections.emptyList() : collection;
    }
}
