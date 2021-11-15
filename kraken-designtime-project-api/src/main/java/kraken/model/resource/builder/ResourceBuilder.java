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
package kraken.model.resource.builder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kraken.annotations.API;
import kraken.el.functionregistry.FunctionHeader;
import kraken.model.FunctionSignature;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.resource.Resource;
import kraken.model.resource.RuleImport;
import kraken.utils.ResourceUtils;

/**
 * Builds {@link Resource}.
 *
 * @author mulevicius
 * @since 1.1.0
 */
@API
public class ResourceBuilder {

    private String namespace;
    private List<String> includes;
    private List<RuleImport> imports;
    private List<EntryPoint> entryPoints;
    private List<Rule> rules;
    private List<ContextDefinition> contextDefinitions;
    private ExternalContext externalContext;
    private List<ExternalContextDefinition> externalContextDefinitions;
    private Map<FunctionHeader, FunctionSignature> functionSignatures;
    private URI uri;

    private ResourceBuilder() {
        this.includes = new ArrayList<>();
        this.imports = new ArrayList<>();
        this.entryPoints = new ArrayList<>();
        this.rules = new ArrayList<>();
        this.contextDefinitions = new ArrayList<>();
        this.externalContextDefinitions = new ArrayList<>();
        this.functionSignatures = new LinkedHashMap<>();
    }

    public static ResourceBuilder getInstance(){
        return new ResourceBuilder();
    }

    public Resource build() {
        return new Resource(
            namespace,
            contextDefinitions,
            entryPoints,
            rules,
            includes,
            imports,
            externalContext,
            externalContextDefinitions,
            new ArrayList<>(functionSignatures.values()),
            uri == null ? ResourceUtils.randomResourceUri() : uri
        );
    }

    public ResourceBuilder setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public ResourceBuilder addInclude(String include) {
        this.includes.add(include);
        return this;
    }

    public ResourceBuilder addIncludes(Collection<String> includes) {
        this.includes.addAll(includes);
        return this;
    }

    public ResourceBuilder addImport(RuleImport imports) {
        this.imports.add(imports);
        return this;
    }

    public ResourceBuilder addImports(Collection<RuleImport> imports) {
        this.imports.addAll(imports);
        return this;
    }

    public ResourceBuilder addContextDefinition(ContextDefinition contextDefinition) {
        this.contextDefinitions.add(contextDefinition);
        return this;
    }

    public ResourceBuilder addContextDefinitions(Collection<ContextDefinition> contextDefinition) {
        this.contextDefinitions.addAll(contextDefinition);
        return this;
    }

    public ResourceBuilder setExternalContext(ExternalContext externalContext) {
        this.externalContext = externalContext;
        return this;
    }

    public ResourceBuilder addExternalContextDefinition(ExternalContextDefinition externalContextDefinition) {
        this.externalContextDefinitions.add(externalContextDefinition);
        return this;
    }

    public ResourceBuilder addExternalContextDefinitions(Collection<ExternalContextDefinition> externalContextDefinition) {
        this.externalContextDefinitions.addAll(externalContextDefinition);
        return this;
    }

    public ResourceBuilder addRule(Rule rule) {
        this.rules.add(rule);
        return this;
    }

    public ResourceBuilder addRules(Collection<Rule> rules) {
        this.rules.addAll(rules);
        return this;
    }

    public ResourceBuilder addEntryPoint(EntryPoint entryPoint) {
        this.entryPoints.add(entryPoint);
        return this;
    }

    public ResourceBuilder addEntryPoints(Collection<EntryPoint> entryPoint) {
        this.entryPoints.addAll(entryPoint);
        return this;
    }

    public ResourceBuilder addFunctionSignature(FunctionSignature functionSignature) {
        FunctionHeader header = new FunctionHeader(
            functionSignature.getName(),
            functionSignature.getParameterTypes().size()
        );
        if(functionSignatures.containsKey(header)) {
            String template = "Cannot add function '%s' to Resource, because function with header '%s' was already added";
            String message = String.format(template, functionSignature, header);
            throw new IllegalStateException(message);
        }
        this.functionSignatures.put(header, functionSignature);
        return this;
    }

    public ResourceBuilder addFunctionSignatures(Collection<FunctionSignature> functionSignatures) {
        functionSignatures.forEach(this::addFunctionSignature);
        return this;
    }

    public ResourceBuilder setResourceUri(URI uri) {
        this.uri = uri;
        return this;
    }
}
