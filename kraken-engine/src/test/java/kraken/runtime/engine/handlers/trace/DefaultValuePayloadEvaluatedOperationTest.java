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
            is("Evaluated 'DefaultValuePayload'. Before value: 'NULL'. After value: 'updated'."));
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
