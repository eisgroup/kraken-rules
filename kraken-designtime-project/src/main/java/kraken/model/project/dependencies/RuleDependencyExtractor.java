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
package kraken.model.project.dependencies;

import static kraken.el.ast.Template.asTemplateExpression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kraken.el.ast.Ast;
import kraken.el.ast.Function;
import kraken.el.ast.Identifier;
import kraken.el.ast.ReferenceValue;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.ast.builder.AstBuildingException;
import kraken.el.ast.dependency.Reference;
import kraken.el.ast.dependency.ReferenceResolvingVisitor;
import kraken.el.ast.visitor.AstTraversingVisitor;
import kraken.el.functionregistry.FunctionHeader;
import kraken.el.scope.Scope;
import kraken.el.scope.ScopeType;
import kraken.el.scope.type.Type;
import kraken.model.Expression;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.model.context.PrimitiveFieldDataType;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.project.KrakenProject;
import kraken.model.project.scope.FunctionInvocationKey;
import kraken.model.project.scope.ScopeBuilder;
import kraken.model.project.scope.ScopeBuilderProvider;
import kraken.model.validation.AssertionPayload;
import kraken.model.validation.ValidationPayload;
import kraken.utils.Assertions;

/**
 * Inspects provided instance of {@link Rule} and resolves dependencies from its expressions.
 * Used to build dependency map.
 *
 * @author rimas
 * @since 1.0
 */
public class RuleDependencyExtractor {

    private final static Logger logger = LoggerFactory.getLogger(RuleDependencyExtractor.class);

    private final ScopeBuilder scopeBuilder;

    private final KrakenProject krakenProject;

    private final Map<FunctionHeader, kraken.model.Function> functions;

    public RuleDependencyExtractor(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
        this.scopeBuilder = ScopeBuilderProvider.forProject(krakenProject);

        this.functions = krakenProject.getFunctions().stream()
            .collect(Collectors.toMap(f -> new FunctionHeader(f.getName(), f.getParameters().size()), f -> f));
    }

    /**
     * Inspects provided instance of {@link Rule} and resolves dependencies from its expressions.
     * Dependency on a field that the rule is applied to is not returned.
     * Only unique dependencies are returned.
     *
     * @param rule
     * @return a list of all dependencies
     */
    public Collection<FieldDependency> extractDependencies(Rule rule) {
        Assertions.assertNotNull(rule, "Rule");

        ContextDefinition contextDefinition = krakenProject.getContextDefinitions().get(rule.getContext());
        Scope scope = scopeBuilder.buildScope(contextDefinition);

        Collection<FieldDependency> dependencies = new LinkedHashSet<>();
        if (rule.getCondition() != null) {
            Expression expression = rule.getCondition().getExpression();
            dependencies.addAll(resolve(expression.getExpressionString(), scope, rule));
        }

        if (rule.getPayload() instanceof AssertionPayload) {
            AssertionPayload payload = (AssertionPayload) rule.getPayload();
            Expression expression = payload.getAssertionExpression();
            dependencies.addAll(resolve(expression.getExpressionString(), scope, rule));
        }

        if(rule.getPayload() instanceof ValidationPayload) {
            ValidationPayload payload = (ValidationPayload) rule.getPayload();
            if(payload.getErrorMessage() != null && payload.getErrorMessage().getErrorMessage() != null) {
                String errorMessageTemplate = asTemplateExpression(payload.getErrorMessage().getErrorMessage());
                dependencies.addAll(resolve(errorMessageTemplate, scope, rule));
            }
        }

        if (rule.getPayload() instanceof DefaultValuePayload) {
            DefaultValuePayload payload = (DefaultValuePayload) rule.getPayload();
            if(payload.getValueExpression() != null) {
                Expression expression = payload.getValueExpression();
                dependencies.addAll(resolve(expression.getExpressionString(), scope, rule));
            }
        }
        return dependencies;
    }

    public Collection<FieldDependency> extractDependencies(Expression expression, Rule rule, Scope scope) {
        return resolve(expression.getExpressionString(), scope, rule);
    }

    private Collection<FieldDependency> resolve(String expression, Scope scope, Rule rule) {
        if(expression == null) {
            return List.of();
        }
        try {
            Ast ast = AstBuilder.from(expression, scope);

            Set<String> targetInheritedContexts = collectTargetInheritedContexts(rule);

            DependencyTypeResolvingVisitor dependencyTypeResolvingVisitor =
                new DependencyTypeResolvingVisitor(krakenProject, targetInheritedContexts);
            dependencyTypeResolvingVisitor.visit(ast.getExpression());
            Set<String> ccrDependencies = dependencyTypeResolvingVisitor.getCcrDependencies();
            Set<String> selfDependencies = dependencyTypeResolvingVisitor.getSelfDependencies();

            var referenceResolvingVisitor = new ReferenceResolvingKrakenVisitor(scope, scopeBuilder, functions);
            referenceResolvingVisitor.visit(ast.getExpression());

            List<FieldDependency> dependencies = new ArrayList<>();
            for(Reference reference : referenceResolvingVisitor.getReferences()) {
                String contextName;
                String fieldName;
                if(reference.isGlobal()) {
                    contextName = reference.getReferenceName();
                    fieldName = null;
                } else {
                    contextName = reference.getTypeName();
                    fieldName = reference.getReferenceName();
                }

                // Target contexts contain target context name that the rule is applied on and also inherited context
                // names. Dependencies to target field or target context (with no field) are not collected.
                if(targetInheritedContexts.contains(contextName)
                    && (fieldName == null || fieldName.equals(rule.getTargetPath()))) {
                    continue;
                }

                if(isValidKrakenDependency(contextName, fieldName)) {
                    boolean ccrDependency = ccrDependencies.contains(toReferenceString(contextName, fieldName));
                    boolean selfDependency = selfDependencies.contains(toReferenceString(contextName, fieldName));
                    var dependency = new FieldDependency(contextName, fieldName, ccrDependency, selfDependency);
                    dependencies.add(dependency);
                }
            }
            return dependencies;
        } catch (AstBuildingException e) {
            logger.trace("Cannot extract dependencies because expression is not parseable: {}.", expression, e);
            return List.of();
        }
    }

    private boolean isValidKrakenDependency(String contextName, String fieldName) {
        // dependency must be a valid kraken context definition
        if(!krakenProject.getContextDefinitions().containsKey(contextName)) {
            return false;
        }
        return fieldName == null || isFieldKrakenPrimitive(contextName, fieldName);
    }

    private boolean isFieldKrakenPrimitive(String contextName, String fieldName) {
        ContextDefinition contextDefinition = krakenProject.getContextProjection(contextName);
        ContextField contextField = contextDefinition.getContextFields().get(fieldName);
        // dependency must be a valid primitive kraken field
        return contextField != null && PrimitiveFieldDataType.isPrimitiveType(contextField.getFieldType());
    }

    private Set<String> collectTargetInheritedContexts(Rule rule) {
        Set<String> targetContexts = new HashSet<>(
            krakenProject.getContextProjection(rule.getContext()).getParentDefinitions()
        );
        targetContexts.add(rule.getContext());
        return targetContexts;
    }

    private static class ReferenceResolvingKrakenVisitor extends ReferenceResolvingVisitor {

        private final ScopeBuilder scopeBuilder;

        private final Map<FunctionHeader, kraken.model.Function> functions;

        private final Set<FunctionInvocationKey> traversedFunctionInvocations;

        public ReferenceResolvingKrakenVisitor(Scope scope,
                                               ScopeBuilder scopeBuilder,
                                               Map<FunctionHeader, kraken.model.Function> functions) {
            this(scope, scopeBuilder, functions, new HashSet<>());
        }

        private ReferenceResolvingKrakenVisitor(Scope scope,
                                                ScopeBuilder scopeBuilder,
                                                Map<FunctionHeader, kraken.model.Function> functions,
                                                Set<FunctionInvocationKey> traversedFunctionInvocations) {
            super(scope);
            this.scopeBuilder = scopeBuilder;
            this.functions = functions;

            this.traversedFunctionInvocations = traversedFunctionInvocations;
        }

        @Override
        public kraken.el.ast.Expression visit(Function function) {
            FunctionHeader header = new FunctionHeader(function.getFunctionName(), function.getParameters().size());
            if(functions.containsKey(header)) {
                kraken.model.Function f = functions.get(header);

                Map<String, Type> parameterTypes = new HashMap<>();
                for(int i = 0; i < function.getParameters().size(); i++) {
                    String parameterName = f.getParameters().get(i).getName();
                    Type parameterType = function.getParameters().get(i).getEvaluationType();
                    parameterTypes.put(parameterName, parameterType);
                }

                FunctionInvocationKey functionInvocationKey = new FunctionInvocationKey(f, parameterTypes);
                if(!traversedFunctionInvocations.contains(functionInvocationKey)) {
                    traversedFunctionInvocations.add(functionInvocationKey);

                    Scope functionScope = scopeBuilder.buildFunctionScope(f, parameterTypes);
                    Ast functionAst = AstBuilder.from(f.getBody().getExpressionString(), functionScope);
                    var functionVisitor = new ReferenceResolvingKrakenVisitor(
                        functionScope,
                        scopeBuilder,
                        functions,
                        traversedFunctionInvocations
                    );
                    functionVisitor.visit(functionAst.getExpression());

                    this.references.addAll(functionVisitor.getReferences());
                }
            }
            return super.visit(function);
        }
    }

    private static class DependencyTypeResolvingVisitor extends AstTraversingVisitor {

        private final Deque<List<Identifier>> referenceIdentifiers = new LinkedList<>();

        private final Set<String> ccrDependencies = new LinkedHashSet<>();

        private final Set<String> selfDependencies = new LinkedHashSet<>();

        private final KrakenProject krakenProject;

        private final Set<String> targetInheritedContexts;

        public DependencyTypeResolvingVisitor(KrakenProject krakenProject, Set<String> targetInheritedContexts) {
            this.krakenProject = krakenProject;
            this.targetInheritedContexts = targetInheritedContexts;
        }

        @Override
        public kraken.el.ast.Expression visit(ReferenceValue reference) {
            referenceIdentifiers.push(new ArrayList<>());
            kraken.el.ast.Expression e = super.visit(reference);
            List<Identifier> identifiers = referenceIdentifiers.pop();

            if(identifiers.isEmpty()) {
                return e;
            }

            Identifier firstIdentifier = identifiers.get(0);
            if(firstIdentifier.isReferenceInGlobalScope()) {
                String contextName = firstIdentifier.getIdentifierToken();
                addDependency(contextName, null);
                if (identifiers.size() >= 2) {
                    String fieldName = identifiers.get(1).getIdentifierToken();
                    addDependency(contextName, fieldName);
                }
            } else if(firstIdentifier.findScopeTypeOfReference() == ScopeType.LOCAL) {
                String contextName = firstIdentifier.getScope()
                    .findClosestScopeOfType(ScopeType.LOCAL).getType().getName();
                String fieldName = firstIdentifier.getIdentifierToken();
                addDependency(contextName, fieldName);
            }
            return e;
        }

        @Override
        public kraken.el.ast.Expression visit(Identifier identifier) {
            this.referenceIdentifiers.peek().add(identifier);

            return super.visit(identifier);
        }

        private void addDependency(String contextName, @Nullable String fieldName) {
            ContextDefinition contextDefinition = krakenProject.getContextProjection(contextName);
            if(contextDefinition != null
                && (fieldName == null || contextDefinition.getContextFields().containsKey(fieldName))) {
                if(targetInheritedContexts.contains(contextName)) {
                    selfDependencies.add(toReferenceString(contextName, fieldName));
                } else {
                    ccrDependencies.add(toReferenceString(contextName, fieldName));
                }
            }
        }

        public Set<String> getCcrDependencies() {
            return ccrDependencies;
        }

        public Set<String> getSelfDependencies() {
            return selfDependencies;
        }

    }

    private static String toReferenceString(String contextName, @Nullable String fieldName) {
        return fieldName == null
            ? contextName
            : contextName + "." + fieldName;
    }
}
