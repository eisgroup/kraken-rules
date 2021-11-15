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
package kraken.context.path.node;

import kraken.model.context.Cardinality;

import java.util.Collection;
import java.util.Map;

public class ContextPathNode {

    private final String name;
    private final Collection<String> inherited;
    private final Map<String, ChildNode> children;

    public ContextPathNode(String name, Collection<String> inherited, Map<String, ChildNode> children) {
        this.name = name;
        this.inherited = inherited;
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public Collection<String> getInherited() {
        return inherited;
    }

    public Map<String, ChildNode> getChildren() {
        return children;
    }

    public static class ChildNode {
        private final Cardinality cardinality;
        private final String name;

        public ChildNode(String name, Cardinality cardinality) {
            this.cardinality = cardinality;
            this.name = name;
        }

        public Cardinality getCardinality() {
            return cardinality;
        }

        public String getName() {
            return name;
        }
    }
}