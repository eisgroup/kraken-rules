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
package kraken.model.project.builder;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import kraken.model.project.exception.IllegalKrakenProjectStateException;

/**
 * Represents namespaces and their inter-dependencies as a Directed Acyclic Graph where each node is a namespace
 * and edge is namespace include. For each namespace there is one and only one {@link NamespaceNode} that represents it.
 * <p/>
 * If namespaces do not conform to Directed Acyclic Graph then {@link IllegalKrakenProjectStateException} is thrown
 * during initialization of a graph.
 *
 * @author mulevicius
 */
class NamespaceTree {

    private NamespaceNode root;

    NamespaceTree(NamespaceNode root) {
        this.root = root;
    }

    NamespaceNode getRoot() {
        return root;
    }

    static NamespaceTree create(String namespace, Map<String, NamespacedResource> resources) {
        NamespaceNode rootNamespace = NamespaceNode.create(namespace, resources.get(namespace));
        Map<String, NamespaceNode> namespaceNodePool = new HashMap<>();
        createChildrenNodes(rootNamespace, resources, namespaceNodePool);
        checkCycle(rootNamespace, new LinkedHashSet<>());
        return new NamespaceTree(rootNamespace);
    }

    private static void createChildrenNodes(NamespaceNode parent,
                                            Map<String, NamespacedResource> resources,
                                            Map<String, NamespaceNode> namespaceNodePool) {
        for(String include : resources.get(parent.getName()).getIncludes()) {
            if(namespaceNodePool.containsKey(include)) {
                parent.addChild(namespaceNodePool.get(include));
            } else {
                NamespaceNode child = NamespaceNode.create(include, resources.get(include));
                parent.addChild(child);
                namespaceNodePool.put(include, child);
                createChildrenNodes(child, resources, namespaceNodePool);
            }
        }
    }

    private static void checkCycle(NamespaceNode current, Set<NamespaceNode> visited) {
        if (visited.contains(current)) {
            throw new IllegalKrakenProjectStateException("A cycle found between namespaces: " + beautify(current, visited));
        }

        visited.add(current);
        current.getChildren().forEach(child -> checkCycle(child, visited));
        visited.remove(current);
    }

    private static String beautify(NamespaceNode current, Set<NamespaceNode> visited) {
        return Stream.concat(visited.stream(), Stream.of(current))
                .map(NamespaceNode::getName)
                .collect(Collectors.joining(" -> "));
    }
}
