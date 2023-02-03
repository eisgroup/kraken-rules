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
