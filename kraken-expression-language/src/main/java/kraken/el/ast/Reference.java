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
import kraken.el.scope.type.Type;

/**
 * @author mulevicius
 */
public abstract class Reference extends Expression {

    public Reference(NodeType nodeType, Scope scope, Type evaluationType, Token token) {
        super(nodeType, scope, evaluationType, token);
    }

    /**
     * @return first token in reference
     * For example, in {@code Coverage[1][*].limitAmount[1]} first token is 'Coverage'
     */
    abstract String getFirstToken();

    /**
     * @return true if this reference exists in current immediate scope;
     *              a reference is in current immediate scope if the scope is static and has reference by name
     *              or a scope is dynamic and no static parent scope has this reference
     */
    public boolean isReferenceInCurrentScope() {
        return scope.isReferenceInCurrentScope(getFirstToken());
    }

    /**
     * @return true if this reference exists only in global scope but not in any descendant scope
     */
    public boolean isReferenceInGlobalScope() {
        return scope.isReferenceInGlobalScope(getFirstToken());
    }

    /**
     * @return a type of scope that has this reference is in
     */
    public ScopeType findScopeTypeOfReference() {
        return scope.findScopeTypeOfReference(getFirstToken());
    }

    public boolean isSimpleBeanPath() {
        return false;
    }

}
