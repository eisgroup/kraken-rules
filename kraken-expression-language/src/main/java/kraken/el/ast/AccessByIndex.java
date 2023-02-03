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
import kraken.el.scope.ScopeType;
import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.Type;

/**
 * @author mulevicius
 */
public class AccessByIndex extends Reference {

    private final Reference collection;

    private final Expression indexExpression;

    public AccessByIndex(Reference collection, Expression indexExpression, Scope scope, Type evaluationType, Token token) {
        super(NodeType.ACCESS_BY_INDEX, scope, evaluationType, token);

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
    public boolean isReferenceInCurrentScope() {
        return collection.isReferenceInCurrentScope();
    }

    @Override
    public boolean isReferenceInGlobalScope() {
        return collection.isReferenceInGlobalScope();
    }

    @Override
    public ScopeType findScopeTypeOfReference() {
        return collection.findScopeTypeOfReference();
    }

    @Override
    public boolean isSimpleBeanPath() {
        return false;
    }

    @Override
    public boolean isSimplePath() {
        return false;
    }

    @Override
    public Reference getFirstReference() {
        return collection.getFirstReference();
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", collection, indexExpression);
    }

}
