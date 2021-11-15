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
package kraken.model.dsl.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents signature of a function specified in Kraken DSL
 *
 * @author mulevicius
 */
public class DSLFunction {

    private String functionName;

    private String returnType;

    private List<DSLFunctionParameter> parameters;

    public DSLFunction(String functionName,
                       String returnType,
                       List<DSLFunctionParameter> parameters) {
        this.functionName = Objects.requireNonNull(functionName);
        this.returnType = Objects.requireNonNull(returnType);
        this.parameters = Objects.requireNonNull(parameters);
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getReturnType() {
        return returnType;
    }

    public List<DSLFunctionParameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }
}
