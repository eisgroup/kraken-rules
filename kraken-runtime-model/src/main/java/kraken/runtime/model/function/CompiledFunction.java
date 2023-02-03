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
package kraken.runtime.model.function;

import java.util.List;

import kraken.runtime.model.expression.CompiledExpression;

/**
 * @author mulevicius
 */
public class CompiledFunction {

    private final String name;
    private final List<Parameter> parameters;
    private final String returnType;
    private final CompiledExpression body;

    public CompiledFunction(String name, List<Parameter> parameters, String returnType, CompiledExpression body) {
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public String getReturnType() {
        return returnType;
    }

    public CompiledExpression getBody() {
        return body;
    }
}
