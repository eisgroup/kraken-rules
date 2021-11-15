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

/**
 * Represents namespaces and their inter-dependencies as a tree.
 *
 * @author Tomas Dapkunas
 * @since 1.9.0
 */
@API
public final class NamespaceTree {

    private final NamespaceNode root;

    public NamespaceTree(NamespaceNode root) {
        this.root = root;
    }

    /**
     * Returns root namespace node.
     *
     * @return Root namespace node.
     */
    public NamespaceNode getRoot() {
        return root;
    }

    /**
     * Returns namespace node for given namespace name.
     *
     * @param namespaceName Name of namespace.
     * @return Namespace node or {@code null} if not exist.
     */
    public NamespaceNode getNode(String namespaceName) {
        return root.getNode(namespaceName);
    }

}
