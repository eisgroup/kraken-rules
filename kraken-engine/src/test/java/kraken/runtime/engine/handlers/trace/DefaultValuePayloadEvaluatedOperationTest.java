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
import kraken.runtime.model.rule.payload.derive.DefaultValuePayload;

/**
 * Unit tests for {@code DefaultValuePayloadEvaluatedOperation} class.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultValuePayloadEvaluatedOperationTest {

    @Mock
    private DefaultValuePayload defaultValuePayload;

    @Test
    public void shouldCreateCorrectDescriptionForDefaultValueWhenNull() {
        when(defaultValuePayload.getType()).thenReturn(PayloadType.DEFAULT);

        var defaultValuePayloadOp =
            new DefaultValuePayloadEvaluatedOperation(defaultValuePayload, null, "updated");

        assertThat(defaultValuePayloadOp.describe(),
            is("Evaluated 'DefaultValuePayload'. Before value: ''. After value: 'updated'."));
    }

    @Test
    public void shouldCreateCorrectDescriptionForDefaultValuePayloadType() {
        when(defaultValuePayload.getType()).thenReturn(PayloadType.DEFAULT);

        var defaultValuePayloadOp =
            new DefaultValuePayloadEvaluatedOperation(defaultValuePayload, "original", "updated");

        assertThat(defaultValuePayloadOp.describe(),
            is("Evaluated 'DefaultValuePayload'. Before value: 'original'. After value: 'updated'."));
    }

}
