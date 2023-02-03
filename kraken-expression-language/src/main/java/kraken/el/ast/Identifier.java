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
import kraken.el.scope.symbol.VariableSymbol;
import kraken.el.scope.type.Type;

import static kraken.el.scope.type.Type.UNKNOWN;

/**
 * @author mulevicius
 */
public class Identifier extends Reference {

    private final String identifierToken;

    private final String identifier;

    private final String[] identifierParts;

    public Identifier(String identifier, Scope scope, Type evaluationType, Token token) {
        this(identifier, identifier, scope, evaluationType, token);
    }

    public Identifier(String identifierToken, String identifier, Scope scope, Type evaluationType, Token token) {
        super(NodeType.IDENTIFIER, scope, evaluationType, token);

        this.identifier = identifier;
        this.identifierToken = identifierToken;

        this.identifierParts = identifier.split("\\.");
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getIdentifierToken() {
        return identifierToken;
    }

    public String[] getIdentifierParts() {
        return identifierParts;
    }

    @Override
    public boolean isReferenceInCurrentScope() {
        return scope.isReferenceInCurrentScope(identifierToken);
    }

    @Override
    public boolean isReferenceInGlobalScope() {
        return scope.isReferenceInGlobalScope(identifierToken);
    }

    @Override
    public ScopeType findScopeTypeOfReference() {
        return scope.findScopeTypeOfReference(identifierToken);
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
        return identifier;
    }

}
