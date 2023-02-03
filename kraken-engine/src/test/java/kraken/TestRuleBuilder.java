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

package kraken;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import kraken.dimensions.DimensionSet;
import kraken.el.Expression;
import kraken.el.ast.Ast;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.scope.Scope;
import kraken.model.derive.DefaultingType;
import kraken.model.validation.UsageType;
import kraken.model.validation.ValidationSeverity;
import kraken.runtime.model.expression.CompiledExpression;
import kraken.runtime.model.expression.ExpressionType;
import kraken.runtime.model.rule.Dependency;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.Payload;
import kraken.runtime.model.rule.payload.derive.DefaultValuePayload;
import kraken.runtime.model.rule.payload.ui.VisibilityPayload;
import kraken.runtime.model.rule.payload.validation.AssertionPayload;
import kraken.runtime.model.rule.payload.validation.ErrorMessage;
import kraken.runtime.model.rule.payload.validation.LengthPayload;
import kraken.runtime.model.rule.payload.validation.RegExpPayload;
import kraken.runtime.model.rule.payload.validation.UsagePayload;

/**
 * @author psurinin@eisgroup.com
 * @since 1.1.0
 */
public class TestRuleBuilder {

    private Payload payload;
    private String targetPath;
    private List<Dependency> dependencies;
    private String name;
    private DimensionSet dimensions;

    public static TestRuleBuilder getInstance() {
        return new TestRuleBuilder();
    }

    public RuntimeRule build() {
        return new RuntimeRule(
                name,
                null,
                targetPath,
                null,
                payload,
                dependencies,
                dimensions,
                null,
                null
        );
    }

    public TestRuleBuilder targetPath(String targetPath) {
        this.targetPath = targetPath;
        return this;
    }

    public TestRuleBuilder payload(Payload payload) {
        this.payload = payload;
        return this;
    }

    public TestRuleBuilder assertionPayload(String expression) {
        return assertionPayload(expression, null);
    }

    public TestRuleBuilder assertionPayload(String expression, List<String> templateParts, List<String> templateExpressions) {
        Expression e = expression(expression);
        this.payload = new AssertionPayload(
            new ErrorMessage(
                "code",
                templateParts,
                templateExpressions.stream()
                    .map(tExp -> {
                        Expression te = expression(tExp);
                        return new CompiledExpression(te.getExpression(), ExpressionType.COMPLEX, null, null, List.of(), te.getAst());
                    })
                    .collect(Collectors.toList())
            ),
            ValidationSeverity.critical,
            false,
            null,
            new CompiledExpression(e.getExpression(), ExpressionType.COMPLEX, null, null, List.of(), e.getAst())
        );
        return this;
    }

    public TestRuleBuilder assertionPayload(String expression, String errorMessage) {
        return assertionPayload(expression, errorMessage == null ? List.of() : List.of(errorMessage), List.of());
    }

    public TestRuleBuilder defaultPayload(String expression, DefaultingType defaultingType) {
        Expression e = expression(expression);
        this.payload = new DefaultValuePayload(
                new CompiledExpression(e.getExpression(), ExpressionType.COMPLEX, null, null, List.of(), e.getAst()),
                defaultingType == null ? DefaultingType.defaultValue : defaultingType
        );
        return this;
    }

    public TestRuleBuilder lengthPayload(int length) {
        return lengthPayload(length, null);
    }

    public TestRuleBuilder lengthPayload(int length, String errorMessage) {
        this.payload = new LengthPayload(
                new ErrorMessage("code", errorMessage == null ? List.of() : List.of(errorMessage), List.of()),
                ValidationSeverity.critical,
                false,
                null,
                length
        );
        return this;
    }

    public TestRuleBuilder regexpPayload(String regexp) {
        return regexpPayload(regexp, null);
    }

    public TestRuleBuilder regexpPayload(String regexp, String errorMessage) {
        this.payload = new RegExpPayload(
                new ErrorMessage("code", errorMessage == null ? List.of() : List.of(errorMessage), List.of()),
                ValidationSeverity.critical,
                false,
                null,
                regexp);
        return this;
    }

    public TestRuleBuilder usagePayload(UsageType usageType) {
        return usagePayload(usageType, null);
    }

    public TestRuleBuilder usagePayload(UsageType usageType, String errorMessage) {
        this.payload = new UsagePayload(
                new ErrorMessage("code", errorMessage == null ? List.of() : List.of(errorMessage), List.of()),
                ValidationSeverity.critical,
                false,
                null,
                usageType
        );
        return this;
    }

    public TestRuleBuilder notVisible() {
        this.payload = new VisibilityPayload(false);
        return this;
    }

    public TestRuleBuilder addDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public TestRuleBuilder name(String name) {
        this.name = name;
        return this;
    }

    public TestRuleBuilder addDimensionSet(DimensionSet dimensionSet) {
        this.dimensions = dimensions;
        return this;
    }

    public TestRuleBuilder addDimensionSet(Set<String> dimensions) {
        this.dimensions = DimensionSet.createForDimensions(dimensions);
        return this;
    }

    private Expression expression(String expression) {
        Ast ast = AstBuilder.from(expression, Scope.dynamic());
        return new Expression(expression, ast);
    }
}
