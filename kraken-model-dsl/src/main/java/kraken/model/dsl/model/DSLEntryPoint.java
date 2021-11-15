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
package kraken.model.dsl.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * @author mulevicius
 */
public class DSLEntryPoint {

    private String name;

    private DSLMetadata metadata;

    private Collection<String> ruleNames;

    private Collection<String> includedEntryPointNames;

    private boolean serverSideOnly;

    public DSLEntryPoint(String name, DSLMetadata metadata, Collection<String> ruleNames,
            Collection<String> includedEntryPointNames, boolean serverSideOnly) {
        this.name = Objects.requireNonNull(name);
        this.metadata = metadata;
        this.ruleNames = Objects.requireNonNull(ruleNames);
        this.includedEntryPointNames = Objects.requireNonNull(includedEntryPointNames);
        this.serverSideOnly = serverSideOnly;
    }

    public Collection<String> getRuleNames() {
        return Collections.unmodifiableCollection(ruleNames);
    }

    public DSLMetadata getMetadata() {
        return metadata;
    }

    public String getName() {
        return name;
    }

    public Collection<String> getIncludedEntryPointNames() {
        return includedEntryPointNames;
    }

    public boolean isServerSideOnly() {
        return serverSideOnly;
    }
}
