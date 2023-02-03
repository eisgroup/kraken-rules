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
import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.Type;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mulevicius
 */
public class InlineArray extends Expression {

    private final List<Expression> items;

    public InlineArray(List<Expression> items, Scope scope, Token token) {
        super(NodeType.INLINE_ARRAY, scope, inlineArrayType(items), token);
        this.items = items;
    }

    public List<Expression> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return items.stream()
                .map(Object::toString)
                .collect(Collectors.joining(",", "{", "}"));
    }

    private static Type inlineArrayType(List<Expression> items) {
        Type type = determineInlineArrayItemType(items);
        return ArrayType.of(type);
    }

    private static Type determineInlineArrayItemType(List<Expression> items) {
        if(items.isEmpty()) {
            return Type.ANY;
        }
        Type type = items.get(0).getEvaluationType();
        for(Expression item : items) {
            type = type.resolveCommonTypeOf(item.getEvaluationType()).orElse(Type.ANY);
        }
        return type;
    }
}
