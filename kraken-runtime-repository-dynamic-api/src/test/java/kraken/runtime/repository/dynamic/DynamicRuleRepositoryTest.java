/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.repository.dynamic;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import kraken.dimensions.DimensionSet;
import kraken.model.Rule;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DynamicRuleRepositoryTest {

    @Mock
    private Rule rule1;

    @Mock
    private Rule rule2;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void defaultImplResolvesDimensionSetsCorrectly() {
        DynamicRuleRepository repo = new MockRepository();
        var rules = repo.resolveDynamicRules(null, null, null).collect(Collectors.toList());
        assertEquals(2, rules.size());
        // first has empty set for static rule
        assertNotNull(rules.get(0).getDimensionSet());
        assertTrue(rules.get(0).getDimensionSet().isStatic());

        // second has null set for rule with unknown dimensions
        assertNotNull(rules.get(1).getDimensionSet());
        assertTrue(rules.get(1).getDimensionSet().isDimensional());
        assertEquals(DimensionSet.Variability.UNKNOWN, rules.get(1).getDimensionSet().getVariability());
    }

    private final class MockRepository implements DynamicRuleRepository {
        @Override
        public Stream<DynamicRuleHolder> resolveDynamicRules(String namespace, String entryPoint,
                                                             Map<String, Object> context) {
            when(rule1.isDimensional()).thenReturn(false);
            when(rule2.isDimensional()).thenReturn(true);
            return Stream.of(DynamicRuleHolder.createNonDimensional(rule1),
                DynamicRuleHolder.createUnknownDimensions(rule2));
        }
    }
}
