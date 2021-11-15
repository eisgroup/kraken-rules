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
package kraken.model.project.validator.rule;

import static kraken.el.ast.Template.asTemplateExpression;

import java.util.function.Function;

import kraken.el.ast.Ast;
import kraken.el.ast.Template;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.ast.validation.AstError;
import kraken.el.scope.Scope;
import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.Type;
import kraken.model.Expression;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.project.KrakenProject;
import kraken.model.project.scope.ScopeBuilder;
import kraken.model.project.scope.ScopeBuilderProvider;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;
import kraken.model.validation.AssertionPayload;
import kraken.model.validation.ValidationPayload;

/**
 * Class that validates {@link Expression} references defined in {@link Rule}s
 * Validations:
 * - Checks {@link Rule#getContext()} -> {@link ContextDefinition#getContextFields()} contains token
 * - Checks {@link Rule#getContext()} -> {@link ContextDefinition#getParentDefinitions()} ->
 * {@link ContextDefinition#getContextFields()} contains token
 *
 * @author psurinin
 * @author avasiliauskas
 * @since 1.0
 */
public final class RuleExpressionValidator {

    private final KrakenProject krakenProject;

    private final ScopeBuilder scopeBuilder;

    public RuleExpressionValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;

        this.scopeBuilder = ScopeBuilderProvider.forProject(krakenProject);
    }

    public void validate(Rule rule, ValidationSession session) {
        ContextDefinition contextDefinition = krakenProject.getContextDefinitions().get(rule.getContext());

        Scope scope = scopeBuilder.buildScope(contextDefinition);

        findErrorsInCondition(rule, scope, session);
        findErrorsInAssertion(rule, scope, session);
        findErrorsInDefault(rule, contextDefinition, scope, session);
        findErrorsInTemplate(rule, scope, session);
    }

    private void findErrorsInTemplate(Rule rule, Scope scope, ValidationSession session) {
        if (rule.getPayload() instanceof ValidationPayload
            && ((ValidationPayload) rule.getPayload()).getErrorMessage() != null
            && ((ValidationPayload) rule.getPayload()).getErrorMessage().getErrorMessage() != null) {

            String template = ((ValidationPayload) rule.getPayload()).getErrorMessage().getErrorMessage();
            Ast ast = AstBuilder.from(asTemplateExpression(template), scope);
            ast.getSyntaxErrors().stream()
                .map(buildErrorMessageFromSyntaxError(rule, "Error Message Template"))
                .forEach(session::add);

            for (kraken.el.ast.Expression expression : ((Template) ast.getExpression()).getTemplateExpressions()) {
                if (!canFormatAsString(expression.getEvaluationType())) {
                    session.add(new ValidationMessage(
                        rule,
                        String.format(
                            "Return type of expression '%s' in validation message template must be primitive "
                                + "or array of primitives, but found: %s",
                            expression.getToken().getText(),
                            expression.getEvaluationType()
                        ),
                        Severity.ERROR
                    ));
                }
            }
        }
    }

    private boolean canFormatAsString(Type type) {
        return type == Type.ANY
            || type.isPrimitive()
            || type instanceof ArrayType && canFormatAsString(((ArrayType) type).getElementType());
    }

    private void findErrorsInAssertion(Rule rule, Scope scope, ValidationSession session) {
        if (rule.getPayload() instanceof AssertionPayload && ((AssertionPayload) rule.getPayload()).getAssertionExpression() != null) {
            String assertionExpression = ((AssertionPayload) rule.getPayload()).getAssertionExpression().getExpressionString();
            Ast ast = AstBuilder.from(assertionExpression, scope);
            ast.getSyntaxErrors().stream()
                .map(buildErrorMessageFromSyntaxError(rule, "Assertion Expression"))
                .forEach(session::add);

            if (!Type.BOOLEAN.isAssignableFrom(ast.getExpression().getEvaluationType())) {
                session.add(new ValidationMessage(
                        rule,
                        String.format("Return type of assertion expression must be BOOLEAN, but found: %s", ast.getExpression().getEvaluationType()),
                        Severity.ERROR
                ));
            }
        }
    }

    private void findErrorsInDefault(Rule rule, ContextDefinition contextDefinition, Scope scope, ValidationSession session) {
        if (rule.getPayload() instanceof DefaultValuePayload && ((DefaultValuePayload) rule.getPayload()).getValueExpression() != null) {
            String defaultExpression = ((DefaultValuePayload) rule.getPayload()).getValueExpression().getExpressionString();
            Ast ast = AstBuilder.from(defaultExpression, scope);
            ast.getSyntaxErrors().stream()
                .map(buildErrorMessageFromSyntaxError(rule, "Default Expression"))
                .forEach(session::add);

            Type fieldType = contextDefinition.isStrict()
                ? determineFieldType(rule, scope)
                : Type.ANY;

            if (!typesAreCompatible(fieldType, ast.getExpression().getEvaluationType())) {
                session.add(new ValidationMessage(
                        rule,
                        String.format("Return type of default expression must be compatible with field type which is %s, " +
                                "but expression return type is %s", fieldType, ast.getExpression().getEvaluationType()),
                        Severity.ERROR
                ));
            }
        }
    }

    private boolean typesAreCompatible(Type fieldType, Type evaluationType) {
        return fieldType.isAssignableFrom(evaluationType) || canBeCoerced(fieldType, evaluationType);
    }

    private boolean canBeCoerced(Type fieldType, Type evaluationType) {
        return Type.DATE.isAssignableFrom(fieldType) && Type.DATETIME.isAssignableFrom(evaluationType)
                || Type.DATETIME.isAssignableFrom(fieldType) && Type.DATE.isAssignableFrom(evaluationType);
    }

    private Type determineFieldType(Rule rule, Scope scope) {
        Type contextType = scope.resolveTypeOf(rule.getContext());
        if(contextType == Type.ANY) {
            return Type.ANY;
        }
        if(!contextType.getProperties().getReferences().containsKey(rule.getTargetPath())) {
            return Type.UNKNOWN;
        }
        return contextType.getProperties().getReferences().get(rule.getTargetPath()).getType();
    }

    private void findErrorsInCondition(Rule rule, Scope scope, ValidationSession session) {
        if (rule.getCondition() != null && rule.getCondition().getExpression() != null) {
            String conditionExpression = rule.getCondition().getExpression().getExpressionString();
            Ast ast = AstBuilder.from(conditionExpression, scope);
            ast.getSyntaxErrors().stream()
                .map(buildErrorMessageFromSyntaxError(rule, "Condition Expression"))
                .forEach(session::add);

            if (!Type.BOOLEAN.isAssignableFrom(ast.getExpression().getEvaluationType())) {
                session.add(new ValidationMessage(
                        rule,
                        String.format("Return type of condition expression must be BOOLEAN, but found: %s", ast.getExpression().getEvaluationType()),
                        Severity.ERROR
                ));
            }
        }
    }

    private Function<AstError, ValidationMessage> buildErrorMessageFromSyntaxError(Rule rule, String expressionParent) {
        return astError -> new ValidationMessage(
                rule,
                String.format("Error found in %s: %s", expressionParent, astError),
                Severity.ERROR
        );
    }

}