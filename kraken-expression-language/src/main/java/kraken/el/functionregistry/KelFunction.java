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
package kraken.el.functionregistry;

import java.util.List;

import kraken.el.ast.Ast;

/**
 * Function implemented in KEL. Such function can be invoked by using
 * {@link kraken.el.interpreter.evaluator.InterpretingExpressionEvaluator} and interpreting {@link #getBody()}
 *
 * @author mulevicius
 */
public class KelFunction {

    private final String name;
    private final List<Parameter> parameters;
    private final Ast body;

    public KelFunction(String name, List<Parameter> parameters, Ast body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public Ast getBody() {
        return body;
    }

    public FunctionHeader header() {
        return new FunctionHeader(name, parameters.size());
    }

    public static class Parameter {
        private final String name;

        public Parameter(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
