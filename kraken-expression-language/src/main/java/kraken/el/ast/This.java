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
public class This extends Reference {

    public This(Scope scope, Type evaluationType, Token token) {
        super(NodeType.THIS, scope, evaluationType, token);
    }

    @Override
    public boolean isReferenceInCurrentScope() {
        return true;
    }

    @Override
    public boolean isReferenceInGlobalScope() {
        return scope.getScopeType() == ScopeType.GLOBAL;
    }

    @Override
    public ScopeType findScopeTypeOfReference() {
        return scope.getScopeType();
    }

    @Override
    public boolean isSimpleBeanPath() {
        return true;
    }

    @Override
    public boolean isSimplePath() {
        return true;
    }

    @Override
    public Reference getFirstReference() {
        return this;
    }

    @Override
    public String toString() {
        return "this";
    }
}
