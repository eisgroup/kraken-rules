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
import kraken.el.scope.ScopeType;

public class Cast extends Reference {

    private final TypeLiteral typeLiteral;

    private final Reference reference;

    public Cast(TypeLiteral typeLiteral, Reference reference, Scope scope, Token token) {
        super(NodeType.CAST, scope, scope.resolveTypeOf(typeLiteral.getValue()), token);

        this.typeLiteral = typeLiteral;
        this.reference = reference;
    }

    public TypeLiteral getTypeLiteral() {
        return typeLiteral;
    }

    public Reference getReference() {
        return reference;
    }

    @Override
    public String toString() {
        return "(" + getTypeLiteral() + ")" + getReference();
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
    public boolean isReferenceInCurrentScope() {
        return reference.isReferenceInCurrentScope();
    }

    @Override
    public boolean isReferenceInGlobalScope() {
        return reference.isReferenceInGlobalScope();
    }

    @Override
    public ScopeType findScopeTypeOfReference() {
        return reference.findScopeTypeOfReference();
    }

    @Override
    public Reference getFirstReference() {
        return reference.getFirstReference();
    }
}
