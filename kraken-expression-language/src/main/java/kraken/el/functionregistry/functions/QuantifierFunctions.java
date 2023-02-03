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

import static java.util.function.Predicate.isEqual;

import java.util.Collection;
import java.util.Collections;

import kraken.el.functionregistry.Example;
import kraken.el.functionregistry.ExpressionFunction;
import kraken.el.functionregistry.FunctionDocumentation;
import kraken.el.functionregistry.FunctionLibrary;
import kraken.el.functionregistry.LibraryDocumentation;
import kraken.el.functionregistry.Native;
import kraken.el.functionregistry.ParameterDocumentation;

/**
 * @author mulevicius
 */
@LibraryDocumentation(
    name = "Quantifier",
    description = "Functions that provide logical quantification operations.",
    since = "1.0.30"
)
@Native
public class QuantifierFunctions implements FunctionLibrary {

    @FunctionDocumentation(
        description = "Returns `true` if collection is not empty and if any boolean value in collection is `true`. "
            + "`null` value in collection is treated as not `true` value. "
            + "`null` collection parameter is treated as an empty collection.",
        example = {
            @Example(value = "Any({true, false})", result = "true"),
            @Example(value = "Any({false, false})", result = "false"),
            @Example(value = "Any({null})", result = "false"),
            @Example(value = "Any({})", result = "false"),
            @Example(value = "Any(Policy.riskItems[*].isPrimary)"),
        }
    )
    @ExpressionFunction("Any")
    public static Boolean any(
        @ParameterDocumentation(name = "collection", description = "collection of booleans")
            Collection<Boolean> any
    ) {
        return nullToEmpty(any).stream().anyMatch(isEqual(true));
    }

    @FunctionDocumentation(
        description = "Returns `true` if collection is empty or if all boolean values in collection are `true`. "
            + "`null` value in collection is treated as not `true` value. "
            + "`null` collection parameter is treated as an empty collection.",
        example = {
            @Example(value = "All({true, false})", result = "false"),
            @Example(value = "All({true, true})", result = "true"),
            @Example(value = "All({false, false})", result = "false"),
            @Example(value = "All({null})", result = "false"),
            @Example(value = "All({})", result = "false"),
            @Example(value = "All(Policy.riskItems[*].isPrimary)"),
        }
    )
    @ExpressionFunction("All")
    public static Boolean all(
        @ParameterDocumentation(name = "collection", description = "collection of booleans")
            Collection<Boolean> all
    ) {
        return nullToEmpty(all).stream().allMatch(isEqual(true));
    }

    private static <T> Collection<T> nullToEmpty(Collection<T> collection) {
        return collection == null ? Collections.emptyList() : collection;
    }
}
