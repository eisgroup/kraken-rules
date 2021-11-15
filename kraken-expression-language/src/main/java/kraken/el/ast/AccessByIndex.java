/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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

/**
 * @author mulevicius
 */
public class AccessByIndex extends Reference {

    private Reference collection;

    private Expression indexExpression;

    public AccessByIndex(Reference collection, Expression indexExpression, Scope scope, Token token) {
        super(NodeType.ACCESS_BY_INDEX, scope, determineEvaluationType(collection), token);

        this.collection = collection;
        this.indexExpression = indexExpression;
    }

    public Reference getCollection() {
        return collection;
    }

    public Expression getIndexExpression() {
        return indexExpression;
    }

    @Override
    String getFirstToken() {
        return collection.getFirstToken();
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", collection, indexExpression);
    }

    private static Type determineEvaluationType(Reference reference) {
        if(reference.getEvaluationType() instanceof ArrayType) {
            return ((ArrayType) reference.getEvaluationType()).getElementType();
        }
        if(reference.getEvaluationType() == Type.ANY) {
            return Type.ANY;
        }
        return Type.UNKNOWN;
    }
}
