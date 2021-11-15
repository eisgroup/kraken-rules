/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
package kraken.model.dsl;

import static kraken.model.dsl.KrakenDSLModelParser.parseResource;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import kraken.model.entrypoint.EntryPoint;
import kraken.model.resource.Resource;
import kraken.model.resource.RuleImport;

/**
 * EntryPoint parsing test for {@link KrakenDSLModelParser} that verifies if {@link EntryPoint} are parsed correctly
 *
 * @author mulevicius
 */
public class KrakenModelDSLParserEntryPointTest {

    @Test
    public void shouldParseEmptyEntryPoint() {
        Resource model = parseResource("EntryPoints{EntryPoint 'ep1'{}}");

        assertThat(model.getEntryPoints(), hasSize(1));

        EntryPoint entryPoint = model.getEntryPoints().get(0);
        assertThat(entryPoint.getName(), equalTo("ep1"));
        assertThat(entryPoint.getRuleNames(), empty());
        assertThat(entryPoint.getEntryPointVariationId(), notNullValue());
    }

    @Test
    public void shouldParseEmptyEntryPointWithIncludeAndImports() {
        Resource model = parseResource("Namespace ENS Include NS Import Rule \"A\" from B Import Rule \"C\" from D EntryPoints{EntryPoint 'ep1'{}}");

        assertThat(model.getEntryPoints(), hasSize(1));
        assertThat(model.getNamespace(), equalTo("ENS"));
        assertThat(model.getIncludes(), hasItems("NS"));
        Map<String, RuleImport> referenceMap = model.getRuleImports().stream()
                .collect(Collectors.toMap(RuleImport::getNamespace, v -> v));

        Set<String> importedFromB = model.getRuleImports().stream()
                .filter(ri -> ri.getNamespace().equals("B"))
                .map(RuleImport::getRuleName)
                .collect(Collectors.toSet());

        Set<String> importedFromD = model.getRuleImports().stream()
                .filter(ri -> ri.getNamespace().equals("D"))
                .map(RuleImport::getRuleName)
                .collect(Collectors.toSet());

        assertThat(importedFromB, hasItems("A"));
        assertThat(importedFromD, hasItems("C"));

        EntryPoint entryPoint = model.getEntryPoints().get(0);
        assertThat(entryPoint.getName(), equalTo("ep1"));
        assertThat(entryPoint.getRuleNames(), empty());
    }

    @Test
    public void shouldParseEntryPoint() {
        Resource model = parseResource("EntryPoints{EntryPoint 'ep1'{ 'rule1', 'rule2'} " +
                "EntryPoint 'ep2' {'rule3'}}");

        assertThat(model.getEntryPoints(), hasSize(2));

        EntryPoint ep1 = model.getEntryPoints().get(0);
        assertThat(ep1.getName(), equalTo("ep1"));
        assertThat(ep1.getRuleNames(), hasItems("rule1", "rule2"));

        EntryPoint ep2 = model.getEntryPoints().get(1);
        assertThat(ep2.getName(), equalTo("ep2"));
        assertThat(ep2.getRuleNames(), hasItems("rule3"));
    }

    @Test
    public void shouldParseEntryPointFeatures() {
        Resource model = parseResource(
                "EntryPoints {"
                        + "@ServerSideOnly EntryPoint 'SS-EP1'{ 'rule1', EntryPoint 'EP1', 'rule2', EntryPoint 'EP2'} "
                        + "EntryPoint 'EP2' {EntryPoint 'EP1'}}");

        assertThat(model.getEntryPoints(), hasSize(2));

        EntryPoint ep1 = model.getEntryPoints().get(0);
        assertThat(ep1.isServerSideOnly(), is(true));
        assertThat(ep1.getName(), equalTo("SS-EP1"));

        EntryPoint ep2 = model.getEntryPoints().get(1);
        assertThat(ep2.isServerSideOnly(), is(false));
        assertThat(ep2.getName(), equalTo("EP2"));
    }

    @Test
    public void shouldParseAndCascadeEntryPointsFeatures() {
        Resource model = parseResource(
                "@ServerSideOnly EntryPoints {"
                        + "EntryPoint 'SS-EP1'{ 'rule1', EntryPoint 'EP1', 'rule2', EntryPoint 'EP2'} "
                        + "EntryPoint 'EP2' {EntryPoint 'EP1'}}");

        assertThat(model.getEntryPoints(), hasSize(2));

        EntryPoint ep1 = model.getEntryPoints().get(0);
        assertThat(ep1.isServerSideOnly(), is(true));
        assertThat(ep1.getName(), equalTo("SS-EP1"));

        EntryPoint ep2 = model.getEntryPoints().get(1);
        assertThat(ep2.isServerSideOnly(), is(true));
        assertThat(ep2.getName(), equalTo("EP2"));
    }

    @Test
    public void shouldParseEntryPointWithIncludes() {
        Resource model = parseResource(
                "EntryPoints{"
                        + "EntryPoint 'ep1'{ 'rule1', EntryPoint 'EP1', 'rule2', EntryPoint 'EP2'} "
                        + "EntryPoint 'ep2' {EntryPoint 'EP1'}}");

        assertThat(model.getEntryPoints(), hasSize(2));

        EntryPoint ep1 = model.getEntryPoints().get(0);
        assertThat(ep1.getName(), equalTo("ep1"));
        assertThat(ep1.getRuleNames(), hasItems("rule1", "rule2"));
        assertThat(ep1.getIncludedEntryPointNames(), hasItems("EP1", "EP2"));

        EntryPoint ep2 = model.getEntryPoints().get(1);
        assertThat(ep2.getName(), equalTo("ep2"));
        assertThat(ep2.getRuleNames(), hasSize(0));
        assertThat(ep2.getIncludedEntryPointNames(), hasItems("EP1"));
    }
}
