/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.model.project.validator.rule;

import static kraken.model.project.KrakenProjectMocks.DEFAULT_NAMESPACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import kraken.model.Rule;
import kraken.model.ValueList;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationSession;
import kraken.model.validation.ValidationSeverity;
import kraken.model.validation.ValueListPayload;

/**
 * @author Tomas Dapkunas
 * @since 1.43.0
 */
public class RuleModelValidatorValueListTest {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    private RuleModelValidator validator;

    @Before
    public void setUp() {
        validator = new RuleModelValidator();
    }

    @Test
    public void shouldContainNoValidationErrorForValidRule() {
        Rule validRule = createRule(ValueList.fromNumber(List.of(BigDecimal.valueOf(100))));

        ValidationSession validationSession = validate(validRule);

        assertThat(validationSession.getValidationMessages(), hasSize(0));
    }

    @Test
    public void shouldContainValidationErrorForRuleWithNoValueList() {
        Rule validRule = createRule(null);

        ValidationSession validationSession = validate(validRule);

        assertThat(validationSession.getValidationMessages(), hasSize(1));
        assertThat(validationSession.getValidationMessages().get(0).getSeverity(), is(Severity.ERROR));
        assertThat(validationSession.getValidationMessages().get(0).getMessage(),
            is("ValueList must be set"));
    }

    @Test
    public void shouldContainValidationErrorsForRuleWithNoValuesAndType() {
        ValueList valueList = mock(ValueList.class);
        when(valueList.getValues()).thenReturn(null);
        when(valueList.getValueType()).thenReturn(null);

        Rule validRule = createRule(valueList);

        ValidationSession validationSession = validate(validRule);

        assertThat(validationSession.getValidationMessages(), hasSize(2));
        assertThat(validationSession.getValidationMessages().get(0).getSeverity(), is(Severity.ERROR));
        assertThat(validationSession.getValidationMessages().get(0).getMessage(),
            is("ValueList data type must be set"));
        assertThat(validationSession.getValidationMessages().get(1).getSeverity(), is(Severity.ERROR));
        assertThat(validationSession.getValidationMessages().get(1).getMessage(),
            is("ValueList should contain at least one value"));
    }

    private ValidationSession validate(Rule rule) {
        ValidationSession validationSession = new ValidationSession();
        validator.validate(rule, validationSession);

        return validationSession;
    }

    private Rule createRule(ValueList valueList) {
        ValueListPayload valueListPayload = factory.createValueListPayload();
        valueListPayload.setValueList(valueList);
        valueListPayload.setSeverity(ValidationSeverity.critical);

        Rule rule = factory.createRule();
        rule.setPayload(valueListPayload);
        rule.setPhysicalNamespace(DEFAULT_NAMESPACE);
        rule.setName("VL_RULE");
        rule.setContext("Context");
        rule.setTargetPath("contextField");

        return rule;
    }

}
