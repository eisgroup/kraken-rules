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
            is("Evaluated 'SizeRangePayload' to false. Expected size withing 0 and 1. Actual size is 2."));
    }

}
