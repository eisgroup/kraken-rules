/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.runtime.engine.handlers.trace;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import kraken.model.ValueList;
import kraken.model.payload.PayloadType;
import kraken.runtime.model.rule.payload.validation.ValueListPayload;

/**
 * @author Tomas Dapkunas
 * @since 1.43.0
 */
@RunWith(MockitoJUnitRunner.class)
public class ValueListPayloadEvaluatedOperationTest {

    @Mock
    private ValueListPayload valueListPayload;

    @Test
    public void shouldCreateCorrectDescriptionForFieldNoValue() {
        when(valueListPayload.getType()).thenReturn(PayloadType.VALUE_LIST);
        when(valueListPayload.getValueList()).thenReturn(ValueList.fromString(List.of("EUR")));

        ValueListPayloadEvaluatedOperation operation
            = new ValueListPayloadEvaluatedOperation(valueListPayload, null, true);

        assertThat(operation.describe(), is("Evaluated 'ValueListPayload' to 'true'. Field value 'null'"));
    }

    @Test
    public void shouldCreateCorrectDescriptionForFieldValueSuccess() {
        when(valueListPayload.getType()).thenReturn(PayloadType.VALUE_LIST);
        when(valueListPayload.getValueList()).thenReturn(ValueList.fromString(List.of("USD")));

        ValueListPayloadEvaluatedOperation operation
            = new ValueListPayloadEvaluatedOperation(valueListPayload, "USD", true);

        assertThat(operation.describe(), is("Evaluated 'ValueListPayload' to 'true'. Field value 'USD'"));
    }

    @Test
    public void shouldCreateCorrectDescriptionForFieldValueFailure() {
        when(valueListPayload.getType()).thenReturn(PayloadType.VALUE_LIST);
        when(valueListPayload.getValueList()).thenReturn(ValueList.fromString(List.of("LTL", "EUR")));

        ValueListPayloadEvaluatedOperation operation
            = new ValueListPayloadEvaluatedOperation(valueListPayload, "USD", false);

        assertThat(operation.describe(),
            is("Evaluated 'ValueListPayload' to 'false'. Field value 'USD' is not one of 'LTL, EUR'"));
    }

}
