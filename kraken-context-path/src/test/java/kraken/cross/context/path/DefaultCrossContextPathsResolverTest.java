/*
 *  Copyright 2020 EIS Ltd and/or one of its affiliates.
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
package kraken.cross.context.path;

import kraken.context.path.ContextPath;
import kraken.model.context.Cardinality;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.allOf;

/**
 * Unit tests for {@code CrossContextPathsResolver} class.
 *
 * @author Tomas Dapkunas
 * @since 1.1.1
 */
public class DefaultCrossContextPathsResolverTest {

    @Test
    public void shouldResolvePathFromPolicyToRiskItem() {
        Map<String, Collection<ContextPath>> paths = preparePaths(Map.of("Policy", List.of("Policy"), "RiskItem", List.of("Policy.RiskItem"),
                "Insured", List.of("Policy.RiskItem.Insured")));
        ContextCardinalityResolver cardinalityResolver = mock(ContextCardinalityResolver.class);
        Mockito.when(cardinalityResolver.getCardinality(anyString(), anyString())).thenReturn(Cardinality.SINGLE);

        DefaultCrossContextPathsResolver testObject = DefaultCrossContextPathsResolver.create(cardinalityResolver, paths);

        List<CrossContextPath> result = testObject.resolvePaths(new ContextPath.ContextPathBuilder("Policy")
                .build(), "RiskItem");

        assertThat(result.size(), is(1));
        assertThat(result, contains(allOf(
                Matchers.hasProperty("path", is(List.of("Policy", "RiskItem"))))
        ));
    }

    @Test
    public void shouldResolveClosestPathFromRiskItemToCoverage() {
        Map<String, Collection<ContextPath>> paths = preparePaths(Map.of("Policy", List.of("Policy"), "RiskItem", List.of("Policy.RiskItem"),
                "Insured", List.of("Policy.RiskItem.Insured"), "Coverage", List.of("Policy.RiskItem.Insured.Coverage", "Policy.RiskItem.Coverage")));
        ContextCardinalityResolver cardinalityResolver = mock(ContextCardinalityResolver.class);
        Mockito.when(cardinalityResolver.getCardinality(anyString(), anyString())).thenReturn(Cardinality.SINGLE);

        DefaultCrossContextPathsResolver testObject = DefaultCrossContextPathsResolver.create(cardinalityResolver, paths);

        List<CrossContextPath> result = testObject.resolvePaths(new ContextPath.ContextPathBuilder("Policy")
                .addPathElement("RiskItem").build(), "Coverage");

        assertThat(result.size(), is(1));
        assertThat(result, contains(allOf(
                Matchers.hasProperty("path", is(List.of("Policy", "RiskItem", "Coverage"))))
        ));
    }

    @Test
    public void shouldResolveClosestPathFromInsuredToDirectChildCoverage() {
        Map<String, Collection<ContextPath>> paths = preparePaths(Map.of("Policy", List.of("Policy"), "RiskItem", List.of("Policy.RiskItem"),
                "Insured", List.of("Policy.RiskItem.Insured"),
                "Other", List.of("Policy.RiskItem.Insured.Coverage.Other"),
                "Coverage", List.of("Policy.RiskItem.Insured.Coverage", "Policy.RiskItem.Insured.Coverage.Other.Coverage")));
        ContextCardinalityResolver cardinalityResolver = mock(ContextCardinalityResolver.class);
        Mockito.when(cardinalityResolver.getCardinality(anyString(), anyString())).thenReturn(Cardinality.SINGLE);

        DefaultCrossContextPathsResolver testObject = DefaultCrossContextPathsResolver.create(cardinalityResolver, paths);

        List<CrossContextPath> result = testObject.resolvePaths(new ContextPath.ContextPathBuilder("Policy")
                .addPathElement("RiskItem").addPathElement("Insured").build(), "Coverage");

        assertThat(result.size(), is(1));
        assertThat(result, contains(allOf(
                Matchers.hasProperty("path", is(List.of("Policy", "RiskItem", "Insured", "Coverage"))))
        ));
    }

    @Test
    public void shouldResolveClosestPathFromInsuredToOtherCoverage() {
        Map<String, Collection<ContextPath>> paths = preparePaths(Map.of("Policy", List.of("Policy"), "RiskItem", List.of("Policy.RiskItem"),
                "Insured", List.of("Policy.RiskItem.Insured"),
                "Other", List.of("Policy.RiskItem.Insured.Coverage.Other"),
                "Coverage", List.of("Policy.RiskItem.Coverage", "Policy.RiskItem.Insured.Other.Coverage")));
        ContextCardinalityResolver cardinalityResolver = mock(ContextCardinalityResolver.class);
        Mockito.when(cardinalityResolver.getCardinality(anyString(), anyString())).thenReturn(Cardinality.SINGLE);

        DefaultCrossContextPathsResolver testObject = DefaultCrossContextPathsResolver.create(cardinalityResolver, paths);

        List<CrossContextPath> result = testObject.resolvePaths(new ContextPath.ContextPathBuilder("Policy")
                .addPathElement("RiskItem").addPathElement("Insured").build(), "Coverage");

        assertThat(result.size(), is(1));
        assertThat(result, contains(allOf(
                Matchers.hasProperty("path", is(List.of("Policy", "RiskItem", "Insured", "Other", "Coverage"))))
        ));
    }

    @Test
    public void shouldResolveClosestPathToSelf() {
        Map<String, Collection<ContextPath>> paths = preparePaths(Map.of("Policy", List.of("Policy"), "RiskItem", List.of("Policy.RiskItem"),
                "Insured", List.of("Policy.RiskItem.Insured"),
                "Other", List.of("Policy.RiskItem.Insured.Coverage.Other"),
                "Coverage", List.of("Policy.RiskItem.Coverage", "Policy.RiskItem.Insured.Other.Coverage")));
        ContextCardinalityResolver cardinalityResolver = mock(ContextCardinalityResolver.class);
        Mockito.when(cardinalityResolver.getCardinality(anyString(), anyString())).thenReturn(Cardinality.SINGLE);

        DefaultCrossContextPathsResolver testObject = DefaultCrossContextPathsResolver.create(cardinalityResolver, paths);

        List<CrossContextPath> result = testObject.resolvePaths(new ContextPath.ContextPathBuilder("Policy")
                .addPathElement("RiskItem").addPathElement("Insured").build(), "Insured");

        assertThat(result.size(), is(1));
        assertThat(result, contains(allOf(
                Matchers.hasProperty("path", is(List.of("Policy", "RiskItem", "Insured"))))
        ));
    }

    private Map<String, Collection<ContextPath>> preparePaths(Map<String, List<String>> contextsAndPaths) {
        Map<String, Collection<ContextPath>> contextPaths = mock(Map.class);

        contextsAndPaths.forEach((key, value) -> {
            Mockito.when(contextPaths.get(key)).thenReturn(value.stream()
                    .map(pathString -> new ContextPath.ContextPathBuilder()
                            .addPathElements(asPathElements(pathString))
                            .build())
                    .collect(Collectors.toList()));
        });

        return contextPaths;
    }

    private List<String> asPathElements(String path) {
        return Arrays.asList(path.split("\\."));
    }

}
