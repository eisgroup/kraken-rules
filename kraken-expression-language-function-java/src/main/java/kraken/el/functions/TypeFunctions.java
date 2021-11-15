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

import kraken.el.InvocationContextHolder;
import kraken.el.functionregistry.ExpressionFunction;
import kraken.el.functionregistry.FunctionLibrary;
import kraken.el.functionregistry.Native;

import static kraken.el.functions.TypeProvider.TYPE_PROVIDER_PROPERTY;

/**
 * @author mulevicius
 */
@Native
public class TypeFunctions implements FunctionLibrary {

    /**
     * @param object that can be null
     * @return type of object as String; null if object is not represented as a type in system or if object is null.
     */
    @ExpressionFunction("GetType")
    public static String getType(Object object) {
        if(object == null) {
            return null;
        }
        return getTypeAdapter().getTypeOf(object);
    }

    private static TypeProvider getTypeAdapter() {
        InvocationContextHolder.InvocationContext invocationContext = InvocationContextHolder.getInvocationContext();
        return (TypeProvider) invocationContext.getVariables().get(TYPE_PROVIDER_PROPERTY);
    }
}
