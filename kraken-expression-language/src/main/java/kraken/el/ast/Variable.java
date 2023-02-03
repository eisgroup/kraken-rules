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
package kraken.el.ast;

import kraken.el.ast.token.Token;
import kraken.el.scope.Scope;

/**
 * @author mulevicius
 */
public class Variable extends Expression {

    private final String variableName;
    private final Expression value;

    public Variable(String variableName, Expression value, Scope scope, Token token) {
        super(NodeType.VARIABLE, scope, value.getEvaluationType(), token);

        this.variableName = variableName;
        this.value = value;
    }

    public String getVariableName() {
        return variableName;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "var " + variableName + " = " + value;
    }
}
