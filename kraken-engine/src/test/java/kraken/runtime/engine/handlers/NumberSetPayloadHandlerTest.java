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
package kraken.runtime.engine.handlers;

import static kraken.runtime.engine.handlers.PayloadHandlerTestConstants.SESSION;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.nullValue;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import kraken.TestRuleBuilder;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.result.NumberSetPayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.test.domain.policy.Policy;

/**
 * @author Mindaugas Ulevicius
 */
public class NumberSetPayloadHandlerTest {
    private NumberSetPayloadHandler payloadHandler;

    @Before
    public void setUp() {
        payloadHandler = new NumberSetPayloadHandler(new KrakenExpressionEvaluator());
    }

    @Test
    public void shouldReturnNumberSetPayloadResultForValidValue() {
        Policy policy = new Policy("P01");
        policy.setTermNo(2);
        var dataContext = new DataContext();
        dataContext.setDataObject(policy);
        RuntimeRule rule = TestRuleBuilder.getInstance()
            .targetPath("termNo")
            .numberSetPayload(0, 4, 2)
            .build();

        var result = payloadHandler.executePayload(rule.getPayload(), rule, dataContext, SESSION);
        assertThat(result, instanceOf(NumberSetPayloadResult.class));
        assertThat(((NumberSetPayloadResult) result).getSuccess(), is(true));
        assertThat(((NumberSetPayloadResult) result).getMin(), is(new BigDecimal("0")));
        assertThat(((NumberSetPayloadResult) result).getMax(), is(new BigDecimal("4")));
        assertThat(((NumberSetPayloadResult) result).getStep(), is(new BigDecimal("2")));
        assertThat(((NumberSetPayloadResult) result).getMessage(), nullValue());
    }

    @Test
    public void shouldValidateNullAndReturnTrue() {
        Policy policy = new Policy("P01");
        policy.setTermNo(null);
        var dataContext = new DataContext();
        dataContext.setDataObject(policy);
        RuntimeRule rule = TestRuleBuilder.getInstance()
            .targetPath("termNo")
            .numberSetPayload(0, 4, 2)
            .build();

        var result = payloadHandler.executePayload(rule.getPayload(), rule, dataContext, SESSION);
        assertThat(((NumberSetPayloadResult) result).getSuccess(), is(true));
    }

    @Test
    public void shouldReturnNumberSetPayloadResultForInvalidValue() {
        Policy policy = new Policy("P01");
        policy.setTermNo(1);
        var dataContext = new DataContext();
        dataContext.setDataObject(policy);
        RuntimeRule rule = TestRuleBuilder.getInstance()
            .targetPath("termNo")
            .numberSetPayload(0, 4, new BigDecimal("2"))
            .build();

        var result = payloadHandler.executePayload(rule.getPayload(), rule, dataContext, SESSION);
        assertThat(((NumberSetPayloadResult) result).getSuccess(), is(false));
    }
}
