/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.model.project;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import kraken.runtime.model.context.RuntimeContextDefinition;
import kraken.runtime.model.entrypoint.RuntimeEntryPoint;
import kraken.runtime.model.rule.RuntimeRule;

/**
 * @author mulevicius
 */
public class RuntimeKrakenProject {

    private UUID checksum;

    private String namespace;

    private String rootContextName;

    private Map<String, RuntimeContextDefinition> contextDefinitions;

    private List<RuntimeEntryPoint> entryPoints;

    private List<RuntimeRule> rules;

    private Map<String, List<RuntimeEntryPoint>> entryPointVersions;

    private Map<String, List<RuntimeRule>> ruleVersions;

    public RuntimeKrakenProject(UUID checksum,
                                String namespace,
                                String rootContextName,
                                Map<String, RuntimeContextDefinition> contextDefinitions,
                                List<RuntimeEntryPoint> entryPoints,
                                List<RuntimeRule> rules) {
        this.checksum = checksum;
        this.namespace = Objects.requireNonNull(namespace);
        this.rootContextName = rootContextName;
        this.contextDefinitions = Collections.unmodifiableMap(contextDefinitions);
        this.entryPoints = Collections.unmodifiableList(entryPoints);
        this.rules = Collections.unmodifiableList(rules);

        this.entryPointVersions = entryPoints.stream().collect(Collectors.groupingBy(RuntimeEntryPoint::getName));
        this.ruleVersions = rules.stream().collect(Collectors.groupingBy(RuntimeRule::getName));
    }

    public UUID getChecksum() {
        return checksum;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getRootContextName() {
        return rootContextName;
    }

    public Map<String, RuntimeContextDefinition> getContextDefinitions() {
        return contextDefinitions;
    }

    public List<RuntimeEntryPoint> getEntryPoints() {
        return entryPoints;
    }

    public List<RuntimeRule> getRules() {
        return rules;
    }

    public Map<String, List<RuntimeEntryPoint>> getEntryPointVersions() {
        return entryPointVersions;
    }

    public Map<String, List<RuntimeRule>> getRuleVersions() {
        return ruleVersions;
    }
}
