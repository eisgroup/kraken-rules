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
package kraken.model.project.validator.rule;

import java.util.List;
import java.util.Map;

import kraken.model.Payload;
import kraken.model.Rule;
import kraken.model.context.Cardinality;
import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.model.context.PrimitiveFieldDataType;
import kraken.model.context.SystemDataTypes;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationSession;
import kraken.model.state.AccessibilityPayload;
import kraken.model.validation.AssertionPayload;
import kraken.model.validation.LengthPayload;
import kraken.model.validation.RegExpPayload;
import kraken.model.validation.SizePayload;
import kraken.model.validation.SizeRangePayload;
import kraken.model.validation.UsagePayload;
import kraken.model.validation.ValueListPayload;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static kraken.model.context.Cardinality.MULTIPLE;
import static kraken.model.context.Cardinality.SINGLE;
import static kraken.model.context.PrimitiveFieldDataType.BOOLEAN;
import static kraken.model.context.PrimitiveFieldDataType.DATE;
import static kraken.model.context.PrimitiveFieldDataType.DATETIME;
import static kraken.model.context.PrimitiveFieldDataType.DECIMAL;
import static kraken.model.context.PrimitiveFieldDataType.INTEGER;
import static kraken.model.context.PrimitiveFieldDataType.MONEY;
import static kraken.model.context.PrimitiveFieldDataType.STRING;
import static kraken.model.project.KrakenProjectMocks.entryPoints;
import static kraken.model.project.KrakenProjectMocks.krakenProject;
import static kraken.model.project.validator.rule.RulePayloadCompatibilityValidatorTest.IsPayloadCompatibleWith.compatibleWithMultipleComplex;
import static kraken.model.project.validator.rule.RulePayloadCompatibilityValidatorTest.IsPayloadCompatibleWith.compatibleWithMultiplePrimitive;
import static kraken.model.project.validator.rule.RulePayloadCompatibilityValidatorTest.IsPayloadCompatibleWith.compatibleWithMultipleSystem;
import static kraken.model.project.validator.rule.RulePayloadCompatibilityValidatorTest.IsPayloadCompatibleWith.compatibleWithSingleComplex;
import static kraken.model.project.validator.rule.RulePayloadCompatibilityValidatorTest.IsPayloadCompatibleWith.compatibleWithSinglePrimitive;
import static kraken.model.project.validator.rule.RulePayloadCompatibilityValidatorTest.IsPayloadCompatibleWith.compatibleWithSingleSystem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author mulevicius
 */
public class RulePayloadCompatibilityValidatorTest {

    private static RulesModelFactory factory = RulesModelFactory.getInstance();

    @Test
    public void shouldValidateUsagePayloadCompatibility() {
        UsagePayload payload = factory.createUsagePayload();

        assertThat(payload, compatibleWithSinglePrimitive());
        assertThat(payload, not(compatibleWithMultiplePrimitive()));
        assertThat(payload, compatibleWithSingleSystem());
        assertThat(payload, not(compatibleWithMultipleSystem()));
        assertThat(payload, compatibleWithSingleComplex());
        assertThat(payload, not(compatibleWithMultipleComplex()));
    }

    @Test
    public void shouldValidateRegExpPayloadCompatibility() {
        RegExpPayload payload = factory.createRegExpPayload();

        assertThat(payload, compatibleWithSinglePrimitive(STRING));
        assertThat(payload, not(compatibleWithSinglePrimitive(INTEGER)));
        assertThat(payload, not(compatibleWithSinglePrimitive(DECIMAL)));
        assertThat(payload, not(compatibleWithSinglePrimitive(DATE)));
        assertThat(payload, not(compatibleWithSinglePrimitive(DATETIME)));
        assertThat(payload, not(compatibleWithSinglePrimitive(BOOLEAN)));
        assertThat(payload, not(compatibleWithSinglePrimitive(MONEY)));
        assertThat(payload, not(compatibleWithMultiplePrimitive()));
        assertThat(payload, not(compatibleWithSingleSystem()));
        assertThat(payload, not(compatibleWithMultipleSystem()));
        assertThat(payload, not(compatibleWithSingleComplex()));
        assertThat(payload, not(compatibleWithMultipleComplex()));
    }

    @Test
    public void shouldValidateLengthPayloadCompatibility() {
        LengthPayload payload = factory.createLengthPayload();

        assertThat(payload, compatibleWithSinglePrimitive(STRING));
        assertThat(payload, not(compatibleWithSinglePrimitive(INTEGER)));
        assertThat(payload, not(compatibleWithSinglePrimitive(DECIMAL)));
        assertThat(payload, not(compatibleWithSinglePrimitive(DATE)));
        assertThat(payload, not(compatibleWithSinglePrimitive(DATETIME)));
        assertThat(payload, not(compatibleWithSinglePrimitive(BOOLEAN)));
        assertThat(payload, not(compatibleWithSinglePrimitive(MONEY)));
        assertThat(payload, not(compatibleWithMultiplePrimitive()));
        assertThat(payload, not(compatibleWithSingleSystem()));
        assertThat(payload, not(compatibleWithMultipleSystem()));
        assertThat(payload, not(compatibleWithSingleComplex()));
        assertThat(payload, not(compatibleWithMultipleComplex()));
    }

    @Test
    public void shouldValidateSizePayloadCompatibility() {
        SizePayload payload = factory.createSizePayload();

        assertThat(payload, not(compatibleWithSinglePrimitive()));
        assertThat(payload, compatibleWithMultiplePrimitive());
        assertThat(payload, not(compatibleWithSingleSystem()));
        assertThat(payload, compatibleWithMultipleSystem());
        assertThat(payload, not(compatibleWithSingleComplex()));
        assertThat(payload, compatibleWithMultipleComplex());
    }

    @Test
    public void shouldValidateSizeRangePayloadCompatibility() {
        SizeRangePayload payload = factory.createSizeRangePayload();

        assertThat(payload, not(compatibleWithSinglePrimitive()));
        assertThat(payload, compatibleWithMultiplePrimitive());
        assertThat(payload, not(compatibleWithSingleSystem()));
        assertThat(payload, compatibleWithMultipleSystem());
        assertThat(payload, not(compatibleWithSingleComplex()));
        assertThat(payload, compatibleWithMultipleComplex());
    }

    @Test
    public void shouldValidateAccessibilityPayloadCompatibility() {
        AccessibilityPayload payload = factory.createAccessibilityPayload();

        assertThat(payload, compatibleWithSinglePrimitive());
        assertThat(payload, compatibleWithMultiplePrimitive());
        assertThat(payload, not(compatibleWithSingleSystem()));
        assertThat(payload, not(compatibleWithMultipleSystem()));
        assertThat(payload, compatibleWithSingleComplex());
        assertThat(payload, compatibleWithMultipleComplex());
    }

    @Test
    public void shouldValidateVisibilityPayloadCompatibility() {
        AccessibilityPayload payload = factory.createAccessibilityPayload();

        assertThat(payload, compatibleWithSinglePrimitive());
        assertThat(payload, compatibleWithMultiplePrimitive());
        assertThat(payload, not(compatibleWithSingleSystem()));
        assertThat(payload, not(compatibleWithMultipleSystem()));
        assertThat(payload, compatibleWithSingleComplex());
        assertThat(payload, compatibleWithMultipleComplex());
    }

    @Test
    public void shouldValidateDefaultValuePayloadCompatibility() {
        DefaultValuePayload payload = factory.createDefaultValuePayload();

        assertThat(payload, compatibleWithSinglePrimitive());
        assertThat(payload, not(compatibleWithMultiplePrimitive()));
        assertThat(payload, not(compatibleWithSingleSystem()));
        assertThat(payload, not(compatibleWithMultipleSystem()));
        assertThat(payload, not(compatibleWithSingleComplex()));
        assertThat(payload, not(compatibleWithMultipleComplex()));
    }

    @Test
    public void shouldValidateAssertionPayloadCompatibility() {
        AssertionPayload payload = factory.createAssertionPayload();

        assertThat(payload, compatibleWithSinglePrimitive());
        assertThat(payload, compatibleWithMultiplePrimitive());
        assertThat(payload, compatibleWithSingleSystem());
        assertThat(payload, compatibleWithMultipleSystem());
        assertThat(payload, compatibleWithSingleComplex());
        assertThat(payload, compatibleWithMultipleComplex());
    }

    @Test
    public void shouldValidateNumberSetPayloadCompatibility() {
        var payload = factory.createNumberSetPayload();

        assertThat(payload, compatibleWithSinglePrimitive(INTEGER));
        assertThat(payload, compatibleWithSinglePrimitive(DECIMAL));
        assertThat(payload, compatibleWithSinglePrimitive(MONEY));
        assertThat(payload, not(compatibleWithSinglePrimitive(DATE)));
        assertThat(payload, not(compatibleWithSinglePrimitive(DATETIME)));
        assertThat(payload, not(compatibleWithSinglePrimitive(BOOLEAN)));
        assertThat(payload, not(compatibleWithSinglePrimitive(STRING)));
        assertThat(payload, not(compatibleWithMultiplePrimitive()));
        assertThat(payload, not(compatibleWithSingleSystem()));
        assertThat(payload, not(compatibleWithMultipleSystem()));
        assertThat(payload, not(compatibleWithSingleComplex()));
        assertThat(payload, not(compatibleWithMultipleComplex()));
    }

    @Test
    public void shouldValidateValueListPayloadCompatibility() {
        ValueListPayload payload = factory.createValueListPayload();

        assertThat(payload, compatibleWithSinglePrimitive(MONEY));
        assertThat(payload, compatibleWithSinglePrimitive(DECIMAL));
        assertThat(payload, compatibleWithSinglePrimitive(INTEGER));
        assertThat(payload, compatibleWithSinglePrimitive(STRING));
        assertThat(payload, not(compatibleWithSinglePrimitive(BOOLEAN)));
        assertThat(payload, not(compatibleWithSinglePrimitive(DATE)));
        assertThat(payload, not(compatibleWithSinglePrimitive(DATETIME)));
        assertThat(payload, not(compatibleWithMultiplePrimitive()));
        assertThat(payload, not(compatibleWithSingleComplex()));
        assertThat(payload, not(compatibleWithMultipleComplex()));
    }

    static class IsPayloadCompatibleWith extends TypeSafeMatcher<Payload> {

        private String type;
        private Cardinality cardinality;

        public IsPayloadCompatibleWith(String type, Cardinality cardinality) {
            this.type = type;
            this.cardinality = cardinality;
        }

        @Override
        protected boolean matchesSafely(Payload payload) {
            ContextField contextField = factory.createContextField();
            contextField.setName("field");
            contextField.setFieldPath("field");
            contextField.setCardinality(cardinality);
            contextField.setFieldType(type);

            ContextDefinition contextDefinition = factory.createContextDefinition();
            contextDefinition.setName("Context");
            contextDefinition.setStrict(true);
            contextDefinition.setContextFields(Map.of(contextField.getName(), contextField));
            contextDefinition.setPhysicalNamespace("Base");

            Rule rule = factory.createRule();
            rule.setContext("Context");
            rule.setTargetPath("field");
            rule.setPayload(payload);
            rule.setPhysicalNamespace("Base");

            KrakenProject krakenProject = krakenProject(
                    List.of(contextDefinition),
                    entryPoints(),
                    List.of(rule)
            );

            RulePayloadCompatibilityValidator validator = new RulePayloadCompatibilityValidator(krakenProject);
            ValidationSession session = new ValidationSession();
            validator.validate(rule, session);
            return session.getValidationMessages().isEmpty();
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Payload which is compatible with " + type + " " + cardinality);
        }

        static IsPayloadCompatibleWith compatibleWithSinglePrimitive() {
            return compatibleWithSinglePrimitive(STRING);
        }

        static IsPayloadCompatibleWith compatibleWithSinglePrimitive(PrimitiveFieldDataType type) {
            return new IsPayloadCompatibleWith(type.toString(), SINGLE);
        }

        static IsPayloadCompatibleWith compatibleWithMultiplePrimitive() {
            return new IsPayloadCompatibleWith(STRING.toString(), MULTIPLE);
        }

        static IsPayloadCompatibleWith compatibleWithSingleSystem() {
            return new IsPayloadCompatibleWith(SystemDataTypes.UNKNOWN.toString(), SINGLE);
        }

        static IsPayloadCompatibleWith compatibleWithMultipleSystem() {
            return new IsPayloadCompatibleWith(SystemDataTypes.UNKNOWN.toString(), MULTIPLE);
        }

        static IsPayloadCompatibleWith compatibleWithSingleComplex() {
            return new IsPayloadCompatibleWith("Context", SINGLE);
        }

        static IsPayloadCompatibleWith compatibleWithMultipleComplex() {
            return new IsPayloadCompatibleWith("Context", MULTIPLE);
        }
    }
}
