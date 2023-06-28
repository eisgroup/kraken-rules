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
package kraken.model.impl;

import kraken.model.Condition;
import kraken.model.Metadata;
import kraken.model.Payload;
import kraken.model.Rule;

public class RuleImpl implements Rule {

    private String ruleVariationId;

    private String name;

    private String description;

    private String physicalNamespace;

    private String context;

    private String targetPath;

    private Condition condition;

    private Payload payload;

    private Metadata metadata;

    private boolean dimensional = false;

    private Integer priority;

    private boolean serverSideOnly;

    @Override
    public String getRuleVariationId() {
        return ruleVariationId;
    }

    public void setRuleVariationId(String ruleVariationId) {
        this.ruleVariationId = ruleVariationId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String ruleDescription) {
        this.description = ruleDescription;
    }

    @Override
    public String getContext() {
        return context;
    }

    @Override
    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public String getTargetPath() {
        return targetPath;
    }

    @Override
    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    @Override
    public Condition getCondition() {
        return condition;
    }

    @Override
    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    @Override
    public Payload getPayload() {
        return payload;
    }

    @Override
    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    @Override
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
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
        return "Rule:" + name + ":" + context + ":" + targetPath;
    }

    @Override
    public boolean isDimensional() {
        return dimensional;
    }

    @Override
    public void setDimensional(boolean dimensional) {
        this.dimensional = dimensional;
    }

    @Override
    public Integer getPriority() {
        return priority;
    }

    @Override
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    public boolean isServerSideOnly() {
        return serverSideOnly;
    }

    @Override
    public void setServerSideOnly(boolean serverSideOnly) {
        this.serverSideOnly = serverSideOnly;
    }

}
