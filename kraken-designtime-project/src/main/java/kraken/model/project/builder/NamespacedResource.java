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
package kraken.model.project.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kraken.el.functionregistry.FunctionHeader;
import kraken.model.FunctionSignature;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.resource.Resource;
import kraken.model.resource.RuleImport;

/**
 * Represents merged {@link Resource} for a single namespace with rule imports already processed.
 *
 * @author mulevicius
 */
class NamespacedResource {

    private final String namespace;

    private ExternalContext externalContext;

    private Map<String, ContextDefinition> contextDefinitions = new HashMap<>();
    private Map<String, ExternalContextDefinition> externalContextDefinitions = new HashMap<>();
    private Map<String, List<EntryPoint>> entryPoints = new HashMap<>();
    private Map<String, List<Rule>> rules = new HashMap<>();
    private Set<RuleImport> ruleImports = new HashSet<>();
    private Set<String> includes = new HashSet<>();
    private Map<FunctionHeader, FunctionSignature> functions = new HashMap<>();

    NamespacedResource(String namespace) {
        this.namespace = namespace;
    }

    void addRule(Rule rule) {
        rules.putIfAbsent(rule.getName(), new ArrayList<>());
        rules.get(rule.getName()).add(rule);
    }

    void addEntryPoint(EntryPoint entryPoint) {
        entryPoints.putIfAbsent(entryPoint.getName(), new ArrayList<>());
        entryPoints.get(entryPoint.getName()).add(entryPoint);
    }

    void addContextDefinition(ContextDefinition contextDefinition) {
        contextDefinitions.putIfAbsent(contextDefinition.getName(), contextDefinition);
    }

    void setExternalContext(ExternalContext externalContext) {
        this.externalContext = externalContext;
    }

    void addExternalContextDefinition(ExternalContextDefinition externalContextDefinition) {
        externalContextDefinitions.putIfAbsent(externalContextDefinition.getName(), externalContextDefinition);
    }

    void addRuleImport(RuleImport ruleImport) {
        ruleImports.add(ruleImport);
    }

    void addInclude(String include) {
        includes.add(include);
    }

    void addFunction(FunctionSignature function) {
        var header = new FunctionHeader(function.getName(), function.getParameterTypes().size());
        functions.putIfAbsent(header, function);
    }

    String getNamespace() {
        return namespace;
    }

    public ExternalContext getExternalContext() {
        return externalContext;
    }

    Map<String, ExternalContextDefinition> getExternalContextDefinitions() {
        return externalContextDefinitions;
    }

    Map<String, ContextDefinition> getContextDefinitions() {
        return contextDefinitions;
    }

    Map<String, List<EntryPoint>> getEntryPoints() {
        return entryPoints;
    }

    Map<String, List<Rule>> getRules() {
        return rules;
    }

    Set<RuleImport> getRuleImports() {
        return ruleImports;
    }

    Set<String> getIncludes() {
        return includes;
    }

    public Map<FunctionHeader, FunctionSignature> getFunctions() {
        return functions;
    }
}
