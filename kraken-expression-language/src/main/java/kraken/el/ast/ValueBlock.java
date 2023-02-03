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

import java.util.List;
import java.util.stream.Collectors;

import kraken.el.ast.token.Token;
import kraken.el.scope.Scope;

/**
 * @author mulevicius
 */
public class ValueBlock extends Expression {

    private final Expression value;

    private final List<Variable> variables;

    public ValueBlock(Expression value,
                      List<Variable> variables,
                      Scope scope,
                      Token token) {
        super(NodeType.VALUE_BLOCK, scope, value.getEvaluationType(), token);
        this.value = value;
        this.variables = variables;
    }

    public Expression getValue() {
        return value;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    @Override
    public String toString() {
        return
            variables.stream().map(Variable::toString).collect(Collectors.joining(" "))
                + (variables.isEmpty() ? value : " return " + value);
    }
}
