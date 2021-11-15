/*
 *  Copyright 2021 EIS Ltd and/or one of its affiliates.
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
package kraken.model.project;

import kraken.annotations.API;

import java.util.List;
import java.util.Objects;

/**
 * Represents a single node in a {@link NamespaceTree}. Child nodes represent all included
 * namespaces.
 *
 * @author Tomas Dapkunas
 * @since 1.9.0
 */
@API
public final class NamespaceNode {

    private final String name;
    private final List<NamespaceNode> childNodes;

    public NamespaceNode(String name, List<NamespaceNode> childNodes) {
        this.name = name;
        this.childNodes = childNodes;
    }

    /**
     * Returns a name of this node, which is namespace name.
     *
     * @return Namespace name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a collection of child nodes, which are included
     * namespaces.
     *
     * @return Child namespace nodes.
     */
    public List<NamespaceNode> getChildNodes() {
        return childNodes;
    }

    /**
     * Returns a {@code NamespaceNode} for given namespace name.
     *
     * @param namespaceName Namespace name.
     * @return {@code NamespaceNode} or {@code null} if not found.
     */
    public NamespaceNode getNode(String namespaceName) {
        if (name.equals(namespaceName)) {
            return this;
        }

        return childNodes.stream()
                .map(node -> node.getNode(namespaceName))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

}
