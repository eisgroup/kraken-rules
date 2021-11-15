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

package kraken.context.model.tree.impl;

import kraken.context.path.node.ContextPathNode;
import kraken.context.path.node.ContextPathNodeRepository;
import kraken.runtime.model.context.RuntimeContextDefinition;
import kraken.runtime.model.context.ContextNavigation;
import kraken.runtime.model.project.RuntimeKrakenProject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author psurinin@eisgroup.com
 * @since 1.1.0
 */
public interface ContextRepository {

    static ContextPathNodeRepository asContextNodeRepository(ContextRepository contextRepository) {
        final Map<String, ContextPathNode> cpnRepo = contextRepository.getKeys().stream().map(contextDefinitionName -> {
            final RuntimeContextDefinition contextDefinition = contextRepository.get(contextDefinitionName);
            final Map<String, ContextPathNode.ChildNode> children = new HashMap<>();
            for (ContextNavigation c : contextDefinition.getChildren().values()) {
                ContextPathNode.ChildNode x = new ContextPathNode.ChildNode(c.getTargetName(), c.getCardinality());
                if (children.put(x.getName(), x) != null) {
                    throw new IllegalStateException("Duplicate key");
                }
            }
            return new ContextPathNode(
                    contextDefinition.getName(),
                    contextDefinition.getInheritedContexts(),
                    children
            );
        })
                .collect(Collectors.toMap(x -> x.getName(), x -> x));
        return new ContextPathNodeRepository() {
            @Override
            public ContextPathNode get(String name) {
                return cpnRepo.get(name);
            }

            @Override
            public boolean has(String name) {
                return cpnRepo.containsKey(name);
            }
        };
    }

    static ContextRepository from(RuntimeKrakenProject krakenProject) {
        return new RuntimeProjectContextRepository(krakenProject);
    }

    RuntimeContextDefinition get(String name);

    Set<String> getKeys();

    String getRootName();

    class RuntimeProjectContextRepository implements ContextRepository {

        private final RuntimeKrakenProject runtimeKrakenProject;

        private RuntimeProjectContextRepository(RuntimeKrakenProject runtimeKrakenProject) {
            this.runtimeKrakenProject = runtimeKrakenProject;
        }

        @Override
        public RuntimeContextDefinition get(String name) {
            return runtimeKrakenProject.getContextDefinitions().get(name);
        }

        @Override
        public Set<String> getKeys() {
            return runtimeKrakenProject.getContextDefinitions().keySet();
        }

        @Override
        public String getRootName() {
            return runtimeKrakenProject.getRootContextName();
        }
    }
}
