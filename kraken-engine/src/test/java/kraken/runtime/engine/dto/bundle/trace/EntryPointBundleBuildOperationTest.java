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
