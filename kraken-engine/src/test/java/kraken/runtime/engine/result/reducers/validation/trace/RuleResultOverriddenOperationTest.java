/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime.engine.result.reducers.validation.trace;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import kraken.runtime.engine.dto.OverrideInfo;
import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.runtime.engine.dto.RuleInfo;

/**
 * Unit tests for {@code RuleResultOverriddenOperation} class.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
@RunWith(MockitoJUnitRunner.class)
public class RuleResultOverriddenOperationTest {

    @Mock
    private RuleEvaluationResult ruleEvaluationResult;

    @Mock
    private RuleInfo ruleInfo;

    @Mock
    private OverrideInfo overrideInfo;

    @Test
    public void shouldCreateCorrectDescriptionForRuleOverride() {
        when(ruleInfo.getRuleName()).thenReturn("Overridden rule");

        when(ruleEvaluationResult.getOverrideInfo()).thenReturn(overrideInfo);
        when(ruleEvaluationResult.getRuleInfo()).thenReturn(ruleInfo);

        var overrideOp = new RuleResultOverriddenOperation(ruleEvaluationResult);

        assertThat(overrideOp.describe(),
            is("Rule 'Overridden rule' validation result is overridden. Original validation result will be ignored."
                + " Override info: {\"isOverridable\":false}"));
    }

}
