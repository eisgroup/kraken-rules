/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
