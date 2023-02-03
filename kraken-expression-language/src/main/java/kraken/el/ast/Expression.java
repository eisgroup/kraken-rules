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

import java.util.Map;
import java.util.Objects;

import kraken.el.ast.token.Token;
import kraken.el.ast.typeguard.TypeFact;
import kraken.el.scope.Scope;
import kraken.el.scope.type.Type;

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

    /**
     * @return type facts that were deduced in scope of this expression node
     */
    public Map<String, TypeFact> getDeducedTypeFacts() {
        return Map.of();
    }

    /**
     * Returns true if expression is semantically empty.
     * Semantically empty expression is when it does not have any logic.
     * Semantically empty expression can be when expression consists only of symbols that does not express logic,
     * like empty spaces, comments or new lines.
     * Note, that semantically empty expression is still considered as a valid expression in KEL.
     *
     * @return
     */
    public boolean isEmpty() {
        return false;
    }
}