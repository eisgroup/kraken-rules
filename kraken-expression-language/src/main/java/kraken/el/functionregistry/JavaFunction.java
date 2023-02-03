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
package kraken.el.functionregistry;

import static kraken.el.scope.type.Type.toType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import kraken.el.scope.symbol.FunctionParameter;
import kraken.el.scope.symbol.FunctionSymbol;
import kraken.el.scope.type.Type;

/**
 * A function implemented in Java. Such a function can be invoked by calling {@link #getMethod()} through reflections
 *
 * @author mulevicius
 */
public final class JavaFunction {

    private final String functionName;

    private final String returnType;

    private final List<String> parameterTypes;

    private final Method method;

    private final Set<String> expressionTargets;

    private final boolean nativeFunction;

    private final List<GenericTypeInfo> genericTypes;
    
    public JavaFunction(String functionName,
                        String returnType,
                        List<String> parameterTypes,
                        Method method,
                        Set<String> expressionTargets,
                        boolean nativeFunction,
                        List<GenericTypeInfo> genericTypes) {
        this.functionName = functionName;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.method = method;
        this.expressionTargets = expressionTargets;
        this.nativeFunction = nativeFunction;
        this.genericTypes = genericTypes;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getReturnType() {
        return returnType;
    }

    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    public Method getMethod() {
        return method;
    }

    public Set<String> getExpressionTargets() {
        return expressionTargets;
    }

    public boolean isNativeFunction() {
        return nativeFunction;
    }

    public List<GenericTypeInfo> getGenericTypes() {
        return genericTypes;
    }
    
    public FunctionHeader header() {
        return new FunctionHeader(functionName, parameterTypes.size());
    }

    @Override
    public String toString() {
        return method.toString();
    }

    public FunctionSymbol toFunctionSymbol(Map<String, Type> types) {
        Map<String, Type> bounds = this.genericTypes.stream()
            .collect(Collectors.toMap(GenericTypeInfo::getGeneric, g -> toType(g.getBound(), types), (v1, v2) -> v1));

        return new FunctionSymbol(
            functionName,
            toType(returnType, types, bounds),
            toParameters(parameterTypes, types, bounds)
        );
    }

    private static List<FunctionParameter> toParameters(List<String> parameterTypes,
                                                        Map<String, Type> types,
                                                        Map<String, Type> bounds) {
        List<FunctionParameter> parameters = new ArrayList<>();
        for(int i = 0; i < parameterTypes.size(); i++) {
            Type parameterType = toType(parameterTypes.get(i), types, bounds);
            parameters.add(new FunctionParameter(i, parameterType));
        }
        return parameters;
    }
}
