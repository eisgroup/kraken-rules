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
package kraken.model.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import kraken.annotations.API;
import kraken.model.FunctionSignature;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.factory.RulesModelFactory;
import kraken.namespace.Namespaced;
import kraken.utils.ResourceUtils;

/**
 * Represents a bunch of {@link Rule}, {@link EntryPoint}, {@link ContextDefinition}, {@link ExternalContext}
 * and {@link #includes) defined in a {@link #namespace}.
 *
 * @author mulevicius
 */
@API
public final class Resource {

    private final String namespace;

    private final List<ContextDefinition> contextDefinitions;

    private final ExternalContext externalContext;

    private final List<ExternalContextDefinition> externalContextDefinitions;

    private final List<EntryPoint> entryPoints;

    private final List<Rule> rules;

    private final List<String> includes;

    private final List<RuleImport> ruleImports;

    private final List<FunctionSignature> functionSignatures;

    private final URI uri;

    /**
     * Creates a new instance of {@code Resource} with given arguments.
     *
     * @param namespace                  Namespace which uniquely identifies this resource.
     * @param contextDefinitions         Context definitions applicable for this resource.
     * @param entryPoints                Entry points applicable fot this resource.
     * @param rules                      Rules applicable for this resource.
     * @param includes                   Includes applicable for this resource.
     * @param ruleImports                Rule import applicable for this resource.
     * @param externalContext            External context bound to this resource.
     * @param externalContextDefinitions ContextDefinition for external context.
     * @param functionSignatures         A list of function signatures that the rules can use from this namespace.
     * @param uri                        Identifies a resource.
     */
    public Resource(String namespace,
                    @Nonnull List<ContextDefinition> contextDefinitions,
                    @Nonnull List<EntryPoint> entryPoints,
                    @Nonnull List<Rule> rules,
                    @Nonnull List<String> includes,
                    @Nonnull List<RuleImport> ruleImports,
                    ExternalContext externalContext,
                    @Nonnull List<ExternalContextDefinition> externalContextDefinitions,
                    @Nonnull List<FunctionSignature> functionSignatures,
                    @Nonnull URI uri) {
        this.namespace = namespace == null ? Namespaced.GLOBAL : namespace;
        this.externalContext = externalContext;
        this.externalContextDefinitions = Objects.requireNonNull(externalContextDefinitions);
        this.contextDefinitions = Objects.requireNonNull(contextDefinitions);
        this.entryPoints = Objects.requireNonNull(entryPoints);
        this.rules = Objects.requireNonNull(rules);
        this.includes = Objects.requireNonNull(includes);
        this.ruleImports = Objects.requireNonNull(ruleImports);
        this.functionSignatures = Objects.requireNonNull(functionSignatures);
        this.uri = Objects.requireNonNull(uri);
    }

    /**
     * Creates a new instance of {@code Resource} with given arguments.
     *
     * @param namespace                  Namespace which uniquely identifies this resource.
     * @param contextDefinitions         Context definitions applicable for this resource.
     * @param entryPoints                Entry points applicable fot this resource.
     * @param rules                      Rules applicable for this resource.
     * @param includes                   Includes applicable for this resource.
     * @param ruleImports                Rule import applicable for this resource.
     * @param externalContext            External context bound to this resource.
     * @param externalContextDefinitions ContextDefinition for external context.
     * @param uri                        Identifies a resource.
     * @deprecated  Use {@link #Resource(String, List, List, List, List, List, ExternalContext, List, List, URI)} instead.
     */
    @Deprecated(since = "1.17.0", forRemoval = true)
    public Resource(String namespace,
                    List<ContextDefinition> contextDefinitions,
                    List<EntryPoint> entryPoints,
                    List<Rule> rules,
                    List<String> includes,
                    List<RuleImport> ruleImports,
                    ExternalContext externalContext,
                    List<ExternalContextDefinition> externalContextDefinitions,
                    URI uri) {
        this(
            namespace == null ? Namespaced.GLOBAL : namespace,
            contextDefinitions,
            entryPoints,
            rules,
            new ArrayList<>(includes),
            new ArrayList<>(ruleImports),
            externalContext,
            externalContextDefinitions,
            List.of(),
            uri
        );
    }

    /**
     * @deprecated  Use {@link #Resource(String, List, List, List, List, List, ExternalContext, List, List, URI)} instead.
     */
    @Deprecated(since = "1.15.0", forRemoval = true)
    public Resource(String namespace,
                    List<ContextDefinition> contextDefinitions,
                    List<EntryPoint> entryPoints,
                    List<Rule> rules,
                    Collection<String> includes,
                    Collection<RuleImport> ruleImports,
                    ExternalContext externalContext,
                    List<ExternalContextDefinition> externalContextDefinitions) {
        this(
            namespace,
            contextDefinitions,
            entryPoints,
            rules,
            new ArrayList<>(includes),
            new ArrayList<>(ruleImports),
            externalContext,
            externalContextDefinitions,
            ResourceUtils.randomResourceUri()
        );
    }

    public String getNamespace() {
        return namespace;
    }

    public List<ContextDefinition> getContextDefinitions() {
        return contextDefinitions;
    }

    public List<ExternalContextDefinition> getExternalContextDefinitions() {
        return externalContextDefinitions;
    }

    @Nullable
    public ExternalContext getExternalContext() {
        return externalContext;
    }

    public List<EntryPoint> getEntryPoints() {
        return entryPoints;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public List<String> getIncludes() {
        return includes;
    }

    public List<RuleImport> getRuleImports() {
        return ruleImports;
    }

    public List<FunctionSignature> getFunctionSignatures() {
        return functionSignatures;
    }

    public URI getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "Resource@" + namespace ;
    }

    public static Resource clone(Resource resource) {
        RulesModelFactory factory = RulesModelFactory.getInstance();
        return new Resource(
            resource.namespace,
            resource.contextDefinitions.stream().map(factory::cloneContextDefinition).collect(Collectors.toList()),
            resource.entryPoints.stream().map(factory::cloneEntryPoint).collect(Collectors.toList()),
            resource.rules.stream().map(factory::cloneRule).collect(Collectors.toList()),
            new ArrayList<>(resource.includes),
            new ArrayList<>(resource.ruleImports.stream()
                .map(i -> new RuleImport(i.getNamespace(), i.getRuleName()))
                .collect(Collectors.toList())),
            resource.externalContext != null ? factory.cloneExternalContext(resource.externalContext) : null,
            resource.externalContextDefinitions.stream().map(factory::cloneExternalContextDefinition).collect(Collectors.toList()),
            resource.functionSignatures.stream().map(factory::cloneFunctionSignature).collect(Collectors.toList()),
            resource.uri
        );
    }

}
