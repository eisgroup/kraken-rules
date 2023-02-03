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
package kraken.el.ast.typeguard;

import kraken.el.ast.Expression;
import kraken.el.scope.type.Type;

/**
 * Models a return type of some part of expression deduced from type operators (instanceof, typeof).
 *
 * @author mulevicius
 */
public class TypeFact {

    private final Expression expression;

    private final Type type;

    public TypeFact(Expression expression, Type type) {
        this.expression = expression;
        this.type = type;
    }

    public Expression getExpression() {
        return expression;
    }

    public Type getType() {
        return type;
    }
}
