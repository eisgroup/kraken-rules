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
package kraken.model.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Suppliers;

import kraken.model.FunctionSignature;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.model.context.ContextNavigation;
import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.ccr.CrossContextService;
import kraken.model.project.ccr.CrossContextServiceSupplier;
import kraken.model.project.exception.IllegalKrakenProjectStateException;
import kraken.model.project.scope.ScopeBuilder;
import kraken.model.project.scope.ScopeBuilderSupplier;

/**
 * Represents a self-contained projection of {@link Rule}, {@link EntryPoint} and {@link ContextDefinition} for a namespace.
 * Kraken Project contains everything required to process and evaluate rules within a particular namespace.
 *
 * @author mulevicius
 */
public class ResourceKrakenProject implements KrakenProject, ScopeBuilderSupplier, CrossContextServiceSupplier {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    private final UUID identifier;

    private final String namespace;

    private final String rootContextName;

    private final ExternalContext externalContext;

    private final Map<String, ExternalContextDefinition> externalContextDefinitions;

    private final Map<String, ContextDefinition> contextDefinitions;

    private final List<EntryPoint> entryPoints;

    private final List<Rule> rules;

    private final Map<String, List<EntryPoint>> entryPointVersions;

    private final Map<String, List<Rule>> ruleVersions;

    private final ConcurrentHashMap<String, ContextDefinition> contextDefinitionProjections = new ConcurrentHashMap<>();

    private final Supplier<ScopeBuilder> scopeBuilder;

    private final Supplier<CrossContextService> crossContextService;

    private final NamespaceTree namespaceTree;

    private final List<FunctionSignature> functionSignatures;

    public ResourceKrakenProject(@Nonnull String namespace,
                                 @Nonnull String rootContextName,
                                 @Nonnull Map<String, ContextDefinition> contextDefinitions,
                                 @Nonnull List<EntryPoint> entryPoints,
                                 @Nonnull List<Rule> rules,
                                 ExternalContext externalContext,
                                 @Nonnull Map<String, ExternalContextDefinition> externalContextDefinitions,
                                 NamespaceTree namespaceTree,
                                 @Nonnull List<FunctionSignature> functionSignatures) {
        this.identifier = UUID.randomUUID();
        this.namespace = Objects.requireNonNull(namespace);
        this.rootContextName = Objects.requireNonNull(rootContextName);
        this.externalContext = externalContext;
        this.externalContextDefinitions = Objects.requireNonNull(externalContextDefinitions);
        this.contextDefinitions = Collections.unmodifiableMap(contextDefinitions);
        this.entryPoints = Collections.unmodifiableList(entryPoints);
        this.rules = Collections.unmodifiableList(rules);
        this.ruleVersions = Collections.unmodifiableMap(
            rules.stream()
                .filter(rule -> rule.getName() != null)
                .collect(Collectors.groupingBy(Rule::getName))
        );
        this.entryPointVersions = Collections.unmodifiableMap(
            entryPoints.stream()
                .filter(ep -> ep.getName() != null)
                .collect(Collectors.groupingBy(EntryPoint::getName))
        );
        this.scopeBuilder = Suppliers.memoize(() -> new ScopeBuilder(this));
        this.crossContextService = Suppliers.memoize(() -> new CrossContextService(this));
        this.namespaceTree = namespaceTree;
        this.functionSignatures = Objects.requireNonNull(functionSignatures);
    }

    @Override
    public UUID getIdentifier() {
        return identifier;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getRootContextName() {
        return rootContextName;
    }

    @Nullable
    @Override
    public ExternalContext getExternalContext() {
        return externalContext;
    }

    @Override
    public Map<String, ContextDefinition> getContextDefinitions() {
        return contextDefinitions;
    }

    @Override
    public Map<String, ExternalContextDefinition> getExternalContextDefinitions() {
        return externalContextDefinitions;
    }

    @Override
    public List<EntryPoint> getEntryPoints() {
        return entryPoints;
    }

    @Override
    public List<Rule> getRules() {
        return rules;
    }

    @Override
    public Map<String, List<EntryPoint>> getEntryPointVersions() {
        return entryPointVersions;
    }

    @Override
    public Map<String, List<Rule>> getRuleVersions() {
        return ruleVersions;
    }

    @Override
    public ScopeBuilder getScopeBuilder() {
        return scopeBuilder.get();
    }

    @Override
    public CrossContextService getCrossContextService() {
        return crossContextService.get();
    }

    @Nullable
    @Override
    public NamespaceTree getNamespaceTree() {
        return namespaceTree;
    }

    @Override
    public List<FunctionSignature> getFunctionSignatures() {
        return functionSignatures;
    }

    /**
     *
     * @param contextName
     * @return projection of ContextDefinition in designtime domain.
     *          Has inherited contexts, fields and children projected.
     *          If context with requested name does not exist then null will be returned.
     */
    @Override
    public ContextDefinition getContextProjection(String contextName) {
        if(!contextDefinitions.containsKey(contextName)) {
            return null;
        }
        return contextDefinitionProjections.computeIfAbsent(contextName, c -> {
            ContextDefinition contextDefinition = contextDefinitions.get(c);
            ContextDefinition contextProjection = factory.createContextDefinition();
            contextProjection.setName(contextDefinition.getName());
            contextProjection.setPhysicalNamespace(contextDefinition.getPhysicalNamespace());
            contextProjection.setStrict(contextDefinition.isStrict());
            contextProjection.setRoot(contextDefinition.isRoot());
            Collection<String> inheritedContextDefinitions = getInheritedContextDefinitions(c);
            contextProjection.setParentDefinitions(inheritedContextDefinitions);
            contextProjection.setContextFields(getContextFieldProjection(c, inheritedContextDefinitions));
            contextProjection.setChildren(getChildrenProjection(c, inheritedContextDefinitions));
            return contextProjection;
        });
    }

    private List<String> getInheritedContextDefinitions(String contextName) {
        ContextDefinition contextDefinition = contextDefinitions.get(contextName);
        List<String> inheritedContextDefinitions = new ArrayList<>(contextDefinition.getParentDefinitions());
        for(String p : contextDefinition.getParentDefinitions()) {
            if(!contextDefinitions.containsKey(p)) {
                String format = "ContextDefinition '%s' does not exist in Kraken Project '%s' but it is inherited by '%s'";
                String message = String.format(format, p, namespace, contextName);
                throw new IllegalKrakenProjectStateException(message);
            }
            inheritedContextDefinitions.addAll(getInheritedContextDefinitions(p));
        }
        return inheritedContextDefinitions;
    }

    private Map<String, ContextField> getContextFieldProjection(String contextName, Collection<String> inheritedContextDefinitions) {
        ContextDefinition contextDefinition = contextDefinitions.get(contextName);
        Map<String, ContextField> fields = new LinkedHashMap<>(contextDefinition.getContextFields());
        for(String inheritedContextName : inheritedContextDefinitions) {
            ContextDefinition inheritedContextDefinition = contextDefinitions.get(inheritedContextName);
            for(ContextField contextField : inheritedContextDefinition.getContextFields().values()) {
                fields.putIfAbsent(contextField.getName(), contextField);
            }
        }

        return fields;
    }

    private Map<String, ContextNavigation> getChildrenProjection(String contextName, Collection<String> inheritedContextDefinitions) {
        ContextDefinition contextDefinition = contextDefinitions.get(contextName);
        Map<String, ContextNavigation> children = new LinkedHashMap<>(contextDefinition.getChildren());
        inheritedContextDefinitions.stream()
                .map(inheritedContextName -> contextDefinitions.get(inheritedContextName))
                .flatMap(context -> context.getChildren().values().stream())
                .forEach(child -> {
                    if (child != null && !alreadyExists(children, child)) {
                        children.put(child.getTargetName(), child);
                    }
                });

        return children;
    }

    private boolean alreadyExists(Map<String, ContextNavigation> children, ContextNavigation candidate) {
        if(children.containsKey(candidate.getTargetName())) {
            return true;
        }
        for(ContextNavigation child : children.values()) {
            if(getInheritedContextDefinitions(child.getTargetName()).contains(candidate.getTargetName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds new instance of KrakenProject by replacing rules and entrypoints with provided.
     * This is more efficient than using {@link KrakenProjectBuilder}, because scope and ccr calculations are reused.
     * Note, that every rule and entrypoint must have physical namespace equal to namespace existing
     * in the KrakenProject and new namespace cannot be created.
     *
     * @param entryPoints
     * @param rules
     * @return new instance of KrakenProject
     */
    public ResourceKrakenProject with(List<EntryPoint> entryPoints, List<Rule> rules) {

        return new ResourceKrakenProject(
            namespace,
            rootContextName,
            contextDefinitions,
            entryPoints,
            rules,
            externalContext,
            externalContextDefinitions,
            namespaceTree,
            functionSignatures,
            scopeBuilder,
            crossContextService
        );
    }

    /**
     * Package private constructor that allows to create new instance by providing context calculations externally.
     * Invoker must ensure that context calculations are valid with respect to provided context models.
     *
     * @param namespace
     * @param rootContextName
     * @param contextDefinitions
     * @param entryPoints
     * @param rules
     * @param externalContext
     * @param externalContextDefinitions
     * @param namespaceTree
     * @param functionSignatures
     * @param scopeBuilder externally provided scope builder
     * @param crossContextService externally provided ccr path service
     */
    ResourceKrakenProject(@Nonnull String namespace,
                          @Nonnull String rootContextName,
                          @Nonnull Map<String, ContextDefinition> contextDefinitions,
                          @Nonnull List<EntryPoint> entryPoints,
                          @Nonnull List<Rule> rules,
                          ExternalContext externalContext,
                          @Nonnull Map<String, ExternalContextDefinition> externalContextDefinitions,
                          NamespaceTree namespaceTree,
                          @Nonnull List<FunctionSignature> functionSignatures,
                          @Nonnull Supplier<ScopeBuilder> scopeBuilder,
                          @Nonnull Supplier<CrossContextService> crossContextService) {
        this.identifier = UUID.randomUUID();
        this.namespace = Objects.requireNonNull(namespace);
        this.rootContextName = Objects.requireNonNull(rootContextName);
        this.externalContext = externalContext;
        this.externalContextDefinitions = Objects.requireNonNull(externalContextDefinitions);
        this.contextDefinitions = Collections.unmodifiableMap(contextDefinitions);
        this.entryPoints = Collections.unmodifiableList(entryPoints);
        this.rules = Collections.unmodifiableList(rules);
        this.ruleVersions = Collections.unmodifiableMap(
            rules.stream()
                .filter(rule -> rule.getName() != null)
                .collect(Collectors.groupingBy(Rule::getName))
        );
        this.entryPointVersions = Collections.unmodifiableMap(
            entryPoints.stream()
                .filter(ep -> ep.getName() != null)
                .collect(Collectors.groupingBy(EntryPoint::getName))
        );
        this.namespaceTree = namespaceTree;
        this.functionSignatures = Objects.requireNonNull(functionSignatures);
        this.scopeBuilder = scopeBuilder;
        this.crossContextService = crossContextService;
    }
}
