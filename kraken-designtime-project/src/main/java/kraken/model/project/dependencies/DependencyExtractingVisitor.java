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
package kraken.model.project.dependencies;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import kraken.el.ast.Expression;
import kraken.el.ast.Identifier;
import kraken.el.ast.ReferenceValue;
import kraken.el.ast.visitor.AstTraversingVisitor;
import kraken.el.scope.Scope;
import kraken.el.scope.ScopeType;
import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.Type;
import kraken.model.context.ContextDefinition;
import kraken.model.project.KrakenProject;

/**
 * @author mulevicius
 */
class DependencyExtractingVisitor extends AstTraversingVisitor {

    private List<FieldDependency> dependencies = new ArrayList<>();

    private Deque<List<Identifier>> referenceIdentifiers = new LinkedList<>();

    private KrakenProject krakenProject;

    private Set<String> externalContextIdentifiers;

    DependencyExtractingVisitor(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
        this.externalContextIdentifiers = getRootExternalContextIdentifiers();
    }

    @Override
    public Expression visit(ReferenceValue reference) {
        referenceIdentifiers.push(new ArrayList<>());
        Expression e = super.visit(reference);
        List<Identifier> identifiers = referenceIdentifiers.pop();

        if (identifiers.isEmpty() || externalContextIdentifiers.contains(identifiers.get(0).getIdentifier())) {
            return e;
        }

        dependencies.addAll(resolveFieldDependencies(identifiers));
        dependencies.addAll(resolveContextDependencies(identifiers, e));

        return e;
    }

    @Override
    public Expression visit(Identifier identifier) {
        referenceIdentifiers.peek().add(identifier);

        return super.visit(identifier);
    }

    private List<FieldDependency> resolveContextDependencies(List<Identifier> identifiers, Expression expression) {
        List<FieldDependency> dependencies = new ArrayList<>();
        String contextName = identifiers.get(0).getIdentifier();
        if(identifiers.size() == 1) {
            if(isReferenceInLocalScope(expression.getScope(), contextName)) {
                resolveContextDependency(expression.getScope().getType().getName(), contextName).ifPresent(dependencies::add);
            } else {
                resolveContextDependency(contextName).ifPresent(dependencies::add);
            }
        } else {
            resolveContextDependency(contextName, identifiers.get(1).getIdentifier()).ifPresent(dependencies::add);
        }
        return dependencies;
    }

    List<FieldDependency> getDependencies() {
        return dependencies;
    }

    private List<FieldDependency> resolveFieldDependencies(List<Identifier> identifiers) {
        List<FieldDependency> dependencies = new ArrayList<>();
        Identifier lastIdentifier = identifiers.get(identifiers.size() - 1);
        if(lastIdentifier.getEvaluationType().isPrimitive() && identifiersExistAsContextChildren(identifiers)) {
            Scope identifierScope = getIdentifierScope(lastIdentifier);
            String fieldType = identifierScope.getType().getName();
            if (krakenProject.getContextDefinitions().containsKey(fieldType)) {
                resolveFieldDependency(fieldType, lastIdentifier.getIdentifier()).ifPresent(dependencies::add);
            }
        }
        return dependencies;
    }

    private Set<String> getRootExternalContextIdentifiers() {
        if (krakenProject.getExternalContext() != null) {
            return Stream.concat(
                    krakenProject.getExternalContext().getContexts().keySet().stream(),
                    krakenProject.getExternalContext().getExternalContextDefinitions().keySet().stream())
                    .collect(Collectors.toSet());
        }

        return Set.of("context");
    }

    private Scope getIdentifierScope(Identifier identifier) {
        Scope identifierScope = identifier.getScope();
        while(identifierScope.getParentScope() != null
                && !identifierScope.isReferenceInCurrentScope(identifier.getIdentifier())) {
            identifierScope = identifierScope.getParentScope();
        }
        return identifierScope;
    }

    private static boolean isReferenceInLocalScope(final Scope scope, final String name) {
        return ScopeType.LOCAL == scope.getScopeType()
            && scope.isReferenceInCurrentScope(name)
            || Optional.ofNullable(scope.getParentScope())
            .map(s -> isReferenceInLocalScope(s, name))
            .orElse(false);
    }


    private boolean identifiersExistAsContextChildren(List<Identifier> identifiers){
        int identifiersSize = identifiers.size() > 0 ? identifiers.size() - 1 : 0;
        for (int index = identifiersSize; index > 0; index--) {
            Identifier identifier = identifiers.get(index);
            if(identifier.getEvaluationType().isPrimitive()){
                continue;
            }

            String typeName = identifier.getScope().getType().getName();
            ContextDefinition contextDefinition = krakenProject.getContextProjection(typeName);

            if(!contextDefinition.getChildren().containsKey(identifierTypeName(identifier))) {
                return false;
            }
        }
        return true;
    }

    private Optional<FieldDependency> resolveDependency(String contextDefinitionName, String contextFieldName, boolean isContextDependency) {
        return Optional.ofNullable(krakenProject.getContextDefinitions().get(contextDefinitionName))
                .filter(ContextDefinition::isStrict)
                .map(contextDefinition -> krakenProject.getContextProjection(contextDefinition.getName()).getContextFields().get(contextFieldName))
                .map(contextField -> new FieldDependency(contextDefinitionName, contextField.getName(), isContextDependency));
    }

    private Optional<FieldDependency> resolveFieldDependency(String contextDefinitionName, String contextFieldName) {
        return resolveDependency(contextDefinitionName, contextFieldName, false);
    }

    private Optional<FieldDependency> resolveContextDependency(String contextDefinitionName, String contextFieldName) {
        return resolveDependency(contextDefinitionName, contextFieldName, true);
    }

    private Optional<FieldDependency> resolveContextDependency(String contextDefinitionName) {
        return Optional.ofNullable(krakenProject.getContextDefinitions().get(contextDefinitionName))
                .filter(ContextDefinition::isStrict)
                .map(contextDefinition -> new FieldDependency(contextDefinitionName, null, true));
    }

    private String identifierTypeName(Identifier identifier){
        Type identifierType = identifier.getEvaluationType();
        if(identifierType instanceof ArrayType) {
            return ((ArrayType) identifierType).getElementType().getName();
        } else {
            return identifierType.getName();
        }
    }
}
