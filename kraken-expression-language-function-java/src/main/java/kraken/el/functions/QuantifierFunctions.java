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
import java.util.Collections;

import kraken.el.functionregistry.ExpressionFunction;
import kraken.el.functionregistry.FunctionLibrary;
import kraken.el.functionregistry.Native;

import static java.util.function.Predicate.isEqual;

/**
 * @author mulevicius
 */
@Native
public class QuantifierFunctions implements FunctionLibrary {

    /**
     *
     * @param any
     * @return true if collection is not empty and at least one item is true
     */
    @ExpressionFunction("Any")
    public static Boolean any(Collection<Boolean> any) {
        return nullToEmpty(any).stream().anyMatch(isEqual(true));
    }

    /**
     *
     * @param all
     * @return true if collection is empty or every item is true
     */
    @ExpressionFunction("All")
    public static Boolean all(Collection<Boolean> all) {
        return nullToEmpty(all).stream().allMatch(isEqual(true));
    }

    private static <T> Collection<T> nullToEmpty(Collection<T> collection) {
        return collection == null ? Collections.emptyList() : collection;
    }
}
