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

import javax.annotation.Nullable;

import kraken.el.ast.token.Token;
import kraken.el.scope.Scope;
import kraken.el.scope.ScopeType;
import kraken.el.scope.type.Type;

/**
 * @author mulevicius
 */
public class ReferenceValue extends Expression {

    private final This thisNode;

    private final Reference reference;

    public ReferenceValue(Reference reference, Scope scope, Token token) {
        super(NodeType.REFERENCE, scope, reference.getEvaluationType(), token);

        this.reference = reference;
        Reference firstReference = reference.getFirstReference();
        this.thisNode =  firstReference instanceof This ? (This) firstReference : null;
    }

    @Nullable
    public This getThisNode() {
        return thisNode;
    }

    public Reference getReference() {
        return reference;
    }

    /**
     * @return a type of scope that this reference is in
     */
    public ScopeType findScopeTypeOfReference() {
        return thisNode != null
            ? thisNode.getScope().findClosestReferencableScope().getScopeType()
            : reference.findScopeTypeOfReference();
    }

    @Override
    public String toString() {
        return reference.toString();
    }
}
