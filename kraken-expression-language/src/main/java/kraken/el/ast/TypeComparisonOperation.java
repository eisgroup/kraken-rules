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

import java.util.Map;
import java.util.Set;

import kraken.el.ast.token.Token;
import kraken.el.ast.typeguard.TypeFact;
import kraken.el.scope.Scope;
import kraken.el.scope.type.Type;

public class TypeComparisonOperation extends Expression {

    private final Expression left;

    private final TypeLiteral typeLiteral;

    public TypeComparisonOperation(Expression left, TypeLiteral typeLiteral, NodeType nodeType, Scope scope, Token token) {
        super(nodeType, scope, Type.BOOLEAN, token);

        this.left = left;
        this.typeLiteral = typeLiteral;
    }

    public Expression getLeft() {
        return left;
    }

    public TypeLiteral getTypeLiteral() {
        return typeLiteral;
    }

    @Override
    public Map<String, TypeFact> getDeducedTypeFacts() {
        String token = left.getToken().getText();
        Type castedType = scope.resolveTypeOf(typeLiteral.getValue());
        if(castedType.isKnown()) {
            return Map.of(token, new TypeFact(left, scope.resolveTypeOf(typeLiteral.getValue())));
        }
        return Map.of();
    }

    @Override
    public String toString() {
        return "(" + getLeft() + " " + getNodeType().getOperator() + " " + typeLiteral + ")";
    }

}
