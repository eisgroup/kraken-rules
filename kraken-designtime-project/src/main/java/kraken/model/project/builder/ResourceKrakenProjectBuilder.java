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

import static kraken.model.project.builder.ContextDefinitionEquality.areEqual;

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(ResourceKrakenProjectBuilder.class);

    private final Map<String, NamespacedResource> resources;

    public ResourceKrakenProjectBuilder(Collection<Resource> resourceList) {
        this.resources = resourceList.stream()
                .collect(Collectors.groupingBy(Resource::getNamespace,
                        Collectors.collectingAndThen(Collectors.toList(), resources -> {
                            String namespaceName = resources.get(0).getNamespace();

                            ensureUniqueContextDefinitionNames(namespaceName, resources);
                            ensureUniqueCustomFunctionSignatures(namespaceName, resources);
                            ensureUniqueCustomFunctions(namespaceName, resources);

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
                                setExternalContext(namespacedResource, resource.getExternalContext());
                            }
                            return namespacedResource;
                })));
        validateNamespacedResources(resources);
        this.resources.values().forEach(namespacedResource -> importRules(namespacedResource, resources));
    }

    public KrakenProject buildKrakenProject(String namespace) {
        if(!resources.containsKey(namespace)) {
            throw new IllegalKrakenProjectStateException("Trying to create KrakenProject for namespace " +
                    "that does not exist: " + namespace);
        }

        NamespaceTree namespaceTree = NamespaceTree.create(namespace, resources);
        NamespaceProjection namespaceProjection = namespaceTree.getRoot().buildNamespaceProjection();
        return build(namespace, namespaceProjection, namespaceTree);
    }

    private ResourceKrakenProject build(String namespace, NamespaceProjection namespaceProjection, NamespaceTree namespaceTree) {
        if (namespaceProjection.getContextDefinitions() == null || namespaceProjection.getContextDefinitions().isEmpty()) {
            String template = "KrakenProject for namespace {0} does not have any context definition defined. " +
                    "At least one context definition is expected.";
            String message = MessageFormat.format(template, namespace);

            throw new IllegalKrakenProjectStateException(message);
        }

        final List<String> roots = namespaceProjection.getContextDefinitions().stream()
                .filter(c -> c.getPhysicalNamespace().equals(namespace))
                .filter(ContextDefinition::isRoot)
                .map(ContextDefinition::getName)
                .collect(Collectors.toList());

        if (roots.isEmpty()) {
            String template = "KrakenProject for namespace {0} does not have a Root Context defined.";
            String message = MessageFormat.format(template, namespace);
            throw new IllegalKrakenProjectStateException(message);
        }
        if (roots.size() > 1) {
            String template = "KrakenProject must have exactly one Root Context, but multiple Root Context found in KrakenProject for namespace {0}: {1}";
            String message = MessageFormat.format(template, namespace, roots);
            throw new IllegalKrakenProjectStateException(message);
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
                    String template = "Rule ''{0}'' is not included into any EntryPoint and is unused in KrakenProject for namespace {1}. " +
                        "This Rule will be removed from KrakenProject and excluded from any further calculations. " +
                        "Such KrakenProject configurations may not be supported in the future.";
                    String message = MessageFormat.format(template, r.getName(), namespace);
                    logger.warn(message);
                }
                return ruleIsIncludedInEntryPoint;
            })
            .collect(Collectors.toList());

        List<FunctionSignature> functionSignatures = new ArrayList<>(namespaceProjection.getFunctionSignatures().values());
        List<Function> functions = new ArrayList<>(namespaceProjection.getFunctions().values());

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
            functions
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

                String msg = String.format(
                    "Functions defined in DSL must be unique by function name, "
                        + "but duplicate functions are defined in namespace '%s':"
                        + System.lineSeparator()
                        + "%s"
                        + System.lineSeparator()
                        + "Please review affected DSL resources and remove duplicated functions:"
                        + System.lineSeparator()
                        + "%s",
                    namespaceName,
                    duplicates.stream().map(Function::getName).collect(Collectors.joining(System.lineSeparator())),
                    resourceUris
                );
                throw new IllegalKrakenProjectStateException(msg);
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
                String msg = String.format(
                    "Function signatures defined in DSL must be unique by function name and parameter "
                        + "count, but duplicate function signatures are defined in namespace '%s':"
                        + System.lineSeparator()
                        + "%s"
                        + System.lineSeparator()
                        + "Please review affected DSL resources and remove duplicated function signatures:"
                        + System.lineSeparator()
                        + "%s",
                    namespaceName,
                    duplicates.stream().map(FunctionSignature::format)
                        .collect(Collectors.joining(System.lineSeparator())),
                    resourceUris
                );
                throw new IllegalKrakenProjectStateException(msg);
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

            String msg = String.format("Duplicate ContextDefinition definitions found in namespace '%s': %s",
                    namespaceName, duplicateNames);
            throw new IllegalKrakenProjectStateException(msg);
        }
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
        List<String> errorMessages = new ArrayList<>();
        for(NamespacedResource namespacedResource : namespacedResources.values()) {
            for(String include : namespacedResource.getIncludes()) {
                validateIncludeNamespaceExistence(include, namespacedResource, namespacedResources, errorMessages);
            }
            validateRuleImports(namespacedResource, namespacedResources, errorMessages);
        }

        if(!errorMessages.isEmpty()) {
            String msg = String.format("Namespace structure is invalid. Errors: %s",
                    String.join(System.lineSeparator(), errorMessages));
            throw new IllegalKrakenProjectStateException(msg);
        }
    }

    private static void validateIncludeNamespaceExistence(String include,
                                                             NamespacedResource namespacedResource,
                                                             Map<String, NamespacedResource> namespacedResources,
                                                             List<String> errorMessages) {
        if(!namespacedResources.containsKey(include)) {
            String msg = String.format("Cannot include namespace '%s' to '%s', because namespace does not exist.",
                    include,
                    namespacedResource.getNamespace());

            errorMessages.add(msg);
        }
    }

    private static void validateRuleImports(NamespacedResource namespacedResource,
                                            Map<String, NamespacedResource> namespacedResources,
                                            List<String> errorMessages) {
        validateRuleImportAmbiguity(errorMessages, namespacedResource);
        for(RuleImport ruleImport : namespacedResource.getRuleImports()) {
            validateRuleImportDuplicates(namespacedResource, errorMessages, ruleImport);
            validateRuleImportNamespaceExistence(ruleImport, namespacedResource, namespacedResources, errorMessages);
            validateRuleImportExistence(namespacedResource, namespacedResources, errorMessages, ruleImport);
        }
    }

    private static void validateRuleImportExistence(NamespacedResource namespacedResource,
                                                    Map<String, NamespacedResource> namespacedResources,
                                                    List<String> errorMessages,
                                                    RuleImport ruleImport) {
        if(namespacedResources.containsKey(ruleImport.getNamespace())) {
            String importedRuleName = ruleImport.getRuleName();
            List<Rule> rules = namespacedResources.get(ruleImport.getNamespace()).getRules().get(importedRuleName);
            if (rules == null || rules.isEmpty()) {
                String msg = String.format("Cannot import rule '%s' from namespace '%s' to '%s', " +
                                "because rule does not exist.",
                        ruleImport.getRuleName(),
                        ruleImport.getNamespace(),
                        namespacedResource.getNamespace());

                errorMessages.add(msg);
            }
        }
    }

    private static void validateRuleImportNamespaceExistence(RuleImport ruleImport,
                                                             NamespacedResource namespacedResource,
                                                             Map<String, NamespacedResource> namespacedResources,
                                                             List<String> errorMessages) {
        if(!namespacedResources.containsKey(ruleImport.getNamespace())) {
            String msg = String.format("Cannot import rule '%s' from namespace '%s' to '%s', " +
                            "because namespace does not exist.",
                    ruleImport.getRuleName(),
                    ruleImport.getNamespace(),
                    namespacedResource.getNamespace());

            errorMessages.add(msg);
        }
    }

    private static void validateRuleImportDuplicates(NamespacedResource namespacedResource,
                                                     List<String> errorMessages,
                                                     RuleImport ruleImport) {
        if(namespacedResource.getRules().containsKey(ruleImport.getRuleName())) {
            String msg = String.format("Cannot import rule '%s' from namespace '%s' to '%s', " +
                            "because rule is already defined.",
                    ruleImport.getRuleName(),
                    ruleImport.getNamespace(),
                    namespacedResource.getNamespace());

            errorMessages.add(msg);
        }
    }

    private static void validateRuleImportAmbiguity(List<String> errorMessages, NamespacedResource namespacedResource) {
        Map<String, List<RuleImport>> groupedImports = namespacedResource.getRuleImports().stream()
                .collect(Collectors.groupingBy(RuleImport::getRuleName));
        for(Map.Entry<String, List<RuleImport>> entry : groupedImports.entrySet()) {
            if(entry.getValue().size() > 1) {
                String msg = String.format("Ambiguous import found in namespace '%s' for rule '%s', " +
                                "because it is imported from multiple namespaces: %s.",
                        namespacedResource.getNamespace(),
                        entry.getKey(),
                        entry.getValue().stream().map(RuleImport::getNamespace).collect(Collectors.joining(", ")));
                errorMessages.add(msg);
            }
        }
    }

    private void setExternalContext(NamespacedResource namespacedResource, ExternalContext externalContext) {
        if (externalContext != null) {
            if (namespacedResource.getExternalContext() != null
                && !externalContext.equals(namespacedResource.getExternalContext())) {
                String template = "Multiple conflicting External Contexts defined in namespace %s. "
                    + "Only one External Context can be configured per namespace.";
                throw new IllegalKrakenProjectStateException(
                    String.format(template, namespacedResource.getNamespace()));
            }

            namespacedResource.setExternalContext(externalContext);
        }
    }
}
