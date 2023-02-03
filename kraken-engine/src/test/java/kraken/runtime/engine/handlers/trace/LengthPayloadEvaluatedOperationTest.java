/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
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
import kraken.runtime.model.rule.payload.validation.LengthPayload;

/**
 * Unit tests for {@code LengthPayloadEvaluatedOperation} class.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
@RunWith(MockitoJUnitRunner.class)
public class LengthPayloadEvaluatedOperationTest {

    @Mock
    private LengthPayload lengthPayload;

    @Test
    public void shouldCreateCorrectDescriptionForFailedLengthEvaluation() {
        when(lengthPayload.getLength()).thenReturn(10);
        when(lengthPayload.getType()).thenReturn(PayloadType.LENGTH);

        var lengthPayloadOp = new LengthPayloadEvaluatedOperation(lengthPayload, "failingString", false);

        assertThat(lengthPayloadOp.describe(),
            is("Evaluated 'LengthPayload' to false. Expected length '10'. Actual length '13'"));
    }

    @Test
    public void shouldCreateCorrectDescriptionForSuccessLengthEvaluation() {
        when(lengthPayload.getLength()).thenReturn(100);
        when(lengthPayload.getType()).thenReturn(PayloadType.LENGTH);

        var lengthPayloadOp = new LengthPayloadEvaluatedOperation(lengthPayload, "passingString", true);

        assertThat(lengthPayloadOp.describe(),
            is("Evaluated 'LengthPayload' to true. Expected length '100'. Actual length '13'"));
    }

}
