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
package kraken.el.functions;

import java.util.Collection;

import kraken.el.functionregistry.ExpressionFunction;
import kraken.el.functionregistry.FunctionLibrary;
import kraken.el.functionregistry.Iterable;
import kraken.el.functionregistry.Native;

/**
 * @author mulevicius
 * @since 1.0.30
 */
@Native
public class GenericValueFunctions implements FunctionLibrary {

    /**
     * @param value
     * @return true if value is null or empty
     */
    @ExpressionFunction("IsEmpty")
    public static Boolean isEmpty(@Iterable(false) Object value) {
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
