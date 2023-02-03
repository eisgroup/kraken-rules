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
