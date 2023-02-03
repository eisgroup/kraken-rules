/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.el.ast.dependency;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import kraken.el.ast.Expression;
import kraken.el.ast.Identifier;
import kraken.el.ast.visitor.AstTraversingVisitor;
import kraken.el.scope.Scope;

/**
 * Collects every known reference defined in expression.
 * Only references to known types are collected.
 * If type of reference is not known, or it is of internal anonymous type (for example, variables map),
 * then such reference is not collected.
 *
 * @author mulevicius
 */
public class ReferenceResolvingVisitor extends AstTraversingVisitor {

    protected final Set<Reference> references = new LinkedHashSet<>();

    private final Scope scope;

    public ReferenceResolvingVisitor(Scope scope) {
        this.scope = scope;
    }

    @Override
    public Expression visit(Identifier identifier) {
        var reference = identifier.getIdentifierToken();
        if(identifier.isReferenceInGlobalScope()) {
            references.add(new Reference(null, reference, true));
        } else {
            identifier.getScope().findScopeOfReference(reference)
                .map(Scope::getType)
                .filter(type -> !type.isDynamic())
                .filter(type -> !type.isPrimitive())
                .filter(type -> type.isKnown())
                .filter(type -> scope.getAllTypes().containsKey(type.getName()))
                .ifPresent(type -> references.add(new Reference(type.getName(), reference, false)));
        }
        return super.visit(identifier);
    }

    /**
     * @return a set of every collected reference
     */
    public Set<Reference> getReferences() {
        return Collections.unmodifiableSet(references);
    }

}
