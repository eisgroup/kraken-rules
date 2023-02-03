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

import javax.annotation.Nullable;

import kraken.el.ast.token.Token;
import kraken.el.scope.Scope;
import kraken.el.scope.ScopeType;
import kraken.el.scope.type.Type;

/**
 * Represent collection filter reference expression.
 * It must filter and return filtered collection
 *
 * @author psurinin
 */
public class CollectionFilter extends Reference {

    private final Reference collection;
    private final Expression predicate;

    public CollectionFilter(Reference collection, Expression predicate, Scope scope, Type evaluationType, Token token) {
        super(NodeType.COLLECTION_FILTER, scope, evaluationType, token);

        this.collection = collection;
        this.predicate = predicate;
    }

    public Reference getCollection() {
        return collection;
    }

    /**
     *
     * @return predicate or null if collection filter selects all
     */
    @Nullable
    public Expression getPredicate() {
        return predicate;
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
        return collection + "[" + (predicate == null ? "*" : predicate) + "]";
    }

}
