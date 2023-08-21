/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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

import static kraken.model.project.KrakenProjectMocks.DEFAULT_NAMESPACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import kraken.model.ValueList;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationSession;
import kraken.model.validation.ValidationSeverity;

/**
 * @author mulevicius
 */
public class RuleModelValidatorPrecisionTest {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    private RuleModelValidator validator;

    @Before
    public void setUp() throws Exception {
        this.validator= new RuleModelValidator();
    }

    @Test
    public void shouldWarnAboutNumberSetRoundingIfMinMaxStepDoesNotFitInDecimal64() {
        var rule = factory.createRule();
        rule.setPhysicalNamespace(DEFAULT_NAMESPACE);
        rule.setName("RL");
        rule.setContext("Context");
        rule.setTargetPath("field");

        var payload = factory.createNumberSetPayload();
        payload.setMin(new BigDecimal("1234567890.1234567"));
        payload.setMax(new BigDecimal("1234567891.1234567"));
        payload.setStep(new BigDecimal("0.12345678901234567890123456789"));
        payload.setSeverity(ValidationSeverity.critical);
        rule.setPayload(payload);

        var session = new ValidationSession();
        validator.validate(rule, session);

        assertThat(session.getValidationMessages(), hasSize(3));
        assertThat(session.getValidationMessages().get(0).getSeverity(), equalTo(Severity.WARNING));
        assertThat(session.getValidationMessages().get(0).getMessage(),
            equalTo("Min value '1234567890.1234567' "
                + "cannot be encoded as a decimal64 without a loss of precision. "
                + "Actual number at runtime would be rounded to '1234567890.123457'.")
        );
        assertThat(session.getValidationMessages().get(1).getSeverity(), equalTo(Severity.WARNING));
        assertThat(session.getValidationMessages().get(1).getMessage(),
            equalTo("Max value '1234567891.1234567' "
                + "cannot be encoded as a decimal64 without a loss of precision. "
                + "Actual number at runtime would be rounded to '1234567891.123457'.")
        );
        assertThat(session.getValidationMessages().get(2).getSeverity(), equalTo(Severity.WARNING));
        assertThat(session.getValidationMessages().get(2).getMessage(),
            equalTo("Step value '0.12345678901234567890123456789' "
                + "cannot be encoded as a decimal64 without a loss of precision. "
                + "Actual number at runtime would be rounded to '0.1234567890123457'.")
        );
    }

    @Test
    public void shouldWarnAboutValueListRoundingIfValueDoesNotFitInDecimal64() {
        var rule = factory.createRule();
        rule.setPhysicalNamespace(DEFAULT_NAMESPACE);
        rule.setName("RL");
        rule.setContext("Context");
        rule.setTargetPath("field");

        var payload = factory.createValueListPayload();
        payload.setValueList(ValueList.fromNumber(List.of(
            new BigDecimal("0.12345678901234567890123456789"),
            new BigDecimal("1234567890.1234567"),
            new BigDecimal("0.1234567890123456")
        )));
        payload.setSeverity(ValidationSeverity.critical);
        rule.setPayload(payload);

        var session = new ValidationSession();
        validator.validate(rule, session);

        assertThat(session.getValidationMessages(), hasSize(2));
        assertThat(session.getValidationMessages().get(0).getSeverity(), equalTo(Severity.WARNING));
        assertThat(session.getValidationMessages().get(0).getMessage(),
            equalTo("Value list value '0.12345678901234567890123456789' cannot be encoded as a decimal64 "
                + "without a loss of precision. Actual number at runtime would be rounded to '0.1234567890123457'.")
        );
        assertThat(session.getValidationMessages().get(1).getSeverity(), equalTo(Severity.WARNING));
        assertThat(session.getValidationMessages().get(1).getMessage(),
            equalTo("Value list value '1234567890.1234567' "
                + "cannot be encoded as a decimal64 without a loss of precision. "
                + "Actual number at runtime would be rounded to '1234567890.123457'.")
        );
    }

    @Test
    public void shouldWarnAboutDimensionValueRoundingIfValueDoesNotFitInDecimal64() {
        var rule = factory.createRule();
        rule.setPhysicalNamespace(DEFAULT_NAMESPACE);
        rule.setName("RL");
        rule.setContext("Context");
        rule.setTargetPath("field");
        rule.setPayload(factory.createAccessibilityPayload());
        var metadata = factory.createMetadata();
        metadata.setProperty("Limit",  new BigDecimal("0.12345678901234567890123456789"));
        rule.setMetadata(metadata);
        var session = new ValidationSession();
        validator.validate(rule, session);

        assertThat(session.getValidationMessages(), hasSize(1));
        assertThat(session.getValidationMessages().get(0).getSeverity(), equalTo(Severity.WARNING));
        assertThat(session.getValidationMessages().get(0).getMessage(),
            equalTo("Dimension 'Limit' value '0.12345678901234567890123456789' cannot be encoded as a "
                + "decimal64 without a loss of precision. "
                + "Actual number at runtime would be rounded to '0.1234567890123457'.")
        );
    }

}
