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
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_ASSERTION_RETURN_TYPE_NOT_BOOLEAN;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_CONDITION_REDUNDANT_TRUE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_CONDITION_RETURN_TYPE_NOT_BOOLEAN;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_DEFAULT_RETURN_TYPE_NOT_COMPATIBLE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_EXPRESSION_COERCE_DATETIME_TO_DATE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_EXPRESSION_COERCE_DATE_TO_DATETIME;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_EXPRESSION_IS_LOGICALLY_EMPTY;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_EXPRESSION_IS_NOT_PARSEABLE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_EXPRESSION_SYNTAX_ERROR;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_EXPRESSION_SYNTAX_INFO;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_EXPRESSION_SYNTAX_WARNING;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_TEMPLATE_RETURN_TYPE_NOT_PRIMITIVE;

import kraken.el.Expression;
import kraken.el.ast.Ast;
import kraken.el.ast.AstType;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.ast.builder.AstBuildingException;
import kraken.el.ast.validation.AstMessage;
import kraken.el.scope.Scope;
import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.Type;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.project.KrakenProject;
import kraken.model.project.ccr.CrossContextServiceProvider;
import kraken.model.project.scope.ScopeBuilder;
import kraken.model.project.scope.ScopeBuilderProvider;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.model.project.validator.ValidationSession;
import kraken.model.project.validator.rule.message.AstMessageDecoratorService;
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
public final class RuleExpressionValidator implements RuleValidator {

    private final KrakenProject krakenProject;

    private final ScopeBuilder scopeBuilder;
    private final AstMessageDecoratorService decorator;

    public RuleExpressionValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
        this.scopeBuilder = ScopeBuilderProvider.forProject(krakenProject);
        this.decorator = new AstMessageDecoratorService(
            krakenProject,
            CrossContextServiceProvider.forProject(krakenProject)
        );
    }

    @Override
    public void validate(Rule rule, ValidationSession session) {
        ContextDefinition contextDefinition = krakenProject.getContextDefinitions().get(rule.getContext());

        Scope scope = scopeBuilder.buildScope(contextDefinition);

        validateConditionExpression(rule, scope, session);
        validateAssertionExpression(rule, scope, session);
        validateDefaultExpression(rule, contextDefinition, scope, session);
        validateMessageTemplateExpression(rule, scope, session);
    }

    @Override
    public boolean canValidate(Rule rule) {
        return rule.getName() != null
            && rule.getContext() != null
            && krakenProject.getContextDefinitions().containsKey(rule.getContext())
            && rule.getTargetPath() != null
            && krakenProject.getContextProjection(rule.getContext()).getContextFields().containsKey(rule.getTargetPath());
    }

    private void validateMessageTemplateExpression(Rule rule, Scope scope, ValidationSession session) {
        if (rule.getPayload() instanceof ValidationPayload
            && ((ValidationPayload) rule.getPayload()).getErrorMessage() != null
            && ((ValidationPayload) rule.getPayload()).getErrorMessage().getErrorMessage() != null) {

            String template = ((ValidationPayload) rule.getPayload()).getErrorMessage().getErrorMessage();
            String templateExpression = asTemplateExpression(template);
            if(!checkIfParseable(templateExpression, "Validation message template", rule, scope, session)) {
                return;
            }
            Ast ast = AstBuilder.from(templateExpression, scope);
            ast.getValidationMessages().stream()
                .map(m -> buildValidationMessageFromAstMessage(m, rule, "Validation message template"))
                .forEach(session::add);

            for (kraken.el.ast.Expression expression : ast.asTemplate().getTemplateExpressions()) {
                if (!canFormatAsString(expression.getEvaluationType())) {
                    var m = ValidationMessageBuilder.create(RULE_TEMPLATE_RETURN_TYPE_NOT_PRIMITIVE, rule)
                        .parameters(expression.getToken().getText(), expression.getEvaluationType())
                        .build();
                    session.add(m);
                }
                checkIfNotEmpty(rule, "Validation message template", expression, session);
            }
        }
    }

    private boolean canFormatAsString(Type type) {
        return type.isPrimitive() || type.isDynamic()
            || type.unwrapArrayType().isPrimitive() || type.unwrapArrayType().isDynamic();
    }

    private void validateAssertionExpression(Rule rule, Scope scope, ValidationSession session) {
        if (rule.getPayload() instanceof AssertionPayload && ((AssertionPayload) rule.getPayload()).getAssertionExpression() != null) {
            String assertionExpression = ((AssertionPayload) rule.getPayload()).getAssertionExpression().getExpressionString();
            if(!checkIfParseable(assertionExpression, "Assertion", rule, scope, session)) {
                return;
            }
            Ast ast = AstBuilder.from(assertionExpression, scope);
            ast.getValidationMessages().stream()
                .map(m -> buildValidationMessageFromAstMessage(m, rule, "Assertion"))
                .forEach(session::add);

            if (!Type.BOOLEAN.isAssignableFrom(ast.getExpression().getEvaluationType())) {
                session.add(ValidationMessageBuilder.create(RULE_ASSERTION_RETURN_TYPE_NOT_BOOLEAN, rule)
                    .parameters(ast.getExpression().getEvaluationType())
                    .build());
            }
            checkIfNotEmpty(rule, "Assertion", ast.getExpression(), session);
        }
    }

    private void validateDefaultExpression(Rule rule, ContextDefinition contextDefinition, Scope scope, ValidationSession session) {
        if (rule.getPayload() instanceof DefaultValuePayload && ((DefaultValuePayload) rule.getPayload()).getValueExpression() != null) {
            String defaultExpression = ((DefaultValuePayload) rule.getPayload()).getValueExpression().getExpressionString();
            if (!checkIfParseable(defaultExpression, "Default", rule, scope, session)) {
                return;
            }
            Ast ast = AstBuilder.from(defaultExpression, scope);
            ast.getValidationMessages().stream()
                .map(m -> buildValidationMessageFromAstMessage(m, rule, "Default"))
                .forEach(session::add);

            Type fieldType = contextDefinition.isStrict()
                ? determineFieldType(rule, scope)
                : Type.ANY;

            Type evaluationType = ast.getExpression().getEvaluationType();
            if (!fieldType.isAssignableFrom(evaluationType)) {
                if (Type.DATE.isAssignableFrom(fieldType) && Type.DATETIME.isAssignableFrom(evaluationType)) {
                    var m = ValidationMessageBuilder.create(RULE_EXPRESSION_COERCE_DATETIME_TO_DATE, rule)
                        .parameters(fieldType, evaluationType)
                        .build();
                    session.add(m);
                } else if (Type.DATETIME.isAssignableFrom(fieldType) && Type.DATE.isAssignableFrom(evaluationType)) {
                    var m = ValidationMessageBuilder.create(RULE_EXPRESSION_COERCE_DATE_TO_DATETIME, rule)
                        .parameters(fieldType, evaluationType)
                        .build();
                    session.add(m);
                } else {
                    var m = ValidationMessageBuilder.create(RULE_DEFAULT_RETURN_TYPE_NOT_COMPATIBLE, rule)
                        .parameters(fieldType, ast.getExpression().getEvaluationType())
                        .build();
                    session.add(m);
                }
            }
            checkIfNotEmpty(rule, "Default", ast.getExpression(), session);
        }
    }

    private Type determineFieldType(Rule rule, Scope scope) {
        Type contextType = scope.resolveTypeOf(rule.getContext());
        if(contextType.isDynamic()) {
            return Type.ANY;
        }
        if(!contextType.getProperties().getReferences().containsKey(rule.getTargetPath())) {
            return Type.UNKNOWN;
        }
        Type fieldType = contextType.getProperties().getReferences().get(rule.getTargetPath()).getType();
        if(Type.MONEY.equals(fieldType)) {
            return Type.NUMBER;
        }
        if(ArrayType.of(Type.MONEY).equals(fieldType)) {
            return ArrayType.of(Type.NUMBER);
        }
        return fieldType;
    }

    private void validateConditionExpression(Rule rule, Scope scope, ValidationSession session) {
        if (rule.getCondition() != null && rule.getCondition().getExpression() != null) {
            String conditionExpression = rule.getCondition().getExpression().getExpressionString();
            if(!checkIfParseable(conditionExpression, "Condition", rule, scope, session)) {
                return;
            }

            Ast ast = AstBuilder.from(conditionExpression, scope);
            ast.getValidationMessages().stream()
                .map(m -> buildValidationMessageFromAstMessage(m, rule, "Condition"))
                .forEach(session::add);

            if (!Type.BOOLEAN.isAssignableFrom(ast.getExpression().getEvaluationType())) {
                var m = ValidationMessageBuilder.create(RULE_CONDITION_RETURN_TYPE_NOT_BOOLEAN, rule)
                    .parameters(ast.getExpression().getEvaluationType())
                    .build();
                session.add(m);
            }
            checkIfNotEmpty(rule, "Condition", ast.getExpression(), session);

            if (ast.getAstType() == AstType.LITERAL && Boolean.TRUE.equals(ast.getCompiledLiteralValue())) {
                var m = ValidationMessageBuilder.create(RULE_CONDITION_REDUNDANT_TRUE, rule).build();
                session.add(m);
            }
        }
    }

    /**
     *
     * @param expression
     * @param scope
     * @param session
     * @return true if parseable; false otherwise, also adds error message to session
     */
    private boolean checkIfParseable(String expression, String type, Rule rule, Scope scope, ValidationSession session) {
        try {
            AstBuilder.from(expression, scope);
            return true;
        } catch (AstBuildingException e) {
            var m = ValidationMessageBuilder.create(RULE_EXPRESSION_IS_NOT_PARSEABLE, rule)
                .parameters(type)
                .build();
            session.add(m);
            return false;
        }
    }

    private void checkIfNotEmpty(Rule rule, String type, kraken.el.ast.Expression e, ValidationSession session) {
        if(e.isEmpty()) {
            var m = ValidationMessageBuilder.create(RULE_EXPRESSION_IS_LOGICALLY_EMPTY, rule)
                .parameters(type)
                .build();
            session.add(m);
        }
    }

    private ValidationMessage buildValidationMessageFromAstMessage(AstMessage astMessage, Rule rule, String type) {
        String message = decorator.decorate(astMessage, rule);
        switch (astMessage.getSeverity()) {
            case ERROR:
                return ValidationMessageBuilder.create(RULE_EXPRESSION_SYNTAX_ERROR, rule)
                    .parameters(type, astMessage.getNode().getToken(), message)
                    .build();
            case WARNING:
                return ValidationMessageBuilder.create(RULE_EXPRESSION_SYNTAX_WARNING, rule)
                    .parameters(type, astMessage.getNode().getToken(), message)
                    .build();
            case INFO:
                return ValidationMessageBuilder.create(RULE_EXPRESSION_SYNTAX_INFO, rule)
                    .parameters(type, astMessage.getNode().getToken(), message)
                    .build();
            default:
                throw new IllegalArgumentException("Unknown AstSeverity encountered: " + astMessage.getSeverity());
        }
    }
}
