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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * @author mulevicius
 */
public final class FunctionDefinition {

    private final String functionName;

    private final String returnType;

    private final List<String> parameterTypes;

    private final Method method;

    private final Set<String> expressionTargets;

    private final boolean nativeFunction;

    public FunctionDefinition(String functionName,
                              String returnType,
                              List<String> parameterTypes,
                              Method method,
                              Set<String> expressionTargets,
                              boolean nativeFunction) {
        this.functionName = functionName;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.method = method;
        this.expressionTargets = expressionTargets;
        this.nativeFunction = nativeFunction;
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

    public long getParameterCount() {
        return parameterTypes.size();
    }

    public Set<String> getExpressionTargets() {
        return expressionTargets;
    }

    public boolean isNativeFunction() {
        return nativeFunction;
    }

    @Override
    public String toString() {
        return method.toString();
    }
}
