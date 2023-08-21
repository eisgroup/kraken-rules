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

import static kraken.model.context.Cardinality.MULTIPLE;
import static kraken.model.context.Cardinality.SINGLE;
import static kraken.model.context.PrimitiveFieldDataType.BOOLEAN;
import static kraken.model.context.PrimitiveFieldDataType.DATETIME;
import static kraken.model.context.PrimitiveFieldDataType.DATE;
import static kraken.model.context.PrimitiveFieldDataType.DECIMAL;
import static kraken.model.context.PrimitiveFieldDataType.INTEGER;
import static kraken.model.context.PrimitiveFieldDataType.MONEY;
import static kraken.model.context.PrimitiveFieldDataType.STRING;
import static kraken.model.project.KrakenProjectMocks.attribute;
import static kraken.model.project.KrakenProjectMocks.contextDefinition;
import static kraken.model.project.KrakenProjectMocks.entryPoints;
import static kraken.model.project.KrakenProjectMocks.field;
import static kraken.model.project.KrakenProjectMocks.functionSignature;
import static kraken.model.project.KrakenProjectMocks.krakenProject;
import static kraken.model.project.KrakenProjectMocks.rule;
import static kraken.model.project.KrakenProjectMocks.type;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import kraken.model.Condition;
import kraken.model.ErrorMessage;
import kraken.model.Expression;
import kraken.model.FunctionSignature;
import kraken.model.Payload;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.derive.DefaultingType;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.project.KrakenProjectMocks;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;
import kraken.model.validation.AssertionPayload;

public class RuleExpressionValidatorTest {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    @Test
    public void shouldBeValidAssertionWithCondition() {
        Rule rule = rule("R1", "PartyContext", "surname");
        rule.setCondition(conditionOf("name != null"));
        Payload assertionPayload = factory.createAssertionPayload();
        ((AssertionPayload) assertionPayload).setAssertionExpression(expressionOf("age >= 21"));
        rule.setPayload(assertionPayload);

        ContextDefinition contextDefinition = contextDefinition("PartyContext", List.of(
            field("surname"),
            field("name"),
            field("age", DECIMAL)
        ));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, empty());
    }

    @Test
    public void shouldBeValidDefaultValueLiteralExpression() {
        Rule rule = rule("R3", "PartyContext", "age");
        Payload defaultValuePayload = factory.createDefaultValuePayload();
        ((DefaultValuePayload) defaultValuePayload).setValueExpression(expressionOf("21"));
        rule.setPayload(defaultValuePayload);

        ContextDefinition contextDefinition = contextDefinition("PartyContext", List.of(
            field("age", DECIMAL)
        ));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, empty());
    }

    @Test
    public void shouldContainInvalidConditionalExpressionError() {
        Rule rule = rule("R3", "PartyContext", "surname");
        rule.setCondition(conditionOf("notExistingField != null"));

        ContextDefinition contextDefinition = contextDefinition("PartyContext", List.of(field("surname")));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), List.of(rule));

        List<String> validationMessages = validate(rule, krakenProject).stream()
                .map(ValidationMessage::getMessage)
                .collect(Collectors.toList());

        assertThat(validationMessages, hasSize(1));
        assertThat(validationMessages,
            contains("Condition expression has error in 'notExistingField'. Reference 'notExistingField' not found."));
    }

    @Test
    public void shouldContainInvalidTemplateExpressionError() {
        Rule rule = rule("R3", "PartyContext", "surname");
        Payload payload = factory.createAssertionPayload();
        ErrorMessage errorMessage = factory.createErrorMessage();
        errorMessage.setErrorCode("CODE");
        errorMessage.setErrorMessage("My name is: ${notExistingField}");
        ((AssertionPayload) payload).setAssertionExpression(expressionOf("true"));
        ((AssertionPayload) payload).setErrorMessage(errorMessage);
        rule.setPayload(payload);

        ContextDefinition contextDefinition = contextDefinition("PartyContext", List.of(field("surname")));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), List.of(rule));

        List<String> validationMessages = validate(rule, krakenProject).stream()
            .map(ValidationMessage::getMessage)
            .collect(Collectors.toList());

        assertThat(validationMessages, hasSize(2));
        assertThat(validationMessages, contains(
            "Validation message template expression has error in 'notExistingField'. "
                + "Reference 'notExistingField' not found.",
            "Return type of expression 'notExistingField' in validation message template "
                + "must be primitive or array of primitives, but found: Unknown."
        ));
    }

    @Test
    public void shouldContainInvalidTemplateExpressionErrorForIncompatibleType() {
        Rule rule = rule("R3", "PartyContext", "surname");
        Payload payload = factory.createAssertionPayload();
        ErrorMessage errorMessage = factory.createErrorMessage();
        errorMessage.setErrorCode("CODE");
        errorMessage.setErrorMessage("My name is: ${PartyContext}");
        ((AssertionPayload) payload).setAssertionExpression(expressionOf("true"));
        ((AssertionPayload) payload).setErrorMessage(errorMessage);
        rule.setPayload(payload);

        ContextDefinition contextDefinition = contextDefinition("PartyContext", List.of(field("surname")));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), List.of(rule));

        List<String> validationMessages = validate(rule, krakenProject).stream()
            .map(ValidationMessage::getMessage)
            .collect(Collectors.toList());

        assertThat(validationMessages, hasSize(1));
        assertThat(validationMessages,
            contains("Return type of expression 'PartyContext' in validation message template must be primitive "
                + "or array of primitives, but found: PartyContext.")
        );
    }

    @Test
    public void shouldContainInvalidAssertionExpression() {
        Rule rule = rule("R3", "PartyContext", "age");
        Payload payload = factory.createAssertionPayload();
        ((AssertionPayload) payload).setAssertionExpression(expressionOf("notExistingField.nested.path >= 21"));
        rule.setPayload(payload);

        ContextDefinition contextDefinition = contextDefinition("PartyContext", List.of(field("age", INTEGER)));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), List.of(rule));

        List<String> validationMessages = validate(rule, krakenProject).stream()
                .map(ValidationMessage::getMessage)
                .collect(Collectors.toList());

        assertThat(validationMessages, hasSize(2));
        assertThat(validationMessages, containsInAnyOrder(
                "Assertion expression has error in 'notExistingField.nested.path >= 21'. "
                    + "Operation MoreThanOrEquals can only be performed on comparable types, "
                    + "but was performed on 'Unknown' and 'Number'.",
                "Assertion expression has error in 'notExistingField.nested.path'. "
                    + "Reference 'notExistingField' not found."
                )
        );
    }

    @Test
    public void shouldPassValidationWhenInOperatorTypeMatchesArrayType() {
        Rule rule = rule("R3", "PartyContext", "name");
        Payload payload = factory.createAssertionPayload();
        ((AssertionPayload) payload).setAssertionExpression(expressionOf("name in stringArray"));
        rule.setPayload(payload);

        ContextField field = field("stringArray", STRING);
        field.setCardinality(MULTIPLE);
        ContextDefinition contextDefinition = contextDefinition("PartyContext", List.of(field("name"), field));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, empty());
    }


    @Test
    public void shouldFailValidationWhenInOperatorTypeNotMatchArrayType() {
        Rule rule = rule("R3", "PartyContext", "age");
        Payload payload = factory.createAssertionPayload();
        ((AssertionPayload) payload).setAssertionExpression(expressionOf("age in stringArray"));
        rule.setPayload(payload);

        ContextField field = field("stringArray", STRING);
        field.setCardinality(MULTIPLE);
        ContextDefinition contextDefinition = contextDefinition("PartyContext", List.of(field("age", INTEGER), field));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, hasSize(1));
    }

    @Test
    public void shouldFailValidationWhenAssertionExpressionHasNonBooleanStaticReturnType() {
        Rule rule = rule("R3", "PartyContext", "age");
        Payload payload = factory.createAssertionPayload();
        ((AssertionPayload) payload).setAssertionExpression(expressionOf("'string'"));
        rule.setPayload(payload);

        ContextDefinition contextDefinition = contextDefinition("PartyContext", List.of(field("age", INTEGER)));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, hasSize(1));
    }

    @Test
    public void shouldFailValidationWhenConditionExpressionHasNonBooleanStaticReturnType() {
        Rule rule = rule("R3", "PartyContext", "age");
        Condition condition = factory.createCondition();
        condition.setExpression(expressionOf("'string'"));
        rule.setCondition(condition);
        Payload payload = factory.createAssertionPayload();
        ((AssertionPayload) payload).setAssertionExpression(expressionOf("true"));
        rule.setPayload(payload);

        ContextDefinition contextDefinition = contextDefinition("PartyContext", List.of(field("age", INTEGER)));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, hasSize(1));
    }

    @Test
    public void shouldContainInvalidDefaultValueExpression() {
        String contextName = "PartyContext";

        Rule rule = rule("R3", contextName, "booleanField");
        Payload payload = factory.createDefaultValuePayload();
        ((DefaultValuePayload) payload).setValueExpression(expressionOf("notValidExpression > valid"));
        rule.setPayload(payload);

        ContextDefinition contextDefinition = contextDefinition(contextName, List.of(field("booleanField", BOOLEAN)));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), List.of(rule));

        List<String> validationMessages = validate(rule, krakenProject).stream()
                .map(ValidationMessage::getMessage)
                .collect(Collectors.toList());

        assertThat(validationMessages, hasSize(3));
        assertThat(validationMessages, containsInAnyOrder(
                "Default expression has error in 'notValidExpression > valid'. "
                    + "Operation MoreThan can only be performed on comparable types, "
                    + "but was performed on 'Unknown' and 'Unknown'.",
                "Default expression has error in 'notValidExpression'. Reference 'notValidExpression' not found.",
                "Default expression has error in 'valid'. Reference 'valid' not found."
                )
        );
    }

    @Test
    public void shouldFailValidationWhenDefaultReturnTypeIsNotCompatibleWithField() {
        Rule rule = rule("R", "Policy", "policyCd");
        DefaultValuePayload payload = factory.createDefaultValuePayload();
        payload.setDefaultingType(DefaultingType.defaultValue);
        payload.setValueExpression(expressionOf("true"));
        rule.setPayload(payload);

        ContextDefinition contextDefinition = contextDefinition("Policy", List.of(field("policyCd", STRING)));
        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, hasSize(1));
        assertThat(validationMessages.get(0).getMessage(),
                equalTo("Return type of default expression must be compatible with field type which is String, "
                    + "but expression return type is Boolean.")
        );
    }

    @Test
    public void shouldNotFailValidationWhenDefaultReturnTypeIsArrayAndCompatibleWithField() {
        Rule rule = rule("R", "Policy", "moneys");
        DefaultValuePayload payload = factory.createDefaultValuePayload();
        payload.setDefaultingType(DefaultingType.defaultValue);
        payload.setValueExpression(expressionOf("{10.00, 20.00}"));
        rule.setPayload(payload);

        ContextDefinition contextDefinition = contextDefinition("Policy",
            List.of(field("moneys", "moneys", MONEY.toString(), MULTIPLE)));
        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, empty());
    }

    @Test
    public void shouldThrowWarningWhenDefaultReturnTypeIsDateTimeButExpressionEvaluationTypeIsDate() {
        Rule rule = rule("R", "Policy", "expirationDate");
        DefaultValuePayload payload = factory.createDefaultValuePayload();
        payload.setDefaultingType(DefaultingType.defaultValue);
        payload.setValueExpression(expressionOf("Date(2018, 1, 1)"));
        rule.setPayload(payload);

        ContextDefinition contextDefinition = contextDefinition("Policy", List.of(field("expirationDate", DATETIME)));
        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, hasSize(1));
        assertThat(validationMessages.get(0).getSeverity(), is(Severity.WARNING));
        assertThat(validationMessages.get(0).getMessage(),
            equalTo("Return type of default expression must be compatible with field type which is DateTime, " +
                "but expression return type is Date. " +
                "Date value will be automatically converted to DateTime value as a moment in time at the start of the day in local locale. " +
                "Automatic conversion should be avoided because it is a lossy operation " +
                "and the converted value depends on the local locale " +
                "which may produce inconsistent rule evaluation results."));
    }

    @Test
    public void shouldThrowWarningWhenDefaultReturnTypeIsDateButExpressionEvaluationTypeIsDateTime() {
        Rule rule = rule("R", "Policy", "txEffectiveDate");
        DefaultValuePayload payload = factory.createDefaultValuePayload();
        payload.setDefaultingType(DefaultingType.defaultValue);
        payload.setValueExpression(expressionOf("DateTime('2018-1-1T00:00:00')"));
        rule.setPayload(payload);

        ContextDefinition contextDefinition = contextDefinition("Policy", List.of(field("txEffectiveDate", DATE)));
        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, hasSize(1));
        assertThat(validationMessages.get(0).getSeverity(), is(Severity.WARNING));
        assertThat(validationMessages.get(0).getMessage(),
            equalTo("Return type of default expression must be compatible with field type which is Date, " +
                "but expression return type is DateTime. " +
                "DateTime value will be automatically converted to Date value as a date in local locale at that moment in time. " +
                "Automatic conversion should be avoided because it is a lossy operation " +
                "and the converted value depends on the local locale " +
                "which may produce inconsistent rule evaluation results."));
    }

    @Test
    public void shouldPassValidationRuleReferencingExternalEntityInDefaultExpression() {
        Rule rule = rule("R", "Policy", "policyNumber");
        DefaultValuePayload payload = factory.createDefaultValuePayload();
        payload.setDefaultingType(DefaultingType.defaultValue);
        payload.setValueExpression(expressionOf("context.prev.previousNumber"));
        rule.setPayload(payload);

        ContextDefinition contextDefinition = contextDefinition("Policy", List.of(field("policyNumber", STRING)));

        ExternalContextDefinition externalContextDefinition = KrakenProjectMocks.externalContextDefinition("PreviousPolicy",
                List.of(attribute("previousNumber", type(STRING.name(), SINGLE, true))));
        ExternalContext externalContext = KrakenProjectMocks.externalContext("ExternalContext_root",
                Map.of(),
                Map.of("prev",
                        KrakenProjectMocks.createExternalContextDefinitionReference(externalContextDefinition.getName())));
        ExternalContext rootExternalContext = KrakenProjectMocks.externalContext("ExternalContext_context",
                Map.of("context", externalContext),
                Map.of());

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), rootExternalContext,
                List.of(externalContextDefinition), entryPoints(), List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, empty());
    }

    @Test
    public void shouldFailWhenExternalContextDefinitionAttributeTypeIsNotCompatibleWithFieldType() {
        Rule rule = rule("R", "Policy", "policyField");
        DefaultValuePayload payload = factory.createDefaultValuePayload();
        payload.setDefaultingType(DefaultingType.defaultValue);
        payload.setValueExpression(expressionOf("context.prev.previousField"));
        rule.setPayload(payload);

        ContextDefinition contextDefinition = contextDefinition("Policy", List.of(field("policyField", STRING)));

        ExternalContextDefinition externalContextDefinition = KrakenProjectMocks.externalContextDefinition("PreviousPolicy",
                List.of(attribute("previousField", type(BOOLEAN.name(), SINGLE, true))));
        ExternalContext externalContext = KrakenProjectMocks.externalContext("ExternalContext_context",
                Map.of(),
                Map.of("prev", KrakenProjectMocks.createExternalContextDefinitionReference(externalContextDefinition.getName())));
        ExternalContext rootExternalContext = KrakenProjectMocks.externalContext("ExternalContext_root",
                Map.of("context", externalContext),
                Map.of());

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), rootExternalContext,
                List.of(externalContextDefinition), entryPoints(), List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, hasSize(1));
        assertThat(validationMessages.get(0).getMessage(),
                equalTo("Return type of default expression must be compatible with field type which is String, "
                    + "but expression return type is Boolean.")
        );
    }

    @Test
    public void shouldFailWhenExpressionReferencesNonExistingExternalContextDefinition() {
        Rule rule = rule("R", "Policy", "someField");
        DefaultValuePayload payload = factory.createDefaultValuePayload();
        payload.setDefaultingType(DefaultingType.defaultValue);
        payload.setValueExpression(expressionOf("context.next.nextField"));
        rule.setPayload(payload);

        ContextDefinition contextDefinition = contextDefinition("Policy", List.of(field("someField", STRING)));

        ExternalContextDefinition externalContextDefinition = KrakenProjectMocks.externalContextDefinition("PreviousPolicy",
                List.of(attribute("nextField", type(BOOLEAN.name(), SINGLE, true))));
        ExternalContext externalContext = KrakenProjectMocks.externalContext("ExternalContext_context",
                Map.of(),
                Map.of("oth", KrakenProjectMocks.createExternalContextDefinitionReference(externalContextDefinition.getName())));
        ExternalContext rootExternalContext = KrakenProjectMocks.externalContext("ExternalContext_root",
                Map.of("context", externalContext),
                Map.of());

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), rootExternalContext,
                List.of(externalContextDefinition), entryPoints(), List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, hasSize(2));
        assertThat(validationMessages.stream()
                        .map(validationMessage -> validationMessage.getMessage())
                        .collect(Collectors.toList()),
                containsInAnyOrder(
                        "Default expression has error in 'context.next.nextField'. "
                            + "Attribute 'next' not found in 'ExternalContext_context'.",
                        "Return type of default expression must be compatible with field type which is String, "
                            + "but expression return type is Unknown.")
        );
    }

    @Test
    public void shouldFailWhenExpressionReferencesIncorrectTypeOfExternalContextDefinitionAttribute() {
        Rule rule = rule("R", "Policy", "stringField");
        DefaultValuePayload payload = factory.createDefaultValuePayload();
        payload.setDefaultingType(DefaultingType.defaultValue);
        payload.setValueExpression(expressionOf("context.prev.previousStringField + 10"));
        rule.setPayload(payload);

        ContextDefinition contextDefinition = contextDefinition("Policy", List.of(field("stringField", STRING)));

        ExternalContextDefinition externalContextDefinition = KrakenProjectMocks.externalContextDefinition("PreviousPolicy",
                List.of(attribute("previousStringField", type(STRING.name(), SINGLE, true))));
        ExternalContext externalContext = KrakenProjectMocks.externalContext("ExternalContext_context",
                Map.of(),
                Map.of("prev", KrakenProjectMocks.createExternalContextDefinitionReference(externalContextDefinition.getName())));
        ExternalContext rootExternalContext = KrakenProjectMocks.externalContext("ExternalContext_root",
                Map.of("context", externalContext),
                Map.of());

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), rootExternalContext,
                List.of(externalContextDefinition), entryPoints(), List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, hasSize(2));
        assertThat(validationMessages.stream()
                        .map(validationMessage -> validationMessage.getMessage())
                        .collect(Collectors.toList()),
                containsInAnyOrder(
                        "Default expression has error in 'context.prev.previousStringField + 10'. "
                            + "Left side of Addition operation must be of type 'Number' but was 'String'.",
                        "Return type of default expression must be compatible with field type which is String, "
                            + "but expression return type is Number.")
        );
    }

    @Test
    public void shouldFailValidationWhenFunctionSignatureIsNotCompatible() {
        Rule rule = rule("R", "Policy", "expirationDate");
        DefaultValuePayload payload = factory.createDefaultValuePayload();
        payload.setDefaultingType(DefaultingType.defaultValue);
        payload.setValueExpression(expressionOf("CustomFunction(1)"));
        rule.setPayload(payload);

        FunctionSignature function = functionSignature("CustomFunction", "Any", List.of("String"));
        ContextDefinition contextDefinition = contextDefinition("Policy", List.of(field("expirationDate", DATETIME)));
        KrakenProject krakenProject = krakenProject(
            List.of(contextDefinition),
            entryPoints(),
            List.of(rule),
            List.of(function)
        );

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages.get(0).getMessage(),
            equalTo(
                "Default expression has error in 'CustomFunction(1)'. "
                    + "Incompatible type 'Number' of function parameter at index 0 when invoking function CustomFunction(1). "
                    + "Expected type is 'String'."
            )
        );
    }

    @Test
    public void shouldNotFailValidationWhenFunctionSignatureIsCompatible() {
        Rule rule = rule("R", "Policy", "expirationDate");
        DefaultValuePayload payload = factory.createDefaultValuePayload();
        payload.setDefaultingType(DefaultingType.defaultValue);
        payload.setValueExpression(expressionOf("CustomFunctionString('string')"));
        rule.setPayload(payload);

        FunctionSignature function = functionSignature("CustomFunctionString", "Any", List.of("String"));
        ContextDefinition contextDefinition = contextDefinition("Policy", List.of(field("expirationDate", DATETIME)));
        KrakenProject krakenProject = krakenProject(
            List.of(contextDefinition),
            entryPoints(),
            List.of(rule),
            List.of(function)
        );

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, empty());
    }

    @Test
    public void shouldNotBeValidIfExpressionIsEmpty() {
        Rule rule = rule("R1", "PartyContext", "name");
        rule.setCondition(conditionOf("  "));
        Payload assertionPayload = factory.createAssertionPayload();
        ((AssertionPayload) assertionPayload).setAssertionExpression(expressionOf("// todo"));
        rule.setPayload(assertionPayload);

        ContextDefinition contextDefinition = contextDefinition("PartyContext", List.of(field("surname")));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, hasSize(2));
        assertThat(
            validationMessages.stream().map(ValidationMessage::getMessage).collect(Collectors.toList()),
            containsInAnyOrder(
                "Condition expression is logically empty. "
                    + "Please check if there are unintentional spaces, new lines or comments remaining.",
                "Assertion expression is logically empty. "
                    + "Please check if there are unintentional spaces, new lines or comments remaining."
            )
        );
    }

    @Test
    public void shouldNotBeValidIfExpressionIsNotParseable() {
        Rule rule = rule("R1", "PartyContext", "name");
        Payload assertionPayload = factory.createAssertionPayload();
        ((AssertionPayload) assertionPayload).setAssertionExpression(expressionOf("name = \"\"\""));
        rule.setPayload(assertionPayload);

        ContextDefinition contextDefinition = contextDefinition("PartyContext", List.of(field("surname")));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, hasSize(1));
        assertThat(
            validationMessages.stream().map(ValidationMessage::getMessage).collect(Collectors.toList()),
            containsInAnyOrder(
                "Assertion expression cannot be parsed, because there is an error in expression syntax."
            )
        );
    }

    @Test
    public void shouldCreateInfoMessageForLiteralTrueConditionExpression() {
        Rule rule = rule("R1", "PartyContext", "surname");
        rule.setCondition(conditionOf("true"));
        Payload assertionPayload = factory.createAssertionPayload();
        ((AssertionPayload) assertionPayload).setAssertionExpression(expressionOf("age >= 21"));
        rule.setPayload(assertionPayload);

        ContextDefinition contextDefinition = contextDefinition("PartyContext", List.of(
            field("surname"),
            field("name"),
            field("age", DECIMAL)
        ));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, hasSize(1));
        assertThat(validationMessages.get(0).getSeverity(), is(Severity.INFO));
        assertThat(validationMessages.get(0).getMessage(),
            equalTo("Redundant literal value true in rule condition expression. "
                + "An empty condition expression is true by default."));
    }

    @Test
    public void shouldCreateWarningMessageForNumbersThatDoesNotFitInDecimal64() {
        Rule rule = rule("R1", "PartyContext", "surname");
        Payload assertionPayload = factory.createAssertionPayload();
        ((AssertionPayload) assertionPayload).setAssertionExpression(expressionOf("age >= 1234567890.1234567"));
        rule.setPayload(assertionPayload);

        ContextDefinition contextDefinition = contextDefinition("PartyContext", List.of(
            field("surname"),
            field("age", DECIMAL)
        ));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, hasSize(1));
        assertThat(validationMessages.get(0).getSeverity(), is(Severity.WARNING));
        assertThat(validationMessages.get(0).getMessage(),
            equalTo("Assertion expression has warning message about '1234567890.1234567'. "
                + "Number '1234567890.1234567' cannot be encoded as a decimal64 without a loss of precision. "
                + "Actual number at runtime would be rounded to '1234567890.123457'."));
    }

    private List<ValidationMessage> validate(Rule rule, KrakenProject krakenProject) {
        RuleExpressionValidator validator = new RuleExpressionValidator(krakenProject);
        ValidationSession session = new ValidationSession();
        validator.validate(rule, session);
        return session.getValidationMessages();
    }

    public static Expression expressionOf(String ex) {
        Expression expression = RulesModelFactory.getInstance().createExpression();
        expression.setExpressionString(ex);
        return expression;
    }

    public static Condition conditionOf(String ex) {
        Condition condition = RulesModelFactory.getInstance().createCondition();
        condition.setExpression(expressionOf(ex));
        return condition;
    }
}
