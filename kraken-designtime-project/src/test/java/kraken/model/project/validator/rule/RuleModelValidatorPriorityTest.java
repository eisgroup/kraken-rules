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
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.Before;
import org.junit.Test;

import kraken.model.derive.DefaultingType;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationSession;

/**
 * @author mulevicius
 */
public class RuleModelValidatorPriorityTest {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    private RuleModelValidator validator;

    @Before
    public void setUp() throws Exception {
        this.validator= new RuleModelValidator();
    }

    @Test
    public void shouldValidateThatPriorityIsNotAllowedForUnsupportedPayloadType() {
        var rule = factory.createRule();
        rule.setPhysicalNamespace(DEFAULT_NAMESPACE);
        rule.setName("RL");
        rule.setContext("Context");
        rule.setTargetPath("field");
        rule.setPayload(factory.createAccessibilityPayload());
        rule.setPriority(10);

        var session = new ValidationSession();
        validator.validate(rule, session);

        assertThat(session.getValidationMessages(), hasSize(1));
        assertThat(session.getValidationMessages().get(0).getSeverity(), equalTo(Severity.ERROR));
        assertThat(
            session.getValidationMessages().get(0).getMessage(),
            equalTo("Priority cannot be set because rule payload type is AccessibilityPayload "
                + "- priority is supported only for defaulting rules.")
        );
    }

    @Test
    public void shouldValidateThatPriorityIsAllowedForSupportedPayloadType() {
        var rule = factory.createRule();
        rule.setPhysicalNamespace(DEFAULT_NAMESPACE);
        rule.setName("RL");
        rule.setContext("Context");
        rule.setTargetPath("field");
        var payload = factory.createDefaultValuePayload();
        payload.setDefaultingType(DefaultingType.defaultValue);
        var expression = factory.createExpression();
        expression.setExpressionString("'value'");
        payload.setValueExpression(expression);
        rule.setPayload(payload);
        rule.setPriority(10);

        var session = new ValidationSession();
        validator.validate(rule, session);

        assertThat(session.getValidationMessages(), empty());
    }
}
