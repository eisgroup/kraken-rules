/*
 *  Copyright 2023 EIS Ltd and/or one of its affiliates.
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
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsEqual.equalTo;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import kraken.model.Rule;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationSession;
import kraken.model.validation.ValidationSeverity;

/**
 * @author Mindaugas Ulevicius
 */
public class RuleModelNumberSetValidatorTest {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    private RuleModelValidator validator;

    @Before
    public void setUp() throws Exception {
        this.validator= new RuleModelValidator();
    }

    @Test
    public void shouldBeValidNumberSetWithMinOnly() {
        var session = validate(numberSetRule(1, null, null));
        assertThat(session.getValidationMessages(), empty());
    }
    @Test
    public void shouldBeValidNumberSetWithMaxOnly() {
        var session = validate(numberSetRule(null, 1, null));
        assertThat(session.getValidationMessages(), empty());
    }

    @Test
    public void shouldBeValidNumberSetWithMinMaxOnly() {
        var session = validate(numberSetRule(-10, -1, null));
        assertThat(session.getValidationMessages(), empty());
    }

    @Test
    public void shouldBeValidNumberSetWithMinAndStepOnly() {
        var session = validate(numberSetRule(-10, null, 1));
        assertThat(session.getValidationMessages(), empty());
    }

    @Test
    public void shouldBeValidNumberSetWithMaxAndStepOnly() {
        var session = validate(numberSetRule(null, -10, 1));
        assertThat(session.getValidationMessages(), empty());
    }

    @Test
    public void shouldNotBeValidNumberSetWithoutMinAndMax() {
        var session = validate(numberSetRule(null, null, 1));

        assertHasSingleValidationError(
            session,
            "Min or max must be set.");
    }

    @Test
    public void shouldNotBeValidNumberSetWithNegativeStep() {
        var session = validate(numberSetRule(1, 10, -1));

        assertHasSingleValidationError(
            session,
            "Step must be more than zero.");
    }

    @Test
    public void shouldNotBeValidNumberSetWithZeroStep() {
        var session = validate(numberSetRule(1, 10, 0));

        assertHasSingleValidationError(
            session,
            "Step must be more than zero.");
    }

    @Test
    public void shouldNotBeValidNumberSetWithMaxLessThanMin() {
        var session = validate(numberSetRule(10, -1, null));

        assertHasSingleValidationError(
            session,
            "Min must be smaller than max.");
    }

    @Test
    public void shouldNotBeValidNumberSetWithMaxEqualToMin() {
        var session = validate(numberSetRule(-999, -999, 1));

        assertHasSingleValidationError(
            session,
            "Min must be smaller than max.");
    }

    private void assertHasSingleValidationError(ValidationSession session, String message) {
        assertThat(session.getValidationMessages(), hasSize(1));
        assertThat(session.getValidationMessages().get(0).getSeverity(), equalTo(Severity.ERROR));
        assertThat(session.getValidationMessages().get(0).getMessage(), equalTo(message));
    }

    private ValidationSession validate(Rule rule) {
        var session = new ValidationSession();
        validator.validate(rule, session);
        return session;
    }

    private Rule numberSetRule(Integer min, Integer max, Integer step) {
        return numberSetRule(
            min != null ? new BigDecimal(min) : null,
            max != null ? new BigDecimal(max) : null,
            step != null ? new BigDecimal(step) : null
        );
    }

    private Rule numberSetRule(BigDecimal min, BigDecimal max, BigDecimal step) {
        var rule = factory.createRule();
        rule.setPhysicalNamespace(DEFAULT_NAMESPACE);
        rule.setName("RL");
        rule.setContext("Context");
        rule.setTargetPath("field");
        var payload = factory.createNumberSetPayload();
        payload.setMin(min);
        payload.setMax(max);
        payload.setStep(step);
        payload.setSeverity(ValidationSeverity.critical);
        rule.setPayload(payload);
        return rule;
    }
}
