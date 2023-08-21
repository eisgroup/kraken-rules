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

import static kraken.message.SystemMessageBuilder.Message.KRAKEN_PROJECT_BUILD_CONTEXT_DEFINITIONS_NOT_UNIQUE;
import static kraken.message.SystemMessageBuilder.Message.KRAKEN_PROJECT_BUILD_DIMENSIONS_NOT_UNIQUE;
import static kraken.message.SystemMessageBuilder.Message.KRAKEN_PROJECT_BUILD_EXTERNAL_CONTEXT_DUPLICATE;
import static kraken.message.SystemMessageBuilder.Message.KRAKEN_PROJECT_BUILD_FUNCTION_NOT_UNIQUE;
import static kraken.message.SystemMessageBuilder.Message.KRAKEN_PROJECT_BUILD_FUNCTION_SIGNATURE_NOT_UNIQUE;
import static kraken.message.SystemMessageBuilder.Message.KRAKEN_PROJECT_BUILD_NAMESPACE_INCLUDE_UNKNOWN;
import static kraken.message.SystemMessageBuilder.Message.KRAKEN_PROJECT_BUILD_NAMESPACE_STRUCTURE_INVALID;
import static kraken.message.SystemMessageBuilder.Message.KRAKEN_PROJECT_BUILD_NAMESPACE_UNKNOWN;
import static kraken.message.SystemMessageBuilder.Message.KRAKEN_PROJECT_BUILD_NO_CONTEXT_DEFINITIONS;
import static kraken.message.SystemMessageBuilder.Message.KRAKEN_PROJECT_BUILD_NO_ROOT_CONTEXT_DEFINITION;
import static kraken.message.SystemMessageBuilder.Message.KRAKEN_PROJECT_BUILD_RULE_IMPORT_DUPLICATE;
import static kraken.message.SystemMessageBuilder.Message.KRAKEN_PROJECT_BUILD_RULE_IMPORT_NAMESPACE_UNKNOWN;
import static kraken.message.SystemMessageBuilder.Message.KRAKEN_PROJECT_BUILD_RULE_IMPORT_UNKNOWN;
import static kraken.message.SystemMessageBuilder.Message.KRAKEN_PROJECT_BUILD_RULE_NOT_IN_ENTRYPOINT;
import static kraken.model.project.builder.ContextDefinitionEquality.areEqual;
import static kraken.utils.MessageUtils.withSpaceBeforeEachLine;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import kraken.message.SystemMessage;
import kraken.message.SystemMessageBuilder;
import kraken.message.SystemMessageLogger;
import kraken.model.Dimension;
import kraken.model.Function;
import kraken.model.FunctionSignature;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.project.KrakenProject;
import kraken.model.project.KrakenProjectBuilder;
import kraken.model.project.ResourceKrakenProject;
import kraken.model.project.builder.NamespaceNode.NamespaceProjection;
import kraken.model.project.exception.IllegalKrakenProjectStateException;
import kraken.model.project.validator.Duplicates;
import kraken.model.resource.Resource;
import kraken.model.resource.RuleImport;

/**
 * Creates {@link ResourceKrakenProject} projections for namespace from a list of {@link Resource}
 *
 * @author mulevicius
 */
public class ResourceKrakenProjectBuilder implements KrakenProjectBuilder {

    private static final SystemMessageLogger logger = SystemMessageLogger.getLogger(ResourceKrakenProjectBuilder.class);

    private final Map<String, NamespacedResource> resources;

    public ResourceKrakenProjectBuilder(Collection<Resource> resourceList) {
        this.resources = resourceList.stream()
                .collect(Collectors.groupingBy(Resource::getNamespace,
                        Collectors.collectingAndThen(Collectors.toList(), resources -> {
                            String namespaceName = resources.get(0).getNamespace();

                            ensureUniqueContextDefinitionNames(namespaceName, resources);
                            ensureUniqueCustomFunctionSignatures(namespaceName, resources);
                            ensureUniqueCustomFunctions(namespaceName, resources);
                            ensureUniqueDimensions(namespaceName, resources);

                            NamespacedResource namespacedResource = new NamespacedResource(namespaceName);
                            for(Resource resource : resources) {
                                resource.getContextDefinitions().forEach(namespacedResource::addContextDefinition);
                                resource.getExternalContextDefinitions().forEach(namespacedResource::addExternalContextDefinition);
                                resource.getRules().forEach(namespacedResource::addRule);
                                resource.getEntryPoints().forEach(namespacedResource::addEntryPoint);
                                resource.getRuleImports().forEach(namespacedResource::addRuleImport);
                                resource.getIncludes().forEach(namespacedResource::addInclude);
                                resource.getFunctionSignatures().forEach(namespacedResource::addFunctionSignature);
                                resource.getFunctions().forEach(namespacedResource::addFunction);
                                resource.getDimensions().forEach(namespacedResource::addDimension);
                                setExternalContext(namespacedResource, resource.getExternalContext());
                            }
                            return namespacedResource;
                })));
        validateNamespacedResources(resources);
        this.resources.values().forEach(namespacedResource -> importRules(namespacedResource, resources));
    }

    public KrakenProject buildKrakenProject(String namespace) {
        if(!resources.containsKey(namespace)) {
            var m = SystemMessageBuilder.create(KRAKEN_PROJECT_BUILD_NAMESPACE_UNKNOWN)
                .parameters(namespace)
                .build();
            throw new IllegalKrakenProjectStateException(m);
        }

        NamespaceTree namespaceTree = NamespaceTree.create(namespace, resources);
        NamespaceProjection namespaceProjection = namespaceTree.getRoot().buildNamespaceProjection();
        return build(namespace, namespaceProjection, namespaceTree);
    }

    private ResourceKrakenProject build(String namespace, NamespaceProjection namespaceProjection, NamespaceTree namespaceTree) {
        if (namespaceProjection.getContextDefinitions() == null || namespaceProjection.getContextDefinitions().isEmpty()) {
            var m = SystemMessageBuilder.create(KRAKEN_PROJECT_BUILD_NO_CONTEXT_DEFINITIONS)
                .parameters(namespace)
                .build();
            throw new IllegalKrakenProjectStateException(m);
        }

        var roots = namespaceProjection.getContextDefinitions().stream()
                .filter(c -> c.getPhysicalNamespace().equals(namespace))
                .filter(ContextDefinition::isRoot)
                .map(ContextDefinition::getName)
                .collect(Collectors.toList());

        if (roots.isEmpty()) {
            var m = SystemMessageBuilder.create(KRAKEN_PROJECT_BUILD_NO_ROOT_CONTEXT_DEFINITION)
                .parameters(namespace)
                .build();
            throw new IllegalKrakenProjectStateException(m);
        }
        if (roots.size() > 1) {
            var m = SystemMessageBuilder.create(KRAKEN_PROJECT_BUILD_NO_ROOT_CONTEXT_DEFINITION)
                .parameters(namespace, roots)
                .build();
            throw new IllegalKrakenProjectStateException(m);
        }

        String rootContextName = roots.get(0);

        Map<String, ContextDefinition> contextDefinitions = namespaceProjection.getContextDefinitions().stream()
                .collect(Collectors.toMap(ContextDefinition::getName, c -> c));

        ExternalContext externalContext = namespaceProjection.getExternalContext();

        Map<String, ExternalContextDefinition> externalContextDefinitions = namespaceProjection.getExternalContextDefinitions()
                .stream()
                .collect(Collectors.toMap(ExternalContextDefinition::getName, c -> c));

        List<EntryPoint> entryPoints = new ArrayList<>(namespaceProjection.getEntryPoints());

        Set<String> includedRules = entryPoints.stream().flatMap(e -> e.getRuleNames().stream()).collect(Collectors.toSet());

        List<Rule> rules = namespaceProjection.getRules().stream()
            .filter(r -> {
                boolean ruleIsIncludedInEntryPoint = includedRules.contains(r.getName());
                if(!ruleIsIncludedInEntryPoint) {
                    logger.warn(KRAKEN_PROJECT_BUILD_RULE_NOT_IN_ENTRYPOINT, r.getName(), namespace);
                }
                return ruleIsIncludedInEntryPoint;
            })
            .collect(Collectors.toList());

        List<FunctionSignature> functionSignatures = new ArrayList<>(namespaceProjection.getFunctionSignatures().values());
        List<Function> functions = new ArrayList<>(namespaceProjection.getFunctions().values());
        List<Dimension> dimensions = new ArrayList<>(namespaceProjection.getDimensions());

        return new ResourceKrakenProject(
            namespace,
            rootContextName,
            contextDefinitions,
            entryPoints,
            rules,
            externalContext,
            externalContextDefinitions,
            convertTree(namespaceTree),
            functionSignatures,
            functions,
            dimensions
        );
    }

    private static kraken.model.project.NamespaceTree convertTree(NamespaceTree namespaceTree) {
        return new kraken.model.project.NamespaceTree(convertNode(namespaceTree.getRoot()));
    }

    private static kraken.model.project.NamespaceNode convertNode(NamespaceNode node) {
        return new kraken.model.project.NamespaceNode(node.getName(),
                node.getChildren()
                        .stream()
                        .map(childNode -> convertNode(childNode))
                        .collect(Collectors.toList()));
    }

    private static void ensureUniqueCustomFunctions(String namespaceName, List<Resource> namespaceResources) {
        Duplicates.findAndDo(
            namespaceResources.stream().flatMap(r -> r.getFunctions().stream()),
            duplicates -> {
                String resourceUris = namespaceResources.stream()
                    .map(Resource::getUri)
                    .map(URI::toString)
                    .collect(Collectors.joining(System.lineSeparator()));
                String functionNames = duplicates.stream()
                    .map(Function::getName)
                    .collect(Collectors.joining(System.lineSeparator()));

                var m = SystemMessageBuilder.create(KRAKEN_PROJECT_BUILD_FUNCTION_NOT_UNIQUE)
                    .parameters(
                        namespaceName,
                        System.lineSeparator() + functionNames + System.lineSeparator(),
                        System.lineSeparator() + resourceUris)
                    .build();

                throw new IllegalKrakenProjectStateException(m);
            });
    }

    private static void ensureUniqueCustomFunctionSignatures(String namespaceName, List<Resource> namespaceResources) {
        Duplicates.findAndDo(
            namespaceResources.stream().flatMap(r -> r.getFunctionSignatures().stream()),
            FunctionSignature::toHeader,
            duplicates -> {
                String resourceUris = namespaceResources.stream()
                    .map(Resource::getUri)
                    .map(URI::toString)
                    .collect(Collectors.joining(System.lineSeparator()));
                String functionSignatures = duplicates.stream().map(FunctionSignature::format)
                    .collect(Collectors.joining(System.lineSeparator()));

                var m = SystemMessageBuilder.create(KRAKEN_PROJECT_BUILD_FUNCTION_SIGNATURE_NOT_UNIQUE)
                    .parameters(
                        namespaceName,
                        System.lineSeparator() + functionSignatures + System.lineSeparator(),
                        System.lineSeparator() + resourceUris)
                    .build();

                throw new IllegalKrakenProjectStateException(m);
            }
        );
    }

    private static void ensureUniqueContextDefinitionNames(String namespaceName, List<Resource> namespaceResources) {
        Set<ContextDefinition> duplicateContextDefinitions = new HashSet<>();
        namespaceResources.stream()
                .flatMap(r -> r.getContextDefinitions().stream())
                .collect(Collectors.groupingBy(ContextDefinition::getName,
                        Collectors.collectingAndThen(Collectors.toList(), contextDefinitions -> {
                            if(!sameContextDefinitions(contextDefinitions)) {
                                duplicateContextDefinitions.add(contextDefinitions.get(0));
                            }
                            return contextDefinitions;
                        }))
                );
        if(!duplicateContextDefinitions.isEmpty()) {
            String duplicateNames = duplicateContextDefinitions.stream()
                    .map(ContextDefinition::getName)
                    .collect(Collectors.joining(", "));

            var m = SystemMessageBuilder.create(KRAKEN_PROJECT_BUILD_CONTEXT_DEFINITIONS_NOT_UNIQUE)
                .parameters(namespaceName, duplicateNames)
                .build();

            throw new IllegalKrakenProjectStateException(m);
        }
    }

    private static void ensureUniqueDimensions(String namespaceName, List<Resource> namespaceResources) {
        Duplicates.findAndDo(
            namespaceResources.stream().flatMap(resource -> resource.getDimensions().stream()),
            duplicates -> {
                String resourceUris = namespaceResources.stream()
                    .map(Resource::getUri)
                    .map(URI::toString)
                    .collect(Collectors.joining(System.lineSeparator()));
                String dimensions = duplicates.stream()
                    .map(Dimension::getName)
                    .collect(Collectors.joining(System.lineSeparator()));

                var m = SystemMessageBuilder.create(KRAKEN_PROJECT_BUILD_DIMENSIONS_NOT_UNIQUE)
                    .parameters(
                        namespaceName,
                        System.lineSeparator() + dimensions + System.lineSeparator(),
                        System.lineSeparator() + resourceUris)
                    .build();

                throw new IllegalKrakenProjectStateException(m);
            });
    }

    private static boolean sameContextDefinitions(List<ContextDefinition> contextDefinitions) {
        if(contextDefinitions.size() <= 1) {
            return true;
        }
        ContextDefinition first = contextDefinitions.get(0);
        return contextDefinitions.stream().skip(1).allMatch(c -> areEqual(c, first));
    }

    private static void importRules(NamespacedResource namespacedResource,
                                    Map<String, NamespacedResource> namespacedResources) {
        Map<String, List<Rule>> importedRules = new HashMap<>();
        for(RuleImport ruleImport : namespacedResource.getRuleImports()) {
            String importedRuleName = ruleImport.getRuleName();
            List<Rule> rules = namespacedResources.get(ruleImport.getNamespace()).getRules().get(importedRuleName);
            importedRules.put(importedRuleName, rules);
        }
        importedRules.values().stream().flatMap(r -> r.stream()).forEach(rule -> namespacedResource.addRule(rule));
    }

    private static void validateNamespacedResources(Map<String, NamespacedResource> namespacedResources) {
        List<SystemMessage> errorMessages = new ArrayList<>();
        for(NamespacedResource namespacedResource : namespacedResources.values()) {
            for(String include : namespacedResource.getIncludes()) {
                validateIncludeNamespaceExistence(include, namespacedResource, namespacedResources, errorMessages);
            }
            validateRuleImports(namespacedResource, namespacedResources, errorMessages);
        }

        if(!errorMessages.isEmpty()) {
            var formattedMessages = errorMessages.stream()
                .map(SystemMessage::formatMessageWithCode)
                .collect(Collectors.joining(System.lineSeparator()));
            var msg = SystemMessageBuilder.create(KRAKEN_PROJECT_BUILD_NAMESPACE_STRUCTURE_INVALID)
                .parameters(System.lineSeparator() + withSpaceBeforeEachLine(formattedMessages))
                .build();

            throw new IllegalKrakenProjectStateException(msg);
        }
    }

    private static void validateIncludeNamespaceExistence(String include,
                                                          NamespacedResource namespacedResource,
                                                          Map<String, NamespacedResource> namespacedResources,
                                                          List<SystemMessage> errorMessages) {
        if(!namespacedResources.containsKey(include)) {
            var m = SystemMessageBuilder.create(KRAKEN_PROJECT_BUILD_NAMESPACE_INCLUDE_UNKNOWN)
                .parameters(include, namespacedResource.getNamespace())
                .build();
            errorMessages.add(m);
        }
    }

    private static void validateRuleImports(NamespacedResource namespacedResource,
                                            Map<String, NamespacedResource> namespacedResources,
                                            List<SystemMessage> errorMessages) {
        validateRuleImportAmbiguity(errorMessages, namespacedResource);
        for(RuleImport ruleImport : namespacedResource.getRuleImports()) {
            validateRuleImportDuplicates(namespacedResource, errorMessages, ruleImport);
            validateRuleImportNamespaceExistence(ruleImport, namespacedResource, namespacedResources, errorMessages);
            validateRuleImportExistence(namespacedResource, namespacedResources, errorMessages, ruleImport);
        }
    }

    private static void validateRuleImportExistence(NamespacedResource namespacedResource,
                                                    Map<String, NamespacedResource> namespacedResources,
                                                    List<SystemMessage> errorMessages,
                                                    RuleImport ruleImport) {
        if(namespacedResources.containsKey(ruleImport.getNamespace())) {
            String importedRuleName = ruleImport.getRuleName();
            List<Rule> rules = namespacedResources.get(ruleImport.getNamespace()).getRules().get(importedRuleName);
            if (rules == null || rules.isEmpty()) {
                var m = SystemMessageBuilder.create(KRAKEN_PROJECT_BUILD_RULE_IMPORT_UNKNOWN)
                    .parameters(ruleImport.getRuleName(), ruleImport.getNamespace(), namespacedResource.getNamespace())
                    .build();
                errorMessages.add(m);
            }
        }
    }

    private static void validateRuleImportNamespaceExistence(RuleImport ruleImport,
                                                             NamespacedResource namespacedResource,
                                                             Map<String, NamespacedResource> namespacedResources,
                                                             List<SystemMessage> errorMessages) {
        if(!namespacedResources.containsKey(ruleImport.getNamespace())) {
            var m = SystemMessageBuilder.create(KRAKEN_PROJECT_BUILD_RULE_IMPORT_NAMESPACE_UNKNOWN)
                .parameters(ruleImport.getRuleName(), ruleImport.getNamespace(), namespacedResource.getNamespace())
                .build();
            errorMessages.add(m);
        }
    }

    private static void validateRuleImportDuplicates(NamespacedResource namespacedResource,
                                                     List<SystemMessage> errorMessages,
                                                     RuleImport ruleImport) {
        if(namespacedResource.getRules().containsKey(ruleImport.getRuleName())) {
            var m = SystemMessageBuilder.create(KRAKEN_PROJECT_BUILD_RULE_IMPORT_DUPLICATE)
                .parameters(ruleImport.getRuleName(), ruleImport.getNamespace(), namespacedResource.getNamespace())
                .build();
            errorMessages.add(m);
        }
    }

    private static void validateRuleImportAmbiguity(List<SystemMessage> errorMessages, NamespacedResource namespacedResource) {
        Map<String, List<RuleImport>> groupedImports = namespacedResource.getRuleImports().stream()
                .collect(Collectors.groupingBy(RuleImport::getRuleName));
        for(Map.Entry<String, List<RuleImport>> entry : groupedImports.entrySet()) {
            if(entry.getValue().size() > 1) {
                var namespaceImports = entry.getValue().stream()
                    .map(RuleImport::getNamespace)
                    .collect(Collectors.joining(", "));
                var m = SystemMessageBuilder.create(KRAKEN_PROJECT_BUILD_RULE_IMPORT_DUPLICATE)
                    .parameters(namespacedResource.getNamespace(), entry.getKey(), namespaceImports)
                    .build();
                errorMessages.add(m);
            }
        }
    }

    private void setExternalContext(NamespacedResource namespacedResource, ExternalContext externalContext) {
        if (externalContext != null) {
            if (namespacedResource.getExternalContext() != null
                && !externalContext.equals(namespacedResource.getExternalContext())) {
                var m = SystemMessageBuilder.create(KRAKEN_PROJECT_BUILD_EXTERNAL_CONTEXT_DUPLICATE)
                    .parameters(namespacedResource.getNamespace())
                    .build();
                throw new IllegalKrakenProjectStateException(m);
            }

            namespacedResource.setExternalContext(externalContext);
        }
    }
}
