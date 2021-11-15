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
import kraken.model.validation.UsageType;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.engine.result.UsagePayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
import org.junit.Before;
import org.junit.Test;

import static kraken.runtime.engine.handlers.PayloadHandlerTestConstants.SESSION;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UsagePayloadHandlerTest {
    private UsagePayloadHandler usagePayloadHandler;

    @Before
    public void setUp() {
        usagePayloadHandler = new UsagePayloadHandler(new KrakenExpressionEvaluator());
    }

    @Test
    public void executePayloadShouldReturnUsagePayloadResult() {
        Coverage coverage = new Coverage();
        coverage.setCode("'Q001'");
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);

        RuntimeRule rule = TestRuleBuilder.getInstance()
                .targetPath("code")
                .usagePayload(UsageType.mandatory)
                .build();

        PayloadResult payloadResult = usagePayloadHandler.executePayload(
                rule.getPayload(),
                rule,
                dataContext,
                SESSION
        );

        assertThat(payloadResult, is(instanceOf(UsagePayloadResult.class)));
        assertThat(((UsagePayloadResult) payloadResult).getSuccess(), is(true));
    }

    @Test
    public void executePayloadShouldReturnUnsuccessfulResultWhenExpressionValueIsMandatoryButEmpty() {
        Coverage coverage = new Coverage();
        coverage.setCode("");
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);

        RuntimeRule rule = TestRuleBuilder.getInstance()
                .targetPath("code")
                .usagePayload(UsageType.mandatory)
                .build();

        UsagePayloadResult payloadResult = (UsagePayloadResult) usagePayloadHandler.executePayload(
                rule.getPayload(),
                rule,
                dataContext,
                SESSION
        );

        assertThat(payloadResult.getSuccess(), is(false));
    }

    @Test
    public void executePayloadShouldReturnUnsuccessfulResultWhenExpressionValueIsMandatoryButNull() {
        Coverage coverage = new Coverage();
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);

        RuntimeRule rule = TestRuleBuilder.getInstance()
                .targetPath("code")
                .usagePayload(UsageType.mandatory)
                .build();

        UsagePayloadResult payloadResult = (UsagePayloadResult) usagePayloadHandler.executePayload(
                rule.getPayload(),
                rule,
                dataContext,
                SESSION
        );

        assertThat(payloadResult.getSuccess(), is(false));
    }

    @Test
    public void executePayloadShouldReturnCustomErrorMessageWhenResultIsUnsuccessful() {
        Coverage coverage = new Coverage();
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);

        String errorMessageText = "The condition was not satisfied.";
        RuntimeRule rule = TestRuleBuilder.getInstance()
                .targetPath("code")
                .usagePayload(UsageType.mandatory, errorMessageText)
                .build();

        UsagePayloadResult payloadResult = (UsagePayloadResult) usagePayloadHandler.executePayload(
                rule.getPayload(),
                rule,
                dataContext,
                SESSION
        );

        assertThat(payloadResult.getMessage(), is(equalTo(errorMessageText)));
    }
}
