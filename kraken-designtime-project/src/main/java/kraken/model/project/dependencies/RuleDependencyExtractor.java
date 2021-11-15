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
import java.util.List;
import java.util.stream.Collectors;

import kraken.el.ast.Template;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.scope.Scope;
import kraken.model.Expression;
import kraken.model.Rule;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.dsl.KrakenDSL;
import kraken.model.project.KrakenProject;
import kraken.model.project.scope.ScopeBuilder;
import kraken.model.project.scope.ScopeBuilderProvider;
import kraken.model.validation.AssertionPayload;
import kraken.model.validation.ValidationPayload;
import kraken.utils.Assertions;

/**
 * Inspects provided instance of {@link Rule} and extracts dependencies from
 *  its expressions. Used to build up dependency map
 *
 * @author rimas
 * @since 1.0
 */
public class RuleDependencyExtractor {

    private ScopeBuilder scopeBuilder;

    private KrakenProject krakenProject;

    public RuleDependencyExtractor(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
        this.scopeBuilder = ScopeBuilderProvider.forProject(krakenProject);
    }

    public Collection<FieldDependency> extractDependencies(Rule rule) {
        Assertions.assertNotNull(rule, "Rule");

        kraken.model.context.ContextDefinition contextDefinition = krakenProject
                .getContextDefinitions().get(rule.getContext());
        Scope scope = scopeBuilder.buildScope(contextDefinition);

        final List<FieldDependency> dependencies = new ArrayList<>();
        if (rule.getCondition() != null) {
            Expression expression = rule.getCondition().getExpression();
            dependencies.addAll(extractDependencies(expression, scope));
        }

        if (rule.getPayload() instanceof AssertionPayload) {
            AssertionPayload payload = (AssertionPayload) rule.getPayload();
            Expression expression = payload.getAssertionExpression();
            dependencies.addAll(extractDependencies(expression, scope));
        }

        if(rule.getPayload() instanceof ValidationPayload) {
            ValidationPayload payload = (ValidationPayload) rule.getPayload();
            if(payload.getErrorMessage() != null && payload.getErrorMessage().getErrorMessage() != null) {
                String errorMessageTemplate = asTemplateExpression(payload.getErrorMessage().getErrorMessage());
                dependencies.addAll(extractDependencies(errorMessageTemplate, scope));
            }
        }

        if (rule.getPayload() instanceof DefaultValuePayload) {
            DefaultValuePayload payload = (DefaultValuePayload) rule.getPayload();
            if(payload.getValueExpression() != null) {
                Expression expression = payload.getValueExpression();
                dependencies.addAll(extractDependencies(expression, scope));
            }
        }

        return dependencies.stream()
                .filter(d -> !sameField(rule, d))
                .distinct()
                .collect(Collectors.toList());
    }

    public Collection<FieldDependency> extractDependencies(Expression expression, Scope scope) {
        return extractDependencies(expression.getExpressionString(), scope);
    }

    private Collection<FieldDependency> extractDependencies(String expression, Scope scope) {
        if(expression == null) {
            return List.of();
        }
        DependencyExtractingVisitor dependencyExtractingVisitor = new DependencyExtractingVisitor(krakenProject);
        dependencyExtractingVisitor.visit(AstBuilder.from(expression, scope).getExpression());
        return dependencyExtractingVisitor.getDependencies();
    }

    private boolean sameField(Rule rule, FieldDependency dependency) {
        if (!rule.getTargetPath().equals(dependency.getPath())) {
            return false;
        }
        final Collection<String> inherited = krakenProject.getContextProjection(rule.getContext()).getParentDefinitions();
        return inherited.contains(dependency.getContextName()) || rule.getContext().equals(dependency.getContextName());
    }
}
