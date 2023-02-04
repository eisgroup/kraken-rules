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
package kraken.runtime.engine.handlers.trace;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import kraken.model.payload.PayloadType;
import kraken.runtime.model.rule.payload.validation.RegExpPayload;

/**
 * Unit tests for {@code RegExpPayloadEvaluatedOperation} class.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
@RunWith(MockitoJUnitRunner.class)
public class RegExpPayloadEvaluatedOperationTest {

    @Mock
    private RegExpPayload regExpPayload;

    @Test
    public void shouldCreateCorrectDescriptionForRegExpMatch() {
        when(regExpPayload.getType()).thenReturn(PayloadType.REGEX);
        when(regExpPayload.getRegExp()).thenReturn("[A-Z]");

        var regExpPayloadOp = new RegExpPayloadEvaluatedOperation(regExpPayload, "A", true);

        assertThat(regExpPayloadOp.describe(),
            is("Evaluated 'RegExpPayload' to true. Value 'A' matches regular expression '[A-Z]'."));
    }

    @Test
    public void shouldCreateCorrectDescriptionForRegExpMismatch() {
        when(regExpPayload.getType()).thenReturn(PayloadType.REGEX);
        when(regExpPayload.getRegExp()).thenReturn("[A-Z]");

        var regExpPayloadOp = new RegExpPayloadEvaluatedOperation(regExpPayload, "1", false);

        assertThat(regExpPayloadOp.describe(),
            is("Evaluated 'RegExpPayload' to false. Value '1' does not match regular expression '[A-Z]'."));
    }

}
