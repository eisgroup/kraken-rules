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
package kraken.context.path;

import kraken.context.path.node.ContextPathNode;
import kraken.context.path.node.ContextPathNodeRepository;
import kraken.model.context.Cardinality;
import kraken.utils.dto.Pair;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@code ContextPathExtractor} class.
 *
 * @author Tomas Dapkunas
 * @since 1.1.1
 */
public class ContextPathExtractorTest {

    private static final String ROOT_CTX_NAME = "Policy";

    private ContextPathNodeRepository contextPathNodeRepository;

    @Before
    public void setUp() {
        contextPathNodeRepository = mock(ContextPathNodeRepository.class);
    }

    @Test
    public void shouldCollectPathsToAllNodesFromRoot() {
        prepareForTesting(List.of(
                new Pair<>("BasePolicy", new Pair<>(List.of(), List.of())),
                new Pair<>("BaseCoverage", new Pair<>(List.of(), List.of())),
                new Pair<>("RiskCoverage", new Pair<>(List.of("BaseCoverage"), List.of())),
                new Pair<>("Coverage", new Pair<>(List.of("BaseCoverage"), List.of("RiskCoverage"))),
                new Pair<>("RiskItem", new Pair<>(List.of(), List.of("Address"))),
                new Pair<>("ExtendedDetails", new Pair<>(List.of(), List.of("Address"))),
                new Pair<>("Address", new Pair<>(List.of(), List.of())),
                new Pair<>("Details", new Pair<>(List.of(), List.of("ExtendedDetails"))),
                new Pair<>(ROOT_CTX_NAME, new Pair<>(List.of("BasePolicy"), List.of("RiskItem", "Coverage", "Details")))));

        Map<String, Collection<ContextPath>> ctxPaths = ContextPathExtractor.create(contextPathNodeRepository, ROOT_CTX_NAME)
                .getAllPaths();
        List<String> ctxPathsAsString = ctxPaths.entrySet()
                .stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(contextPath -> entry.getKey() + " -> " + contextPath.getPathAsString()))
                .collect(Collectors.toList());

        assertThat(ctxPaths.size(), is(9));
        assertThat(ctxPathsAsString, containsInAnyOrder(
                "RiskItem -> Policy.RiskItem",
                "Policy -> Policy",
                "ExtendedDetails -> Policy.Details.ExtendedDetails",
                "Details -> Policy.Details",
                "Address -> Policy.RiskItem.Address",
                "Address -> Policy.Details.ExtendedDetails.Address",
                "RiskCoverage -> Policy.Coverage.RiskCoverage",
                "Coverage -> Policy.Coverage",
                "BaseCoverage -> Policy.Coverage",
                "BaseCoverage -> Policy.Coverage.RiskCoverage",
                "BasePolicy -> Policy"));
    }

    @Test
    public void shouldNotCreateRecursivePaths() {
        var a = new ContextPathNode(
                "A",
                List.of(),
                Map.of(
                        "B", createChild("B")
                )
        );
        var b = new ContextPathNode(
                "B",
                List.of(),
                Map.of(
                        "A", createChild("A")
                )
        );

        ContextPathNodeRepository repository = createRepository(a, b);
        List<ContextPath> pathsFor = ContextPathExtractor.create(repository, a.getName()).getPathsFor(b.getName());

        assertThat(pathsFor, hasSize(1));
        assertThat(pathsFor.get(0).getPath(), Matchers.contains("A", "B"));
    }

    @Test
    public void shouldNotCreateRecursivePathsWithInheritedContexts() {
        var a = new ContextPathNode(
                "A",
                List.of(),
                Map.of(
                        "B", createChild("B")
                )
        );
        var b = new ContextPathNode(
                "B",
                List.of("A"),
                Map.of()
        );

        ContextPathNodeRepository repository = createRepository(a, b);
        List<ContextPath> pathsFor = ContextPathExtractor.create(repository, a.getName()).getPathsFor(b.getName());

        assertThat(pathsFor, hasSize(1));
        assertThat(pathsFor.get(0).getPath(), Matchers.contains("A", "B"));
    }

    @Test
    public void shouldFindCycle() {
        var a = new ContextPathNode(
                "A",
                List.of(),
                Map.of(
                        "B", createChild("B")
                )
        );
        var b = new ContextPathNode(
                "B",
                List.of(),
                Map.of(
                        "C", createChild("C")
                )
        );
        var c = new ContextPathNode(
                "C",
                List.of(),
                Map.of(
                        "A", createChild("A")
                )
        );

        ContextPathNodeRepository repository = createRepository(a, b, c);
        ContextPathExtractor extractor = ContextPathExtractor.create(repository, a.getName());
        Collection<ContextPathExtractor.Cycle> cycles =  extractor.getAllCycles();

        assertThat(cycles, hasSize(1));
        Iterator<ContextPathExtractor.Cycle> iterator = cycles.iterator();
        assertThat(iterator.next().getNodeNames(), Matchers.contains("A", "B", "C"));
    }

    private ContextPathNodeRepository createRepository(ContextPathNode ...nodes) {
        ContextPathNodeRepository repository = mock(ContextPathNodeRepository.class);
        for (ContextPathNode node : nodes) {
            when(repository.get(node.getName())).thenReturn(node);
            when(repository.has(node.getName())).thenReturn(true);
        }
        return repository;
    }

    private ContextPathNode.ChildNode createChild(String name) {
        return new ContextPathNode.ChildNode(name, Cardinality.SINGLE);
    }

    private void prepareForTesting(List<Pair<String, Pair<List<String>, List<String>>>> parentChildNodes) {
        parentChildNodes.forEach(
                pair -> {
                    ContextPathNode node = createNode(pair.left, pair.right.left, pair.right.right);
                    Mockito.when(contextPathNodeRepository.get(node.getName())).thenReturn(node);
                    Mockito.when(contextPathNodeRepository.has(node.getName())).thenReturn(true);
                });
    }

    private ContextPathNode createNode(String name, Collection<String> inherited, Collection<String> children) {
        return new ContextPathNode(
                name,
                inherited,
                children.stream().collect(Collectors.toMap(child -> child, this::createChildNode))
        );
    }

    private ContextPathNode.ChildNode createChildNode(String name) {
        return new ContextPathNode.ChildNode(name, Cardinality.SINGLE);
    }
}
