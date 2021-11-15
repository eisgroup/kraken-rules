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
import kraken.el.scope.symbol.VariableSymbol;

import static kraken.el.scope.type.Type.UNKNOWN;

/**
 * @author mulevicius
 */
public class Identifier extends Reference {

    private String identifierToken;

    private String identifier;

    public Identifier(String identifier, Scope scope, Token token) {
        this(identifier, identifier, scope, token);
    }

    public Identifier(String identifierToken, String identifier, Scope scope, Token token) {
        super(NodeType.IDENTIFIER, scope, scope.resolveReferenceSymbol(identifierToken).map(VariableSymbol::getType).orElse(UNKNOWN), token);

        this.identifier = identifier;
        this.identifierToken = identifierToken;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getIdentifierToken() {
        return identifierToken;
    }

    @Override
    String getFirstToken() {
        return identifierToken;
    }

    @Override
    public String toString() {
        return identifier;
    }

    @Override
    public boolean isSimpleBeanPath() {
        return true;
    }
}
