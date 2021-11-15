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
package kraken.model.entrypoint;

import java.util.ArrayList;
import java.util.List;

import kraken.model.Metadata;

public class EntryPointImpl implements EntryPoint {

    private String entryPointVariationId;

    private Metadata metadata;

    private String name;

    private String physicalNamespace;

    private List<String> ruleNames;

    private List<String> includedEntryPointNames;

    private boolean serverSideOnly;

    @Override
    public String getEntryPointVariationId() {
        return entryPointVariationId;
    }

    @Override
    public void setEntryPointVariationId(String entryPointVariationId) {
        this.entryPointVariationId = entryPointVariationId;
    }

    @Override
    public boolean isServerSideOnly() {
        return serverSideOnly;
    }

    @Override
    public void setServerSideOnly(boolean serverSideOnly) {
        this.serverSideOnly = serverSideOnly;
    }

    @Override
    public void setRuleNames(List<String> ruleNames) {
        this.ruleNames = ruleNames;
    }

    @Override
    public List<String> getRuleNames() {
        if (ruleNames == null) {
            ruleNames = new ArrayList<>();
        }
        return ruleNames;
    }

    @Override
    public List<String> getIncludedEntryPointNames() {
        if(includedEntryPointNames == null){
            includedEntryPointNames = new ArrayList<>();
        }
        return includedEntryPointNames;
    }

    @Override
    public void setIncludedEntryPointNames(List<String> includedEntryPointNames) {
        this.includedEntryPointNames = includedEntryPointNames;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public String getPhysicalNamespace() {
        return physicalNamespace;
    }

    @Override
    public void setPhysicalNamespace(String name) {
        this.physicalNamespace = name;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
