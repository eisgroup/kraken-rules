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

import java.text.MessageFormat;

import kraken.TestRuleBuilder;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.result.LengthPayloadResult;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
import org.junit.Before;
import org.junit.Test;

import static kraken.runtime.engine.handlers.PayloadHandlerTestConstants.SESSION;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LengthPayloadHandlerTest {
    private LengthPayloadHandler lengthPayloadHandler;

    @Before
    public void setUp() {
        lengthPayloadHandler = new LengthPayloadHandler(new KrakenExpressionEvaluator());
    }

    @Test
    public void executePayloadShouldReturnInstanceOfLengthPayloadResult() {
        Coverage coverage = new Coverage();
        coverage.setCode("Q001");
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);
        RuntimeRule rule = TestRuleBuilder.getInstance()
                .lengthPayload(4)
                .targetPath("code")
                .build();

        PayloadResult payloadResult = lengthPayloadHandler.executePayload(
                rule.getPayload(),
                rule,
                dataContext,
                SESSION
        );

        assertThat(payloadResult, is(instanceOf(LengthPayloadResult.class)));
        assertThat(((LengthPayloadResult) payloadResult).getSuccess(), is(true));
    }

    @Test
    public void executePayloadShouldReturnUnsuccessfulResultWhenExpressionValueIsLongerThanMaxLength() {
        Coverage coverage = new Coverage();
        coverage.setCode("Q001");
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);

        String errorMessageText = MessageFormat.format(
                "The word must not contain more than {0} characters",
                3
        );
        RuntimeRule rule = TestRuleBuilder.getInstance()
                .lengthPayload(3, errorMessageText)
                .targetPath("code")
                .build();
        LengthPayloadResult payloadResult = (LengthPayloadResult) lengthPayloadHandler.executePayload(
                rule.getPayload(),
                rule,
                dataContext,
                SESSION
        );

        assertThat(payloadResult.getSuccess(), is(false));
        assertThat(payloadResult.getMessage(), is(equalTo(errorMessageText)));
    }
}
