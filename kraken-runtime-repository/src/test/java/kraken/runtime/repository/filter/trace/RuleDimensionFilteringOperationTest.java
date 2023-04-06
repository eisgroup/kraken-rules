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
        when(metadata.getProperties()).thenReturn(dimensions);
        when(ruleVersion.getMetadata()).thenReturn(metadata);

        var dimFilterOp = new RuleDimensionFilteringOperation(List.of(ruleVersion));

        assertThat(
            dimFilterOp.describe(),
            is("Applying dimension filters on rule 'Versioned rule' which has 1 version(s): "
                + System.lineSeparator() + "{\"dimKey\":\"dimValue\"}"
            )
        );
    }

    @Test
    public void shouldCreateCorrectDescriptionForCompletedFilteringNoResults() {
        var dimFilterOp = new RuleDimensionFilteringOperation(null);

        assertThat(
            dimFilterOp.describeAfter(null),
            is("Dimension filters applied. All version have been filtered out.")
        );
    }

    @Test
    public void shouldCreateCorrectDescriptionForCompletedFilteringResult() {
        var dimensions = new HashMap<String, Object>();
        dimensions.put("dimKey", "dimValue");

        when(metadata.getProperties()).thenReturn(dimensions);
        when(ruleVersion.getMetadata()).thenReturn(metadata);

        var dimFilterOp = new RuleDimensionFilteringOperation(null);

        assertThat(
            dimFilterOp.describeAfter(ruleVersion),
            is("Dimension filters applied. Remaining version: {\"dimKey\":\"dimValue\"}")
        );
    }

}
