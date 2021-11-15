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
package kraken.el.function;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import kraken.el.accelerated.ReflectionsCache;
import kraken.el.functionregistry.ExpressionFunction;
import kraken.el.functionregistry.FunctionLibrary;
import kraken.el.functionregistry.NotNull;

/**
 * @author mulevicius
 */
public class FunctionInvokerTestFunctions implements FunctionLibrary {

    @ExpressionFunction
    public static Object context(String key) {
        return key;
    }

    @ExpressionFunction
    public static Object context(String key, String overloadedParam) {
        return key;
    }

    @ExpressionFunction("Name")
    public static Object name() {
        return "result";
    }

    @ExpressionFunction
    public static Double add(Double value1, Double value2) {
        return value1 + value2;
    }

    @ExpressionFunction
    public static void set(@NotNull Object object, String property, Object value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ReflectionsCache.getSettersOrCompute(object.getClass()).get(property).invoke(object, value);
    }

    @ExpressionFunction
    public static Object get(@NotNull Object object, String property) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return ReflectionsCache.getGettersOrCompute(object.getClass()).get(property).invoke(object);
    }

    @ExpressionFunction
    public static Number count(Collection collection) {
        return collection != null
                ? collection.size()
                : 0;
    }
}
