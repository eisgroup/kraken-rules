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

import kraken.el.InvocationContextHolder;
import kraken.el.TypeProvider;
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
    name = "Type",
    description = "Functions that operate with types of values and objects.",
    since = "1.0.34"
)
@Native
public class TypeFunctions implements FunctionLibrary {

    @FunctionDocumentation(
        description = "Returns type of object as a string. If the object is `null` then return `null`. "
            + "If the object does not have a type then `null` is returned. "
            + "The function only returns the type of entity object. "
            + "The function does not return types of native types like String, Number, or Boolean.",
        example = {
            @Example(value = "GetType(Coverage)", result = "RRCoverage"),
            @Example(
                value = "for c in Policy.blob.lobs[*].riskItems[*].coverages return GetType(c)",
                result = "{\"RRCoverage\", \"MEDCoverage\", \"RACoverage\"}"
            ),
            @Example(value = "GetType(5)", result = "null")
        }
    )
    @ExpressionFunction("GetType")
    public static String getType(
        @ParameterDocumentation(name = "entity", description = "Returns type of object as a string. "
            + "If the object is `null` or the object does not have a type then `null` is returned. "
            + "The function only returns the type of entity object. The function does not return types "
            + "of native types like String, Number, or Boolean.")
            Object object
    ) {
        if (object == null) {
            return null;
        }
        return getTypeAdapter().getTypeOf(object);
    }

    private static TypeProvider getTypeAdapter() {
        return InvocationContextHolder.getInvocationContext().getEvaluationContext().getTypeProvider();
    }
}
