/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime.repository.filter.trace;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import kraken.runtime.model.Metadata;
import kraken.runtime.model.entrypoint.RuntimeEntryPoint;

/**
 * Unit tests for {@code EntryPointDimensionFilteringOperation} class.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
@RunWith(MockitoJUnitRunner.class)
public class EntryPointDimensionFilteringOperationTest {

    @Mock
    private RuntimeEntryPoint runtimeEntryPoint;

    @Mock
    private Metadata metadata;

    @Test
    public void shouldCreateCorrectDescriptionForEntryPointFiltering() {
        var dimensions = new HashMap<String, Object>();
        dimensions.put("dimKey", "dimValue");

        when(runtimeEntryPoint.getName()).thenReturn("Versioned Entry Point");
        when(metadata.getProperties()).thenReturn(dimensions);
        when(runtimeEntryPoint.getMetadata()).thenReturn(metadata);

        var dimFilterOp = new EntryPointDimensionFilteringOperation(List.of(runtimeEntryPoint));

        assertThat(dimFilterOp.describe(),
            is("Will apply dimension filters for entry point 'Versioned Entry Point' which has 1 version(s): "
                + System.lineSeparator() + "{\"dimKey\":\"dimValue\"}"));
    }

    @Test
    public void shouldCreateCorrectDescriptionForCompletedFilteringNoResults() {
        var dimFilterOp = new EntryPointDimensionFilteringOperation(null);

        assertThat(dimFilterOp.describeAfter(null),
            is("Entry point dimension filtering completed. All version were filtered out."));
    }

    @Test
    public void shouldCreateCorrectDescriptionForCompletedFilteringResult() {
        var dimensions = new HashMap<String, Object>();
        dimensions.put("dimKey", "dimValue");

        when(metadata.getProperties()).thenReturn(dimensions);
        when(runtimeEntryPoint.getMetadata()).thenReturn(metadata);

        var dimFilterOp = new EntryPointDimensionFilteringOperation(null);

        assertThat(dimFilterOp.describeAfter(runtimeEntryPoint),
            is("Entry point dimension filtering completed. Filtered version metadata: {\"dimKey\":\"dimValue\"}"));
    }

}
