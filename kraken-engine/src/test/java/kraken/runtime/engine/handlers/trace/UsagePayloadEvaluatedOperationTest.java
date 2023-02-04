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
import kraken.model.validation.UsageType;
import kraken.runtime.model.rule.payload.validation.UsagePayload;

/**
 * Unit tests for {@code UsagePayloadEvaluatedOperation} class.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
@RunWith(MockitoJUnitRunner.class)
public class UsagePayloadEvaluatedOperationTest {

    @Mock
    private UsagePayload usagePayload;

    @Test
    public void shouldCreateCorrectDescriptionForMandatoryFieldWithNoValue() {
        when(usagePayload.getUsageType()).thenReturn(UsageType.mandatory);
        when(usagePayload.getType()).thenReturn(PayloadType.USAGE);

        var usagePayloadOp = new UsagePayloadEvaluatedOperation(usagePayload, null, false);

        assertThat(usagePayloadOp.describe(),
            is("Evaluated 'UsagePayload' to false. Mandatory field value is missing."));
    }

    @Test
    public void shouldCreateCorrectDescriptionForMandatoryFieldWithValue() {
        when(usagePayload.getUsageType()).thenReturn(UsageType.mandatory);
        when(usagePayload.getType()).thenReturn(PayloadType.USAGE);

        var usagePayloadOp = new UsagePayloadEvaluatedOperation(usagePayload, "stringValue", true);

        assertThat(usagePayloadOp.describe(),
            is("Evaluated 'UsagePayload' to true. Mandatory field has value 'stringValue'."));
    }

    @Test
    public void shouldCreateCorrectDescriptionForEmptyFieldWithNoValue() {
        when(usagePayload.getUsageType()).thenReturn(UsageType.mustBeEmpty);
        when(usagePayload.getType()).thenReturn(PayloadType.USAGE);

        var usagePayloadOp = new UsagePayloadEvaluatedOperation(usagePayload, null, true);

        assertThat(usagePayloadOp.describe(),
            is("Evaluated 'UsagePayload' to true. Empty field has no value set."));
    }

    @Test
    public void shouldCreateCorrectDescriptionForEmptyFieldWithValue() {
        when(usagePayload.getUsageType()).thenReturn(UsageType.mustBeEmpty);
        when(usagePayload.getType()).thenReturn(PayloadType.USAGE);

        var usagePayloadOp = new UsagePayloadEvaluatedOperation(usagePayload, "stringValue", false);

        assertThat(usagePayloadOp.describe(),
            is("Evaluated 'UsagePayload' to false. Empty field has value 'stringValue'."));
    }


}
