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

package kraken.runtime.model.entrypoint;

import kraken.runtime.model.Metadata;
import kraken.runtime.model.MetadataContainer;

import java.util.Collection;

/**
 * @author psurinin@eisgroup.com
 * @since 1.1.0
 */
public class RuntimeEntryPoint implements MetadataContainer {
    private final String name;
    private final Collection<String> ruleNames;
    private final Collection<String> includedEntryPoints;
    private final Metadata metadata;

    public RuntimeEntryPoint(
            String name,
            Collection<String> ruleNames,
            Collection<String> includedEntryPoints,
            Metadata metadata
    ) {
        this.name = name;
        this.ruleNames = ruleNames;
        this.includedEntryPoints = includedEntryPoints;
        this.metadata = metadata;
    }

    public String getName() {
        return name;
    }

    public Collection<String> getRuleNames() {
        return ruleNames;
    }

    public Collection<String> getIncludedEntryPoints() {
        return includedEntryPoints;
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }
}
