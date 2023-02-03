/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.model.dsl.model;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

/**
 * Represents function implementation specified in Kraken DSL
 *
 * @author mulevicius
 */
public class DSLFunction {

    private final String functionName;

    private final String returnType;

    private final List<DSLFunctionParameter> parameters;

    private final List<DSLGenericTypeBound> genericTypeBounds;

    private final DSLExpression body;

    private final DSLFunctionDocumentation documentation;

    public DSLFunction(String functionName,
                       String returnType,
                       List<DSLFunctionParameter> parameters,
                       List<DSLGenericTypeBound> genericTypeBounds,
                       DSLExpression body,
                       @Nullable DSLFunctionDocumentation documentation) {
        this.functionName = Objects.requireNonNull(functionName);
        this.returnType = Objects.requireNonNull(returnType);
        this.parameters = Objects.requireNonNull(parameters);
        this.genericTypeBounds = Objects.requireNonNull(genericTypeBounds);
        this.body = Objects.requireNonNull(body);
        this.documentation = documentation;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getReturnType() {
        return returnType;
    }

    public List<DSLFunctionParameter> getParameters() {
        return parameters;
    }

    public List<DSLGenericTypeBound> getGenericTypeBounds() {
        return genericTypeBounds;
    }

    public DSLExpression getBody() {
        return body;
    }

    @Nullable
    public DSLFunctionDocumentation getDocumentation() {
        return documentation;
    }
}
