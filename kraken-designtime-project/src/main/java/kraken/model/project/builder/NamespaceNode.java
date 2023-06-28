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
package kraken.model.project.builder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import kraken.el.functionregistry.FunctionHeader;
import kraken.model.Dimension;
import kraken.model.Function;
import kraken.model.FunctionSignature;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.project.exception.IllegalKrakenProjectStateException;
import kraken.namespace.Namespaced;

/**
 * Represents a single node in the namespace tree that represents all resources defined in this namespace.
 * </p>
 * Node contains children which are other namespaces that are included.
 *
 * @author mulevicius
 */
public final class NamespaceNode {

    private final String name;

    private final Set<NamespaceNode> children;

    private final NamespacedResource namespacedResource;

    NamespaceNode(String name, Set<NamespaceNode> children, NamespacedResource namespacedResource) {
        this.name = name;
        this.children = children;
        this.namespacedResource = namespacedResource;
    }

    /**
     * @return the name of the namespace represented by this node.
     */
    String getName() {
        return name;
    }

    /**
     * @return a collection of namespaces that include this namespace.
     */
    Collection<NamespaceNode> getChildren() {
        return Collections.unmodifiableCollection(children);
    }

    /**
     * Adds the provided node as a child.
     *
     * @param node the node to add.
     */
    void addChild(NamespaceNode node) {
        children.add(node);
    }

    /**
     *
     * @return projection that contains all Kraken models applicable in this namespace after processing namespace includes
     */
    NamespaceProjection buildNamespaceProjection() {
        NamespaceProjection namespaceProjection = new NamespaceProjection(name);

        addLocalNamespaceResourceItemsToProjection(namespaceProjection);
        addIncludedNamespaceResourceItemsToProjection(namespaceProjection);
        validateNamespaceProjectionForIncludeAmbiguities(namespaceProjection);

        return namespaceProjection;
    }

    private void addIncludedNamespaceResourceItemsToProjection(NamespaceProjection namespaceProjection) {
        boolean hasNoExternalContext = namespaceProjection.getExternalContexts().isEmpty();

        for(NamespaceNode child : children) {
            NamespaceProjection childNamespaceProjection = child.buildNamespaceProjection();
            childNamespaceProjection.getContextDefinitions().stream()
                    .filter(c -> !namespacedResource.getContextDefinitions().containsKey(c.getName()))
                    .forEach(c -> namespaceProjection.getContextDefinitions().add(c));
            childNamespaceProjection.getEntryPoints().stream()
                    .filter(ep -> !namespacedResource.getEntryPoints().containsKey(ep.getName()))
                    .forEach(ep -> namespaceProjection.getEntryPoints().add(ep));
            childNamespaceProjection.getRules().stream()
                    .filter(r -> !namespacedResource.getRules().containsKey(r.getName()))
                    .forEach(r -> namespaceProjection.getRules().add(r));
            childNamespaceProjection.getExternalContextDefinitions()
                    .stream()
                    .filter(extDef -> !namespacedResource.getExternalContextDefinitions().containsKey(extDef.getName()))
                    .forEach(extDef -> namespaceProjection.getExternalContextDefinitions().add(extDef));
            childNamespaceProjection.getFunctionSignatures().entrySet().stream()
                    .filter(e -> !namespacedResource.getFunctionSignatures().containsKey(e.getKey()))
                    .forEach(e -> namespaceProjection.getFunctionSignatures().put(e.getKey(), e.getValue()));
            childNamespaceProjection.getFunctions().entrySet().stream()
                    .filter(e -> !namespacedResource.getFunctions().containsKey(e.getKey()))
                    .forEach(e -> namespaceProjection.getFunctions().put(e.getKey(), e.getValue()));
            childNamespaceProjection.getDimensions().stream()
                .filter(dimension -> !namespacedResource.getDimensions().containsKey(dimension.getName()))
                .forEach(dimension -> namespaceProjection.getDimensions().add(dimension));

            if (hasNoExternalContext) {
                namespaceProjection.getExternalContexts().addAll(childNamespaceProjection.getExternalContexts());
            }
        }
    }

    private void addLocalNamespaceResourceItemsToProjection(NamespaceProjection namespaceProjection) {
        namespaceProjection.getContextDefinitions().addAll(
            namespacedResource.getContextDefinitions().values()
        );
        namespaceProjection.getExternalContextDefinitions().addAll(
            namespacedResource.getExternalContextDefinitions().values()
        );
        namespaceProjection.getRules().addAll(
            namespacedResource.getRules().values().stream().flatMap(r -> r.stream()).collect(Collectors.toList())
        );
        namespaceProjection.getEntryPoints().addAll(
            namespacedResource.getEntryPoints().values().stream().flatMap(ep -> ep.stream()).collect(Collectors.toList())
        );
        namespaceProjection.getFunctionSignatures().putAll(
            namespacedResource.getFunctionSignatures()
        );
        namespaceProjection.getFunctions().putAll(
            namespacedResource.getFunctions()
        );
        namespacedResource.getDimensions()
            .forEach((key, value) -> namespaceProjection.getDimensions().add(value));

        if (namespacedResource.getExternalContext() != null) {
            namespaceProjection.getExternalContexts().add(namespacedResource.getExternalContext());
        }
    }

    private void validateNamespaceProjectionForIncludeAmbiguities(NamespaceProjection namespaceProjection) {
        Map<String, AmbiguousInclude> ambiguousIncludes = new HashMap<>();

        collectAmbiguousIncludes(namespaceProjection.getExternalContextDefinitions(), ambiguousIncludes);
        collectAmbiguousIncludes(namespaceProjection.getContextDefinitions(), ambiguousIncludes);
        collectAmbiguousIncludes(namespaceProjection.getRules(), ambiguousIncludes);
        collectAmbiguousIncludes(namespaceProjection.getEntryPoints(), ambiguousIncludes);
        collectAmbiguousIncludes(namespaceProjection.getExternalContexts(), ambiguousIncludes);
        collectAmbiguousIncludes(namespaceProjection.getDimensions(), ambiguousIncludes);

        if (!ambiguousIncludes.isEmpty()) {
            Set<String> errors = ambiguousIncludes.values().stream()
                    .map(ambiguousInclude ->
                            String.format("Item '%s' is ambiguous, because it is included from multiple namespaces: %s.",
                                    ambiguousInclude.getName(),
                                    String.join(", ", ambiguousInclude.getNamespaces())
                            )
                    ).collect(Collectors.toSet());

            String msg = String.format(
                    "Kraken Project '%s' has namespace include errors: %s",
                    namespaceProjection.getNamespace(),
                    String.join(System.lineSeparator(), errors)
            );

            throw new IllegalKrakenProjectStateException(msg);
        }

    }

    private <T extends Namespaced> void collectAmbiguousIncludes(Set<T> allNamespacedItems,
                                                                 Map<String, AmbiguousInclude> ambiguousIncludes) {
        allNamespacedItems.stream()
                .collect(Collectors.groupingBy(T::getName,
                        Collectors.collectingAndThen(Collectors.toSet(), items -> {
                            String itemName = items.iterator().next().getName();
                            Set<String> namespaces = items.stream().map(T::getPhysicalNamespace).collect(Collectors.toSet());
                            if(namespaces.size() > 1) {
                                ambiguousIncludes.computeIfAbsent(itemName, i -> new AmbiguousInclude(i));
                                ambiguousIncludes.get(itemName).addNamespaceIncludes(namespaces);
                            }
                            return items;
                        })
                ));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NamespaceNode that = (NamespaceNode) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    static NamespaceNode create(String name, NamespacedResource namespacedResource) {
        return new NamespaceNode(name, new HashSet<>(), namespacedResource);
    }

    static class AmbiguousInclude {

        private String name;

        private Set<String> namespaces = new HashSet<>();

        public AmbiguousInclude(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        void addNamespaceIncludes(Set<String> namespaceIncludes) {
            namespaces.addAll(namespaceIncludes);
        }

        public Set<String> getNamespaces() {
            return Collections.unmodifiableSet(namespaces);
        }
    }

    /**
     * Represents a namespace projection of all Kraken Model items accumulated by processing namespace includes.
     */
    static class NamespaceProjection {

        private final String namespace;

        private final Set<ExternalContext> externalContexts = new HashSet<>();
        private final Set<ContextDefinition> contextDefinitions = new HashSet<>();
        private final Set<ExternalContextDefinition> externalContextDefinitions = new HashSet<>();
        private final Set<EntryPoint> entryPoints = new HashSet<>();
        private final Set<Rule> rules = new HashSet<>();
        private final Map<FunctionHeader, FunctionSignature> functionSignatures = new HashMap<>();
        private final Map<String, Function> functions = new HashMap<>();
        private final Set<Dimension> dimensions = new HashSet<>();

        NamespaceProjection(String namespace) {
            this.namespace = namespace;
        }

        String getNamespace() {
            return namespace;
        }

        Set<ExternalContext> getExternalContexts() {
            return externalContexts;
        }

        ExternalContext getExternalContext() {
            return externalContexts.stream()
                    .findFirst()
                    .orElse(null);
        }

        Set<ContextDefinition> getContextDefinitions() {
            return contextDefinitions;
        }

        Set<ExternalContextDefinition> getExternalContextDefinitions() {
            return externalContextDefinitions;
        }

        Set<EntryPoint> getEntryPoints() {
            return entryPoints;
        }

        Set<Rule> getRules() {
            return rules;
        }

        Map<FunctionHeader, FunctionSignature> getFunctionSignatures() {
            return functionSignatures;
        }

        Map<String, Function> getFunctions() {
            return functions;
        }

        public Set<Dimension> getDimensions() {
            return dimensions;
        }
    }

}
