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
package kraken.runtime.engine.handlers;

import kraken.TestRuleBuilder;
import kraken.model.derive.DefaultingType;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.events.ValueChangedEvent;
import kraken.runtime.engine.result.DefaultValuePayloadResult;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
import org.junit.Before;
import org.junit.Test;

import static kraken.runtime.engine.handlers.PayloadHandlerTestConstants.SESSION;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class DefaultValuePayloadHandlerTest {
    private DefaultValuePayloadHandler defaultValuePayloadHandler;

    @Before
    public void setUp() {
        defaultValuePayloadHandler = new DefaultValuePayloadHandler(new KrakenExpressionEvaluator());
    }

    @Test
    public void executePayloadShouldReturnInstanceOfDefaultValuePayloadResult() {
        Coverage coverage = new Coverage();
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);

        RuntimeRule rule = TestRuleBuilder.getInstance()
                .defaultPayload("'Q001'", DefaultingType.defaultValue)
                .targetPath("code")
                .build();
        PayloadResult payloadResult = defaultValuePayloadHandler.executePayload(
                rule.getPayload(),
                rule,
                dataContext,
                SESSION
        );

        assertThat(payloadResult, is(instanceOf(DefaultValuePayloadResult.class)));
    }

    @Test
    public void executePayloadShouldReturnEventWithDefaultValueWhenDefaultingToDefaultValue() {
        Coverage coverage = new Coverage();
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);
        RuntimeRule rule = TestRuleBuilder.getInstance()
                .defaultPayload("'Q001'", DefaultingType.defaultValue)
                .targetPath("code")
                .build();

        DefaultValuePayloadResult payloadResult = (DefaultValuePayloadResult) defaultValuePayloadHandler.executePayload(
                rule.getPayload(),
                rule,
                dataContext,
                SESSION
        );
        assertThat(
                ((ValueChangedEvent) payloadResult.getEvents().get(0)).getNewValue().toString(),
                is(equalTo("Q001"))
        );
    }

    @Test
    public void executePayloadShouldReturnEventWithResetValueWhenResettingToResetValue() {
        Coverage coverage = new Coverage();
        coverage.setCode("Q001");
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);
        RuntimeRule rule = TestRuleBuilder.getInstance()
                .defaultPayload("'Q777'", DefaultingType.resetValue)
                .targetPath("code")
                .build();

        DefaultValuePayloadResult payloadResult = (DefaultValuePayloadResult) defaultValuePayloadHandler.executePayload(
                rule.getPayload(),
                rule,
                dataContext,
                SESSION
        );

        assertThat(
                ((ValueChangedEvent) payloadResult.getEvents().get(0)).getNewValue(),
                is(equalTo("Q777"))
        );
    }
}
