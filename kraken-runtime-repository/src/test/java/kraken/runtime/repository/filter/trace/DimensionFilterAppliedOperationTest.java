/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime.repository.filter.trace;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import kraken.runtime.repository.filter.DimensionFilter;

/**
 * Unit tests for {@code DimensionFilterAppliedOperation} class.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
@RunWith(MockitoJUnitRunner.class)
public class DimensionFilterAppliedOperationTest {

    @Mock
    private DimensionFilter dimensionFilter;

    @Test
    public void shouldCreateCorrectDescriptionForDimensionFilterApplied() {

        var dimFilterOp = new DimensionFilterAppliedOperation(dimensionFilter, List.of("one", "two"), List.of("one"));

        assertThat(dimFilterOp.describe(),
            is("Dimension filter '" + dimensionFilter.getClass().getSimpleName() + "' applied."
                + " Versions before filter - 2, versions after filter - 1."));
    }

}
