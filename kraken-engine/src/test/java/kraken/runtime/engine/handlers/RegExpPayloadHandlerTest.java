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
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.engine.result.RegExpPayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
import org.junit.Before;
import org.junit.Test;

import static kraken.runtime.engine.handlers.PayloadHandlerTestConstants.SESSION;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RegExpPayloadHandlerTest {
    private RegExpPayloadHandler regExpPayloadHandler;

    @Before
    public void setUp() {
        regExpPayloadHandler = new RegExpPayloadHandler(new KrakenExpressionEvaluator());
    }

    @Test
    public void executePayloadShouldReturnRegExpPayloadResult() {
        Coverage coverage = new Coverage();
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);

        RuntimeRule rule = TestRuleBuilder.getInstance()
                .targetPath("code")
                .regexpPayload("foo")
                .build();

        PayloadResult payloadResult = regExpPayloadHandler.executePayload(
                rule.getPayload(),
                rule,
                dataContext,
                SESSION
        );

        assertThat(payloadResult, instanceOf(RegExpPayloadResult.class));
        assertThat(((RegExpPayloadResult) payloadResult).getSuccess(), is(true));
    }

    @Test
    public void executePayloadShouldReturnSuccessfulResultWhenExpressionValueMatchesRegularExpression() {
        Coverage coverage = new Coverage();
        coverage.setCode("555987");
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);

        RuntimeRule rule = TestRuleBuilder.getInstance()
                .targetPath("code")
                // The following regular expression matches a sequence of digits that start with a sequence "555".
                .regexpPayload("^5{3}\\d+")
                .build();

        RegExpPayloadResult payloadResult = (RegExpPayloadResult) regExpPayloadHandler.executePayload(
                rule.getPayload(),
                rule,
                dataContext,
                SESSION
        );

        assertThat(payloadResult.getSuccess(), is(true));
    }

    @Test
    public void executePayloadShouldReturnUnsuccessfulResultWhenExpressionValueDoesNotMatchRegularExpression() {
        Coverage coverage = new Coverage();
        coverage.setCode("847");
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);

        String errorMessageText = "Digit sequence must start with the digit 9.";
        RuntimeRule rule = TestRuleBuilder.getInstance()
                .targetPath("code")
                // The following regular expression matches a sequence of digits that start with the digit 9.
                .regexpPayload("^9\\d+", errorMessageText)
                .build();

        RegExpPayloadResult payloadResult = (RegExpPayloadResult) regExpPayloadHandler.executePayload(
                rule.getPayload(),
                rule,
                dataContext,
                SESSION
        );

        assertThat(payloadResult.getSuccess(), is(false));
        assertThat(payloadResult.getMessage(), is(equalTo(errorMessageText)));
    }
}
