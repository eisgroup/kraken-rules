/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime.engine.trace;

import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import kraken.runtime.EvaluationConfig;
import kraken.runtime.engine.EntryPointResult;

/**
 * Unit tests for {@link RuleEngineInvocationOperation} class.
 *
 * @author kjuraityte
 * @since 1.40.0
 */
@RunWith(MockitoJUnitRunner.class)
public class RuleEngineInvocationOperationTest {

    @Mock
    private Object rootNode;
    @Mock
    private EvaluationConfig evaluationConfig;
    @Mock
    private EntryPointResult result;

    @Test
    public void shouldCreateCorrectDescriptionForRuleEngineInvocation() {
        var ruleEngineInvocationPointOp = new RuleEngineInvocationOperation("Entry Point", rootNode, evaluationConfig);
        assertThat(
            ruleEngineInvocationPointOp.describe(),
            containsString("Rule engine called to evaluate entry point 'Entry Point'. "
                + "DimensionSetResolver: kraken.model.dimensions.DefaultDimensionSetResolver. Data and configuration:")
        );
    }

    @Test
    public void shouldCreateCorrectDescriptionForCompletedRuleEngineCall() {
        var ruleEngineInvocationPointOp = new RuleEngineInvocationOperation("Entry Point", rootNode, evaluationConfig);
        when(result.getEvaluationTimeStamp()).thenReturn(LocalDateTime.parse("2022-11-11T10:15:30"));
        assertThat(
            ruleEngineInvocationPointOp.describeAfter(result),
            is("Rule engine call completed. Entry point evaluation timestamp '2022-11-11T10:15:30'")
        );
    }
}
