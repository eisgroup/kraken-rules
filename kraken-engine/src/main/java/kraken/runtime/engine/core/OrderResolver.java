/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.engine.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import kraken.runtime.KrakenRuntimeException;
import kraken.runtime.model.context.ContextNavigation;
import kraken.runtime.model.rule.Dependency;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.repository.RuntimeContextRepository;

/**
 *
 * @author mulevicius
 * @since 1.40.0
 */
public class OrderResolver {

    private final RuntimeContextRepository contextRepository;

    // all leaf context definitions accessible from root context definition as a child
    private final Set<String> leafContextDefinitions = new HashSet<>();

    // a map of supertypes for each context definition
    // for example, if key is Coverage, then values will be COLLCoverage, MEDCoverage, ...
    // key can be a leaf type, in which case value set will contain itself
    private final Map<String, Set<String>> leafTypesOfSupertype = new HashMap<>();

    public OrderResolver(RuntimeContextRepository contextRepository) {
        this.contextRepository = contextRepository;
        this.collectLeafContextDefinitions(contextRepository.getRootContextDefinition().getName());
        for(var contextDefinitionName : contextRepository.getAllContextDefinitionNames()) {
            var leafTypes = new HashSet<String>();
            for(var leafDefinitionName : leafContextDefinitions) {
                var leafContextDefinition = contextRepository.getContextDefinition(leafDefinitionName);
                if(leafContextDefinition.getName().equals(contextDefinitionName)
                    || leafContextDefinition.getInheritedContexts().contains(contextDefinitionName)) {
                    leafTypes.add(leafDefinitionName);
                }
            }
            leafTypesOfSupertype.put(contextDefinitionName, leafTypes);
        }
    }

    /**
     *
     * @return ordered fields according to dependencies between defaulting rules
     */
    public List<Field> resolveOrderedFields(Collection<RuntimeRule> defaultRules) {
        var graph = buildFieldGraph(defaultRules);

        throwIfGraphHasCycle(defaultRules, graph);

        return orderFields(graph);
    }

    private Graph<Field, DefaultEdge> buildFieldGraph(Collection<RuntimeRule> defaultRules) {
        var graph = new DirectedMultigraph<Field, DefaultEdge>(DefaultEdge.class);
        for(RuntimeRule rule : defaultRules) {
            leafTypesOfSupertype.get(rule.getContext()).stream()
                .map(leaf -> new Field(leaf, rule.getTargetPath()))
                .forEach(graph::addVertex);
        }

        for(RuntimeRule rule : defaultRules) {
            Set<Field> leafRuleTargetNodes = leafTypesOfSupertype.get(rule.getContext()).stream()
                .map(leaf -> new Field(leaf, rule.getTargetPath()))
                .collect(Collectors.toSet());

            Set<Field> leafDependencyNodes = rule.getDependencies().stream()
                .filter(dependency -> dependency.getFieldName() != null)
                .filter(dependency -> !dependsOnTargetField(rule, dependency))
                .flatMap(dependency -> leafTypesOfSupertype.get(dependency.getContextName()).stream()
                    .map(leaf -> new Field(leaf, dependency.getFieldName())))
                .filter(graph::containsVertex)
                .collect(Collectors.toSet());

            for(Field ruleTargetNode : leafRuleTargetNodes) {
                for(Field dependencyNode : leafDependencyNodes) {
                    // graph edge goes from dependency node to rule target node because dependencies
                    // must be defaulted first before rule on target field can be evaluated
                    if(!ruleTargetNode.equals(dependencyNode)) {
                        graph.addEdge(dependencyNode, ruleTargetNode);
                    }
                }
            }
        }
        return graph;
    }

    private boolean dependsOnTargetField(RuntimeRule rule, Dependency dependency) {
        return dependency.isSelfDependency()
            && dependency.getFieldName() != null
            && dependency.getFieldName().equals(rule.getTargetPath());
    }

    private List<Field> orderFields(Graph<Field, DefaultEdge> graph) {
        var iterator = new TopologicalOrderIterator<>(graph);
        var orderedFields = new ArrayList<Field>();
        while (iterator.hasNext()) {
            orderedFields.add(iterator.next());
        }
        return orderedFields;
    }

    private void throwIfGraphHasCycle(Collection<RuntimeRule> defaultRules, Graph<Field, DefaultEdge> graph) {
        var cycle = new CycleDetector<>(graph).findCycles();
        if(!cycle.isEmpty()) {
            String fieldsInCycle = cycle.stream()
                .map(a -> a.contextName + "." + a.contextField)
                .distinct()
                .collect(Collectors.joining(", "));
            String involvedRules = cycle.stream()
                .flatMap(a -> defaultRules.stream()
                    .filter(r -> r.getTargetPath().equals(a.contextField))
                    .filter(r -> leafTypesOfSupertype.get(r.getContext()).contains(a.contextName)))
                .map(RuntimeRule::getName)
                .distinct()
                .collect(Collectors.joining(", "));

            var messageTemplate = "Cycle detected between fields: %s. Involved rules are: %s";
            throw new KrakenRuntimeException(String.format(messageTemplate, fieldsInCycle, involvedRules));
        }
    }

    private void collectLeafContextDefinitions(String root) {
        if(leafContextDefinitions.contains(root)) {
            return;
        }

        leafContextDefinitions.add(root);

        for(ContextNavigation child : contextRepository.getContextDefinition(root).getChildren().values()) {
            collectLeafContextDefinitions(child.getTargetName());
        }
    }

    public static class Field {
        private final String contextName;
        private final String contextField;

        public Field(String contextName, String contextField) {
            this.contextName = contextName;
            this.contextField = contextField;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Field field = (Field) o;

            return Objects.equals(contextName, field.contextName) &&
                    Objects.equals(contextField, field.contextField);
        }

        @Override
        public int hashCode() {
            return Objects.hash(contextName, contextField);
        }

        @Override
        public String toString() {
            return contextName + "." + contextField;
        }

        public String getContextName() {
            return contextName;
        }

        public String getContextField() {
            return contextField;
        }
    }

}
