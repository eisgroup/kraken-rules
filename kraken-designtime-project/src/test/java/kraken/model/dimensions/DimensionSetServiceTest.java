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
package kraken.model.dimensions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import kraken.dimensions.DimensionSet;
import kraken.dimensions.DimensionSet.Variability;
import kraken.model.Metadata;
import kraken.model.Rule;
import kraken.model.dimensions.DimensionSetService;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;

public class DimensionSetServiceTest {

    private static final RulesModelFactory MODEL_FACTORY = RulesModelFactory.getInstance();

    @Test
    public void shouldResolveStaticDimensionSetForStaticRule() {
        Rule rule = baseRule();

        KrakenProject krakenProject = krakenProject(List.of(rule));

        DimensionSetService dimensionSetService = new DimensionSetService(krakenProject);
        DimensionSet dimensionSet = dimensionSetService.resolveRuleDimensionSet("namespace", rule);
        assertThat(dimensionSet.isStatic(), is(true));
        assertThat(dimensionSet.getDimensions(), empty());
    }

    @Test
    public void shouldResolveDimensionSetForDimensionalRule() {
        Rule dimensionalRule = ruleWithDimensions("Package");

        KrakenProject krakenProject = krakenProject(List.of(dimensionalRule));

        DimensionSetService dimensionSetService = new DimensionSetService(krakenProject);
        DimensionSet dimensionSet = dimensionSetService.resolveRuleDimensionSet("namespace", dimensionalRule);
        assertThat(dimensionSet.isDimensional(), is(true));
        assertThat(dimensionSet.getDimensions(), hasItem("Package"));
    }

    @Test
    public void shouldMergeDimensionSetsForAllRuleVersions() {
        Rule baseRule = baseRule();
        Rule dimensionalRule1 = ruleWithDimensions("Package");
        Rule dimensionalRule2 = ruleWithDimensions("Package", "State");

        KrakenProject krakenProject = krakenProject(List.of(baseRule, dimensionalRule1, dimensionalRule2));

        DimensionSetService dimensionSetService = new DimensionSetService(krakenProject);

        DimensionSet dimensionSet0 = dimensionSetService.resolveRuleDimensionSet("namespace", baseRule);
        assertThat(dimensionSet0.isDimensional(), is(true));
        assertThat(dimensionSet0.getDimensions(), hasItems("Package", "State"));

        DimensionSet dimensionSet1 = dimensionSetService.resolveRuleDimensionSet("namespace", dimensionalRule1);
        assertThat(dimensionSet1.isDimensional(), is(true));
        assertThat(dimensionSet1.getDimensions(), hasItems("Package", "State"));

        DimensionSet dimensionSet2 = dimensionSetService.resolveRuleDimensionSet("namespace", dimensionalRule2);
        assertThat(dimensionSet2.isDimensional(), is(true));
        assertThat(dimensionSet2.getDimensions(), hasItems("Package", "State"));
    }

    @Test
    public void shouldResolveUnknownDimensionSetForDimensionalByUnknownDimensionsRule() {
        Rule dimensionalRule = ruleWithDimensions("Package");

        KrakenProject krakenProject = krakenProject(List.of(dimensionalRule));

        DimensionSetService dimensionSetService = new DimensionSetService(
            krakenProject,
            (namespace, metadataAwareKrakenItem) -> DimensionSet.createForUnknownDimensions()
        );

        DimensionSet dimensionSet = dimensionSetService.resolveRuleDimensionSet("namespace", dimensionalRule);
        assertThat(dimensionSet.isDimensional(), is(true));
        assertThat(dimensionSet.getVariability(), is(Variability.UNKNOWN));
        assertThat(dimensionSet.getDimensions(), nullValue());
    }

    @Test
    public void shouldMergeDimensionSetsForAllRuleVersionsWithUnknownDimensionsAsPriority() {
        Rule baseRule = baseRule();
        Rule dimensionalRule1 = ruleWithDimensions("Package");
        Rule dimensionalRule2 = ruleWithDimensions("Package", "Unknown");

        KrakenProject krakenProject = krakenProject(List.of(baseRule, dimensionalRule1, dimensionalRule2));

        DimensionSetService dimensionSetService = new DimensionSetService(
            krakenProject,
            (namespace, metadataAwareKrakenItem) -> {
                if(metadataAwareKrakenItem.getMetadata() == null) {
                    return DimensionSet.createStatic();
                }
                if(metadataAwareKrakenItem.getMetadata().hasProperty("Unknown")) {
                    return DimensionSet.createForUnknownDimensions();
                }
                return DimensionSet.createForDimensions(metadataAwareKrakenItem.getMetadata().asMap().keySet());
            }
        );

        DimensionSet dimensionSet0 = dimensionSetService.resolveRuleDimensionSet("namespace", baseRule);
        assertThat(dimensionSet0.isDimensional(), is(true));
        assertThat(dimensionSet0.getVariability(), is(Variability.UNKNOWN));
        assertThat(dimensionSet0.getDimensions(), nullValue());

        DimensionSet dimensionSet1 = dimensionSetService.resolveRuleDimensionSet("namespace", dimensionalRule1);
        assertThat(dimensionSet1.isDimensional(), is(true));
        assertThat(dimensionSet1.getVariability(), is(Variability.UNKNOWN));
        assertThat(dimensionSet1.getDimensions(), nullValue());

        DimensionSet dimensionSet2 = dimensionSetService.resolveRuleDimensionSet("namespace", dimensionalRule2);
        assertThat(dimensionSet2.isDimensional(), is(true));
        assertThat(dimensionSet2.getVariability(), is(Variability.UNKNOWN));
        assertThat(dimensionSet2.getDimensions(), nullValue());
    }

    private Rule baseRule() {
        return ruleWithDimensions();
    }

    private Rule ruleWithDimensions(String... dimensions) {
        Rule rule = MODEL_FACTORY.createRule();
        rule.setName("ruleName");
        rule.setPhysicalNamespace("namespace");

        Metadata metadata = MODEL_FACTORY.createMetadata();
        for(String dimension : dimensions) {
            metadata.setProperty(dimension, "value");
        }
        rule.setMetadata(metadata);

        return rule;
    }

    private KrakenProject krakenProject(List<Rule> ruleVersions) {
        KrakenProject krakenProject = mock(KrakenProject.class);
        when(krakenProject.getNamespace()).thenReturn("namespace");
        when(krakenProject.getRuleVersions()).thenReturn(Map.of("ruleName", ruleVersions));
        return krakenProject;
    }

}
