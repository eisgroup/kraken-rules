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
import kraken.runtime.engine.result.AccessibilityPayloadResult;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.model.rule.payload.ui.AccessibilityPayload;
import org.junit.Test;

import static kraken.runtime.engine.handlers.PayloadHandlerTestConstants.SESSION;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class AccessibilityPayloadHandlerTest {
    @Test
    public void executePayloadShouldReturnAccessibilityPayloadResult() {
        AccessibilityPayloadHandler accessibilityPayloadHandler = new AccessibilityPayloadHandler();

        AccessibilityPayload accessibilityPayload = new AccessibilityPayload(true);

        PayloadResult payloadResult = accessibilityPayloadHandler.executePayload(
                accessibilityPayload,
                TestRuleBuilder.getInstance().build(),
                new DataContext(),
                SESSION
        );
        assertThat(payloadResult, is(instanceOf(AccessibilityPayloadResult.class)));
    }

    @Test
    public void executePayloadShouldSuccessfullyExecuteProvidedPayload() {
        AccessibilityPayloadHandler accessibilityPayloadHandler = new AccessibilityPayloadHandler();

        AccessibilityPayload presentationPayload = new AccessibilityPayload(true);

        AccessibilityPayloadResult payloadResult = (AccessibilityPayloadResult) accessibilityPayloadHandler.executePayload(
                presentationPayload,
                TestRuleBuilder.getInstance().build(),
                new DataContext(),
                SESSION
        );

        assertThat(payloadResult.getAccessible(), is(true));
    }
}
