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
import kraken.model.validation.SizeOrientation;
import kraken.runtime.model.rule.payload.validation.SizePayload;

/**
 * Trace specific unit tests for {@code SizePayloadHandler} class.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
@RunWith(MockitoJUnitRunner.class)
public class SizePayloadEvaluatedOperationTest {

    @Mock
    private SizePayload sizePayload;

    @Test
    public void shouldCreateCorrectDescriptionForSuccessSizePayloadEvaluation() {
        var value = List.of("sizeValue");

        when(sizePayload.getType()).thenReturn(PayloadType.SIZE);
        when(sizePayload.getOrientation()).thenReturn(SizeOrientation.MIN);
        when(sizePayload.getSize()).thenReturn(1);

        var sizePayloadOp = new SizePayloadEvaluatedOperation(sizePayload, value, true);

        assertThat(sizePayloadOp.describe(), is("Evaluated 'SizePayload' to true. "
            + "Expected size no less than 1 - actual size is 1."));
    }

    @Test
    public void shouldCreateCorrectDescriptionForFailedSizePayloadEvaluation() {
        var value = List.of("sizeValue", "other");

        when(sizePayload.getType()).thenReturn(PayloadType.SIZE);
        when(sizePayload.getOrientation()).thenReturn(SizeOrientation.MIN);
        when(sizePayload.getSize()).thenReturn(1);

        var sizePayloadOp = new SizePayloadEvaluatedOperation(sizePayload, value, false);

        assertThat(sizePayloadOp.describe(), is("Evaluated 'SizePayload' to false. "
            + "Expected size no less than 1 - actual size is 2."));
    }

}
