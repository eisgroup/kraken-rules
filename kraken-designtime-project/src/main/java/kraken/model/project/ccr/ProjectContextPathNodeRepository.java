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

package kraken.model.project.ccr;


import kraken.context.path.node.ContextPathNode;
import kraken.context.path.node.ContextPathNodeRepository;
import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextNavigation;
import kraken.model.project.KrakenProject;

import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;

/**
 * @author psurinin@eisgroup.com
 * @since 1.1.0
 */
public class ProjectContextPathNodeRepository implements ContextPathNodeRepository {

    private final KrakenProject krakenProject;

    public static ProjectContextPathNodeRepository create(KrakenProject krakenProject) {
        return new ProjectContextPathNodeRepository(krakenProject);
    }

    private ProjectContextPathNodeRepository(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
    }

    @Override
    public ContextPathNode get(String name) {
        return createNode(krakenProject.getContextProjection(name));
    }

    @Override
    public boolean has(String name) {
        return krakenProject.getContextDefinitions().containsKey(name);
    }

    private ContextPathNode createNode(ContextDefinition contextDefinition) {
        var referencableChildren = contextDefinition.getChildren().values().stream()
            .filter(contextNavigation -> BooleanUtils.isNotTrue(contextNavigation.getForbidReference()))
            .collect(Collectors.toMap(
                ContextNavigation::getTargetName,
                x -> new ContextPathNode.ChildNode(x.getTargetName(), x.getCardinality())
        ));
        return new ContextPathNode(
            contextDefinition.getName(),
            contextDefinition.getParentDefinitions(),
            referencableChildren
        );
    }
}
