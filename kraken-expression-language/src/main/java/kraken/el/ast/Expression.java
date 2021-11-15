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
import kraken.el.scope.type.GenericType;
import kraken.el.scope.type.Type;

import java.util.Objects;

/**
 * @author mulevicius
 */
@SuppressWarnings("squid:S1694")
public abstract class Expression {

    protected NodeType nodeType;

    protected Scope scope;

    protected Type evaluationType;

    protected Token token;

    public Expression(NodeType nodeType, Scope scope, Type evaluationType, Token token) {
        if(evaluationType instanceof GenericType) {
            throw new IllegalStateException();
        }
        this.nodeType = Objects.requireNonNull(nodeType);
        this.scope = Objects.requireNonNull(scope);
        this.evaluationType = Objects.requireNonNull(evaluationType);
        this.token = Objects.requireNonNull(token);
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public Scope getScope() {
        return scope;
    }

    public Type getEvaluationType() {
        return evaluationType;
    }

    public Token getToken() {
        return token;
    }
}