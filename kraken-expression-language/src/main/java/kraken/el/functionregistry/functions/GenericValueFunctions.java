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

import java.util.Collection;

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
 * @since 1.0.30
 */
@LibraryDocumentation(
    name = "Generic Value",
    since = "1.0.30",
    description = "Functions that operate with general values."
)
@Native
public class GenericValueFunctions implements FunctionLibrary {

    @FunctionDocumentation(
        description = "Return `true` if collection parameter value is `null` or is empty collection. "
            + "`null` value in collection is treated as a valid value and the collection is considered "
            + "not empty. Handles string and collections. Otherwise will return false.",
        example = {
            @Example(value = "IsEmpty(null)", result = "true"),
            @Example(value = "IsEmpty({})", result = "true"),
            @Example(value = "IsEmpty({1})", result = "false"),
            @Example(value = "IsEmpty('')", result = "true"),
            @Example(value = "IsEmpty('a')", result = "false"),
        }
    )
    @ExpressionFunction("IsEmpty")
    public static Boolean isEmpty(
        @Iterable(false) @ParameterDocumentation(name = "stringOrCollection") Object value
    ) {
        if(value == null) {
            return true;
        }
        if(value instanceof String) {
            return ((String) value).isEmpty();
        }
        if(value instanceof Collection) {
            return ((Collection) value).isEmpty();
        }
        return false;
    }
}
