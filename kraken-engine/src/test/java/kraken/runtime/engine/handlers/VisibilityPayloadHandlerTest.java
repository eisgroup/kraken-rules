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
import kraken.runtime.engine.result.VisibilityPayloadResult;
import kraken.runtime.model.rule.RuntimeRule;
import org.junit.Test;

import static kraken.runtime.engine.handlers.PayloadHandlerTestConstants.SESSION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class VisibilityPayloadHandlerTest {

    @Test
    public void shouldReturnNonVisibleResult() {
        VisibilityPayloadHandler visibilityPayloadHandler = new VisibilityPayloadHandler();

        RuntimeRule rule = TestRuleBuilder.getInstance()
                .targetPath("code")
                .notVisible()
                .build();
        VisibilityPayloadResult payloadResult = (VisibilityPayloadResult) visibilityPayloadHandler.executePayload(
                rule.getPayload(),
                rule,
                new DataContext(),
                SESSION
        );

        assertThat(payloadResult.getVisible(), is(false));
    }
}
