/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime.engine.dto.bundle.trace;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kraken.dimensions.DimensionSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import kraken.runtime.engine.core.EntryPointEvaluation;
import kraken.runtime.engine.dto.bundle.EntryPointBundle;
import kraken.runtime.model.rule.RuntimeRule;

/**
 * Unit tests for {@link EntryPointBundleBuildOperation} class.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
@RunWith(MockitoJUnitRunner.class)
public class EntryPointBundleBuildOperationTest {

    @Mock
    private EntryPointEvaluation entryPointEvaluation;

    @Mock
    private RuntimeRule firstRule;

    @Mock
    private RuntimeRule secondRule;

    @Test
    public void shouldCreateCorrectDescriptionForEntryPointEvaluation() {
        when(firstRule.getName()).thenReturn("First Rule");
        when(secondRule.getName()).thenReturn("Second Rule");
        when(entryPointEvaluation.getRules()).thenReturn(new ArrayList<>(List.of(firstRule, secondRule)));

        var entryPointOp = new EntryPointBundleBuildOperation("Entry Point", Set.of());

        assertThat(entryPointOp.describe(), is("Collecting rules for entry point 'Entry Point'"));
        assertThat(entryPointOp.describeAfter(new EntryPointBundle(entryPointEvaluation, Map.of(), "")),
            is("Collected rules for entry point 'Entry Point':"
                + System.lineSeparator()
                + "'First Rule',"
                + System.lineSeparator()
                + "'Second Rule'"));
    }

}
