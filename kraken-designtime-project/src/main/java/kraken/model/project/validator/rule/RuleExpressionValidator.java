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
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
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
            if(!checkIfParseable(templateExpression, "Error Message Template", rule, scope, session)) {
                return;
            }
            Ast ast = AstBuilder.from(templateExpression, scope);
            ast.getValidationMessages().stream()
                .map(buildValidationMessageFromAstMessage(rule, "Error Message Template"))
                .forEach(session::add);

            for (kraken.el.ast.Expression expression : ast.asTemplate().getTemplateExpressions()) {
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
                checkIfNotEmpty(rule, "Template", expression, session);
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
                .map(buildValidationMessageFromAstMessage(rule, "Assertion Expression"))
                .forEach(session::add);

            if (!Type.BOOLEAN.isAssignableFrom(ast.getExpression().getEvaluationType())) {
                session.add(new ValidationMessage(
                        rule,
                        String.format("Return type of assertion expression must be BOOLEAN, but found: %s", ast.getExpression().getEvaluationType()),
                        Severity.ERROR
                ));
            }
            checkIfNotEmpty(rule, "Assertion", ast.getExpression(), session);
        }
    }

    private void checkIfNotEmpty(Rule rule, String type, kraken.el.ast.Expression e, ValidationSession session) {
        if(e.isEmpty()) {
            session.add(new ValidationMessage(
                rule,
                String.format("%s expression is logically empty. "
                    + "Please check if there are unintentional spaces, new lines or comments remaining.", type),
                Severity.ERROR
            ));
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
                .map(buildValidationMessageFromAstMessage(rule, "Default Expression"))
                .forEach(session::add);

            Type fieldType = contextDefinition.isStrict()
                ? determineFieldType(rule, scope)
                : Type.ANY;

            Type evaluationType = ast.getExpression().getEvaluationType();
            String targetPath = rule.getTargetPath();

            if (!fieldType.isAssignableFrom(evaluationType)) {
                String commonMessage = "Return type of default expression must be compatible with field type which is %1$s, " +
                    "but expression return type is %2$s. " +
                    "%2$s value will be automatically converted to %1$s value as a %3$s. " +
                    "Automatic conversion should be avoided because it is a lossy operation " +
                    "and the converted value depends on the local locale " +
                    "which may produce inconsistent rule evaluation results.";

                if (Type.DATE.isAssignableFrom(fieldType) && Type.DATETIME.isAssignableFrom(evaluationType)) {
                    String dateFieldSpecificMessage = "date in local locale at that moment in time";
                    session.add(
                        new ValidationMessage(
                            rule,
                            String.format(commonMessage, fieldType, evaluationType, dateFieldSpecificMessage),
                            Severity.WARNING
                        ));
                } else if (Type.DATETIME.isAssignableFrom(fieldType) && Type.DATE.isAssignableFrom(evaluationType)) {
                    String dateTimeFieldSpecificMessage = "moment in time at the start of the day in local locale";
                    session.add(
                        new ValidationMessage(
                            rule,
                            String.format(commonMessage, fieldType, evaluationType, dateTimeFieldSpecificMessage),
                            Severity.WARNING
                        ));
                } else {
                    session.add(new ValidationMessage(
                        rule,
                        String.format("Return type of default expression must be compatible with field type which is %s, " +
                            "but expression return type is %s", fieldType, ast.getExpression().getEvaluationType()),
                        Severity.ERROR
                    ));
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
                .map(buildValidationMessageFromAstMessage(rule, "Condition Expression"))
                .forEach(session::add);

            if (!Type.BOOLEAN.isAssignableFrom(ast.getExpression().getEvaluationType())) {
                session.add(new ValidationMessage(
                        rule,
                        String.format("Return type of condition expression must be BOOLEAN, but found: %s", ast.getExpression().getEvaluationType()),
                        Severity.ERROR
                ));
            }
            checkIfNotEmpty(rule, "Condition", ast.getExpression(), session);

            if (ast.getAstType() == AstType.LITERAL && Boolean.TRUE.equals(ast.getCompiledLiteralValue())) {
                var message = "Redundant literal value 'true' in rule condition expression. "
                    + "An empty condition expression is 'true' by default.";

                session.add(new ValidationMessage(rule, message, Severity.INFO));
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
            session.add(new ValidationMessage(
                rule,
                String.format("%s expression cannot be parsed, because there is an error in expression syntax", type),
                Severity.ERROR
            ));
            return false;
        }
    }

    private Function<AstMessage, ValidationMessage> buildValidationMessageFromAstMessage(Rule rule,
                                                                                         String expressionParent) {

        return astMessage -> {
            String message = decorator.decorate(astMessage, rule);
            switch (astMessage.getSeverity()) {
                case ERROR:
                    return new ValidationMessage(
                        rule,
                        String.format("Error found in %s: %s", expressionParent, message),
                        Severity.ERROR
                    );
                case WARNING:
                    return new ValidationMessage(
                        rule,
                        String.format("Warning about %s: %s", expressionParent, message),
                        Severity.WARNING
                    );
                case INFO:
                    return new ValidationMessage(
                        rule,
                        String.format("Info about %s: %s", expressionParent, message),
                        Severity.INFO
                    );
                default:
                    throw new IllegalArgumentException("Unknown AstSeverity encountered: " + astMessage.getSeverity());
            }
        };
    }
}
