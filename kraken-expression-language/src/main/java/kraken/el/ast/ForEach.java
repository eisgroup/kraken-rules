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

/**
 * @author mulevicius
 * @since 1.0.30
 */
public class ForEach extends Expression {

    private final String var;

    private final Expression collection;

    private final Expression returnExpression;

    public ForEach(String var, Expression collection, Expression returnExpression, Scope scope, Token token) {
        super(NodeType.FOR, scope, returnExpression.getEvaluationType().wrapArrayType(), token);

        this.var = var;
        this.collection = collection;
        this.returnExpression = returnExpression;
    }

    public String getVar() {
        return var;
    }

    public Expression getCollection() {
        return collection;
    }

    public Expression getReturnExpression() {
        return returnExpression;
    }

    @Override
    public String toString() {
        return "(for " + var + " in " + collection + " return " + returnExpression + ")";
    }
}
