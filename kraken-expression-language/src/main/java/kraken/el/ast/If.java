/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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
import kraken.el.scope.type.Type;

import java.util.Optional;

/**
 * @author mulevicius
 */
public class If extends Expression {

    private final Expression condition;

    private final Expression thenExpression;

    private final Expression elseExpression;

    public If(Expression condition, Expression thenExpression, Expression elseExpression, Scope scope, Token token) {
        super(NodeType.IF, scope, determineEvaluationType(thenExpression, elseExpression), token);
        this.condition = condition;
        this.thenExpression = thenExpression;
        this.elseExpression = elseExpression;
    }

    public Expression getCondition() {
        return condition;
    }

    public Expression getThenExpression() {
        return thenExpression;
    }

    public Optional<Expression> getElseExpression() {
        return Optional.ofNullable(elseExpression);
    }

    private static Type determineEvaluationType(Expression then, Expression elze) {
        if(elze == null) {
            return then.getEvaluationType();
        }
        return then.getEvaluationType()
            .resolveCommonTypeOf(elze.getEvaluationType())
            .orElse(Type.UNKNOWN);
    }

    @Override
    public String toString() {
        return "(if " + condition +
                " then " + thenExpression +
                Optional.ofNullable(elseExpression).map(e -> " else " + e).orElse("") + ")";
    }
}
