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

import kraken.el.ast.builder.Literals;
import kraken.el.ast.token.Token;
import kraken.el.scope.type.Type;
import kraken.el.scope.Scope;

/**
 * @author mulevicius
 */
public class StringLiteral extends LiteralExpression<String> {

    public StringLiteral(String value, Scope scope, Token token) {
        super(value, NodeType.STRING, scope, Type.STRING, token);
    }

    @Override
    public String toString() {
        return "'" + Literals.deescape(getValue()) + "'";
    }
}
