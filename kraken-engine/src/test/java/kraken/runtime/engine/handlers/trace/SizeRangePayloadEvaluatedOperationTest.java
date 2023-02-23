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

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import kraken.model.payload.PayloadType;
import kraken.runtime.model.rule.payload.validation.SizeRangePayload;

/**
 * Unit tests for {@code SizeRangePayloadEvaluatedOperation} class.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
@RunWith(MockitoJUnitRunner.class)
public class SizeRangePayloadEvaluatedOperationTest {

    @Mock
    private SizeRangePayload sizePayload;

    @Test
    public void shouldCreateCorrectDescriptionForSuccessSizeRangePayloadEvaluation() {
        when(sizePayload.getType()).thenReturn(PayloadType.SIZE_RANGE);

        var sizeRangePayloadOp = new SizeRangePayloadEvaluatedOperation(sizePayload, List.of("val"), true);

        assertThat(sizeRangePayloadOp.describe(),
            is("Evaluated 'SizeRangePayload' to true. Collection field size is within expected range."));
    }

    @Test
    public void shouldCreateCorrectDescriptionForFailedSizeRangePayloadEvaluation() {
        when(sizePayload.getMax()).thenReturn(1);
        when(sizePayload.getType()).thenReturn(PayloadType.SIZE_RANGE);

        var sizeRangePayloadOp = new SizeRangePayloadEvaluatedOperation(sizePayload, List.of("val1", "val2"), false);

        assertThat(sizeRangePayloadOp.describe(),
            is("Evaluated 'SizeRangePayload' to false. Expected size within 0 and 1. Actual size is 2."));
    }

}
