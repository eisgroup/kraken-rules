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
