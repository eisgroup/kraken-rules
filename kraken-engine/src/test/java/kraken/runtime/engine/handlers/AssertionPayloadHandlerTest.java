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

import static kraken.runtime.engine.handlers.PayloadHandlerTestConstants.SESSION;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import kraken.TestRuleBuilder;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.result.AssertionPayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;

public class AssertionPayloadHandlerTest {
    private AssertionPayloadHandler assertionPayloadHandler;

    @Before
    public void setUp() {
        assertionPayloadHandler = new AssertionPayloadHandler(new KrakenExpressionEvaluator());
    }

    @Test
    public void executePayloadShouldReturnSuccessfulResultWhenExpressionIsTrue() {
        Coverage coverage = new Coverage();
        coverage.setLevel(7);
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);

        final RuntimeRule rule = TestRuleBuilder.getInstance().assertionPayload("level > 5", List.of("Level: ", ""), List.of("level")).build();
        AssertionPayloadResult payloadResult = (AssertionPayloadResult) assertionPayloadHandler.executePayload(
                rule.getPayload(),
                rule,
                dataContext,
                SESSION
        );

        assertThat(payloadResult.getSuccess(), is(true));
        assertThat(payloadResult.getMessage(), is(equalTo("Level: 7")));
    }

    @Test
    public void executePayloadShouldReturnUnsuccessfulResultWhenExpressionIsFalse() {
        Coverage coverage = new Coverage();
        coverage.setLevel(3);
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);
        final RuntimeRule rule = TestRuleBuilder.getInstance().assertionPayload("level > 5", List.of("Level: ", ""), List.of("level")).build();
        AssertionPayloadResult payloadResult = (AssertionPayloadResult) assertionPayloadHandler.executePayload(
                rule.getPayload(),
                rule,
                dataContext,
                SESSION
        );

        assertThat(payloadResult.getSuccess(), is(false));
        assertThat(payloadResult.getMessage(), is(equalTo("Level: 3")));
    }
}
