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

import kraken.dimensions.DimensionSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import kraken.runtime.model.Metadata;
import kraken.runtime.model.rule.RuntimeRule;

/**
 * Unit tests for {@code RuleDimensionFilteringOperation} class.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
@RunWith(MockitoJUnitRunner.class)
public class RuleDimensionFilteringOperationTest {

    @Mock
    private RuntimeRule ruleVersion;

    @Mock
    private Metadata metadata;

    @Test
    public void shouldCreateCorrectDescriptionRuleFiltering() {
        var dimensions = new HashMap<String, Object>();
        dimensions.put("dimKey", "dimValue");

        when(ruleVersion.getName()).thenReturn("Versioned rule");
        when(ruleVersion.getDimensionSet()).thenReturn(DimensionSet.createForUnknownDimensions());
        when(metadata.getProperties()).thenReturn(dimensions);
        when(ruleVersion.getMetadata()).thenReturn(metadata);

        var dimFilterOp = new RuleDimensionFilteringOperation(List.of(ruleVersion));

        assertThat(dimFilterOp.describe(),
            is("Will apply dimension filters for rule 'Versioned rule' which has 1 version(s): "
                + System.lineSeparator() + "{\"dimKey\":\"dimValue\"}"));
    }

    @Test
    public void shouldCreateCorrectDescriptionForCompletedFilteringNoResults() {
        var dimFilterOp = new RuleDimensionFilteringOperation(null);

        assertThat(dimFilterOp.describeAfter(null),
            is("Rule dimension filtering completed. All version were filtered out."));
    }

    @Test
    public void shouldCreateCorrectDescriptionForCompletedFilteringResult() {
        var dimensions = new HashMap<String, Object>();
        dimensions.put("dimKey", "dimValue");

        when(metadata.getProperties()).thenReturn(dimensions);
        when(ruleVersion.getMetadata()).thenReturn(metadata);

        var dimFilterOp = new RuleDimensionFilteringOperation(null);

        assertThat(dimFilterOp.describeAfter(ruleVersion),
            is("Rule dimension filtering completed. Filtered version metadata: {\"dimKey\":\"dimValue\"}"));
    }

}
