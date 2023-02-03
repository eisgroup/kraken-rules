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

package kraken.runtime.model.rule;

import kraken.runtime.model.Metadata;
import kraken.runtime.model.MetadataContainer;
import kraken.dimensions.DimensionSet;
import kraken.runtime.model.rule.payload.Payload;

import java.util.List;

/**
 * @author psurinin@eisgroup.com
 * @since 1.1.0
 */
public class RuntimeRule implements MetadataContainer {

    private final String name;
    private final String context;
    private final String targetPath;
    private final Condition condition;
    private final Payload payload;
    private final List<Dependency> dependencies;
    private final DimensionSet dimensionSet;
    private final Metadata metadata;
    private final Integer priority;

    public RuntimeRule(
            String name,
            String context,
            String targetPath,
            Condition condition,
            Payload payload,
            List<Dependency> dependencies,
            DimensionSet dimensionSet,
            Metadata metadata,
            Integer priority
    ) {
        this.name = name;
        this.context = context;
        this.targetPath = targetPath;
        this.condition = condition;
        this.payload = payload;
        this.dimensionSet = dimensionSet;
        this.metadata = metadata;
        this.dependencies = dependencies;
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public String getContext() {
        return context;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public Condition getCondition() {
        return condition;
    }

    public Payload getPayload() {
        return payload;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public DimensionSet getDimensionSet() {
        return dimensionSet;
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    public Integer getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return "Rule@" + name;
    }
}
