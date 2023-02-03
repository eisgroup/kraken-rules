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

import static kraken.model.context.PrimitiveFieldDataType.STRING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import kraken.el.scope.type.Type;
import kraken.model.Expression;
import kraken.model.Function;
import kraken.model.FunctionDocumentation;
import kraken.model.FunctionParameter;
import kraken.model.FunctionSignature;
import kraken.model.GenericTypeBound;
import kraken.model.Rule;
import kraken.model.context.Cardinality;
import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.model.context.ContextNavigation;
import kraken.model.context.PrimitiveFieldDataType;
import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.context.external.ExternalContextDefinitionAttribute;
import kraken.model.context.external.ExternalContextDefinitionAttributeType;
import kraken.model.context.external.ExternalContextDefinitionReference;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.factory.RulesModelFactory;
import kraken.model.resource.Resource;
import kraken.model.resource.RuleImport;
import kraken.model.state.AccessibilityPayload;
import kraken.namespace.Namespaced;
import kraken.utils.ResourceUtils;

/**
 * Use to mock {@link KrakenProject} and it's contents for testing purposes
 *
 * @author mulevicius
 */
public class KrakenProjectMocks {

    public static final String DEFAULT_NAMESPACE = "Base";

    public static RulesModelFactory factory = RulesModelFactory.getInstance();

    public static List<ContextDefinition> contextDefinitionsWithRoot(String ...name) {
        List<ContextDefinition> contextDefinitions = contextDefinitions(name);
        if(!contextDefinitions.isEmpty()) {
            contextDefinitions.get(0).setRoot(true);
        }
        return contextDefinitions;
    }

    public static List<ExternalContextDefinition> externalContextDefinitions(String... name) {
        return Arrays.stream(name).map(extDefName -> toExternalContextDefinition(extDefName))
                .collect(Collectors.toList());
    }

    public static ExternalContext externalContext() {
        ExternalContext externalContext = factory.createExternalContext();
        externalContext.setName("ExternalContext");
        externalContext.setPhysicalNamespace(DEFAULT_NAMESPACE);

        return externalContext;
    }

    public static ExternalContext externalContext(String name, String namespace) {
        ExternalContext externalContext = factory.createExternalContext();
        externalContext.setName(name);
        externalContext.setPhysicalNamespace(namespace);

        return externalContext;
    }

    public static ExternalContext externalContext(List<String> childContexts) {
        ExternalContext externalContext = factory.createExternalContext();
        externalContext.setName("ExternalEntity_root");
        externalContext.setPhysicalNamespace(DEFAULT_NAMESPACE);

        childContexts
                .forEach(childCtxKey -> externalContext.getContexts().put(childCtxKey, externalContext()));

        return externalContext;
    }

    public static List<ContextDefinition> contextDefinitions(String ...name) {
        return Arrays.stream(name).map(n -> toContextDefinition(n)).collect(Collectors.toList());
    }

    public static List<EntryPoint> entryPoints(String ...name) {
        return Arrays.stream(name).map(n -> toEntryPoint(n)).collect(Collectors.toList());
    }

    public static List<Rule> rules(String ...name) {
        return Arrays.stream(name).map(n -> toRule(n)).collect(Collectors.toList());
    }

    public static List<String> includes(String ...includes) {
        return List.of(includes);
    }

    public static List<RuleImport> imports(RuleImport ...ruleImports) {
        return List.of(ruleImports);
    }

    public static RuleImport ri(String namespace, String rule) {
        return new RuleImport(namespace, rule);
    }

    public static ContextDefinition toContextDefinition(String name) {
        ContextDefinition contextDefinition = factory.createContextDefinition();
        contextDefinition.setName(name);
        return contextDefinition;
    }

    public static ContextDefinition toSystemContextDefinition(String name) {
        ContextDefinition contextDefinition = factory.createContextDefinition();
        contextDefinition.setName(name);
        contextDefinition.setSystem(true);
        contextDefinition.setStrict(true);

        return contextDefinition;
    }

    public static ExternalContextDefinition toExternalContextDefinition(String name) {
        ExternalContextDefinition externalContextDefinition = factory.createExternalContextDefinition();
        externalContextDefinition.setName(name);

        return externalContextDefinition;
    }

    public static Rule toRule(String name) {
        Rule rule = factory.createRule();
        rule.setName(name);
        return rule;
    }

    public static EntryPoint toEntryPoint(String name) {
        EntryPoint entryPoint = factory.createEntryPoint();
        entryPoint.setName(name);
        return entryPoint;
    }

    public static Resource resource(String ns,
                                     List<ContextDefinition> contextDefinitions,
                                     List<EntryPoint> entryPoints,
                                     List<Rule> rules) {
        return resource(ns, contextDefinitions, null, List.of(), entryPoints, rules, List.of());
    }

    public static Resource resource(String ns,
                                    List<ContextDefinition> contextDefinitions,
                                    List<EntryPoint> entryPoints,
                                    List<Rule> rules,
                                    List<String> includes) {
        return resource(ns, contextDefinitions, null, List.of(), entryPoints, rules, includes);
    }

    public static Resource resource(String ns,
                                    List<ContextDefinition> contextDefinitions,
                                    List<EntryPoint> entryPoints,
                                    List<Rule> rules,
                                    List<String> includes,
                                    List<RuleImport> ruleImports) {
        return resource(ns, contextDefinitions, null, List.of(), entryPoints, rules, includes, ruleImports);
    }

    public static Resource resource(String ns,
                                    List<ContextDefinition> contextDefinitions,
                                    ExternalContext externalContext,
                                    List<ExternalContextDefinition> externalContextDefinitions,
                                    List<EntryPoint> entryPoints,
                                    List<Rule> rules) {
        return resource(ns, contextDefinitions, externalContext, externalContextDefinitions, entryPoints, rules, List.of());
    }

    public static Resource resource(String ns,
                                    List<ContextDefinition> contextDefinitions,
                                    ExternalContext externalContext,
                                    List<ExternalContextDefinition> externalContextDefinitions,
                                    List<EntryPoint> entryPoints,
                                    List<Rule> rules,
                                    List<String> includes) {
        return resource(ns, contextDefinitions, externalContext, externalContextDefinitions, entryPoints, rules, includes, List.of());
    }

    public static Resource resource(String ns, List<FunctionSignature> f, List<String> includes) {
        return resource(ns, contextDefinitionsWithRoot("Policy"), null, List.of(), List.of(), List.of(), includes, List.of(), f);
    }

    public static Resource resource(String ns,
                                    List<ContextDefinition> contextDefinitions,
                                    ExternalContext externalContext,
                                    List<ExternalContextDefinition> externalContextDefinitions,
                                    List<EntryPoint> entryPoints,
                                    List<Rule> rules,
                                    List<String> includes,
                                    List<RuleImport> ruleImports) {
        return resource(
            ns,
            contextDefinitions,
            externalContext,
            externalContextDefinitions,
            entryPoints,
            rules,
            includes,
            ruleImports,
            List.of()
        );
    }

    public static Resource resource(String ns,
                                    List<ContextDefinition> contextDefinitions,
                                    ExternalContext externalContext,
                                    List<ExternalContextDefinition> externalContextDefinitions,
                                    List<EntryPoint> entryPoints,
                                    List<Rule> rules,
                                    List<String> includes,
                                    List<RuleImport> ruleImports,
                                    List<FunctionSignature> functionSignatures) {
        applyPhysicalNamespace(ns, externalContextDefinitions);
        applyPhysicalNamespace(ns, contextDefinitions);
        applyPhysicalNamespace(ns, entryPoints);
        applyPhysicalNamespace(ns, rules);
        applyPhysicalNamespace(ns, functionSignatures);

        return resource(
            ns,
            contextDefinitions,
            externalContext,
            externalContextDefinitions,
            entryPoints,
            rules,
            includes,
            ruleImports,
            functionSignatures,
            List.of()
        );
    }

    public static Resource resource(String ns,
                                    List<ContextDefinition> contextDefinitions,
                                    ExternalContext externalContext,
                                    List<ExternalContextDefinition> externalContextDefinitions,
                                    List<EntryPoint> entryPoints,
                                    List<Rule> rules,
                                    List<String> includes,
                                    List<RuleImport> ruleImports,
                                    List<FunctionSignature> functionSignatures,
                                    List<Function> functions) {
        applyPhysicalNamespace(ns, externalContextDefinitions);
        applyPhysicalNamespace(ns, contextDefinitions);
        applyPhysicalNamespace(ns, entryPoints);
        applyPhysicalNamespace(ns, rules);
        applyPhysicalNamespace(ns, functionSignatures);

        return new Resource(
            ns,
            contextDefinitions,
            entryPoints,
            rules,
            includes,
            ruleImports,
            externalContext,
            externalContextDefinitions,
            functionSignatures,
            functions,
            ResourceUtils.randomResourceUri()
        );
    }

    public static Resource resource(String ns,
                                    List<ContextDefinition> contextDefinitions,
                                    ExternalContext externalContext,
                                    List<ExternalContextDefinition> externalContextDefinitions) {
        return resource(ns, contextDefinitions, externalContext, externalContextDefinitions,
            List.of(), List.of(), List.of(), List.of());
    }

    public static Resource resourceWithFunctions(String ns, List<Function> functions, List<String> includes) {
        applyPhysicalNamespace(ns, functions);

        return resource(
            ns,
            contextDefinitionsWithRoot("Policy"),
            null,
            List.of(),
            List.of(),
            List.of(),
            includes,
            List.of(),
            List.of(),
            functions
        );
    }

    private static <T extends Namespaced> void applyPhysicalNamespace(String ns, List<T> namespacedItems) {
        namespacedItems.forEach(namespaced -> namespaced.setPhysicalNamespace(ns));
    }

    public static KrakenProject krakenProject(List<ContextDefinition> contextDefinitions,
                                              List<EntryPoint> entryPoints,
                                              List<Rule> rules) {
        return krakenProject(
            contextDefinitions,
            null,
            List.of(),
            entryPoints,
            rules
        );
    }

    public static KrakenProject krakenProject(List<ContextDefinition> contextDefinitions,
                                              List<EntryPoint> entryPoints,
                                              List<Rule> rules,
                                              List<FunctionSignature> functionSignatures) {
        return krakenProject(
            contextDefinitions,
            null,
            List.of(),
            entryPoints,
            rules,
            functionSignatures
        );
    }

    public static KrakenProject krakenProjectWithFunctions(List<ContextDefinition> contextDefinitions,
                                                           List<FunctionSignature> functionSignatures,
                                                           List<Function> functions) {
        return krakenProject(
            contextDefinitions,
            null,
            List.of(),
            List.of(),
            List.of(),
            functionSignatures,
            functions
        );
    }

    public static KrakenProject krakenProjectWithFunctions(List<Function> functions) {
        return krakenProject(
            List.of(),
            null,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            functions
        );
    }

    public static KrakenProject krakenProjectWithFunctions(List<FunctionSignature> functionSignatures,
                                                           List<Function> functions) {
        return krakenProject(
            List.of(),
            null,
            List.of(),
            List.of(),
            List.of(),
            functionSignatures,
            functions
        );
    }

    public static KrakenProject krakenProject(List<ContextDefinition> contextDefinitions,
                                              ExternalContext externalContext,
                                              List<ExternalContextDefinition> externalContextDefinitions,
                                              List<EntryPoint> entryPoints,
                                              List<Rule> rules) {
        return krakenProject(
            contextDefinitions,
            externalContext,
            externalContextDefinitions,
            entryPoints,
            rules,
            List.of()
        );
    }

    public static KrakenProject krakenProject(List<ContextDefinition> contextDefinitions,
                                              ExternalContext externalContext,
                                              List<ExternalContextDefinition> externalContextDefinitions,
                                              List<EntryPoint> entryPoints,
                                              List<Rule> rules,
                                              List<FunctionSignature> functionSignatures) {
        return krakenProject(contextDefinitions, externalContext, externalContextDefinitions, entryPoints, rules,
            functionSignatures, List.of());
    }

    public static KrakenProject krakenProject(List<ContextDefinition> contextDefinitions,
                                              ExternalContext externalContext,
                                              List<ExternalContextDefinition> externalContextDefinitions,
                                              List<EntryPoint> entryPoints,
                                              List<Rule> rules,
                                              List<FunctionSignature> functionSignatures,
                                              List<Function> functions) {
        List<ContextDefinition> allContextDefinitions = new ArrayList<>(contextDefinitions);
        if(allContextDefinitions.isEmpty()) {
            ContextDefinition contextDefinition = factory.createContextDefinition();
            contextDefinition.setRoot(true);
            contextDefinition.setName("Root");
            contextDefinition.setPhysicalNamespace("Base");
            allContextDefinitions.add(contextDefinition);
        }

        return new ResourceKrakenProject(
            DEFAULT_NAMESPACE,
            allContextDefinitions.get(0).getName(),
            allContextDefinitions.stream().collect(Collectors.toMap(ContextDefinition::getName, c -> c)),
            entryPoints,
            rules,
            externalContext,
            externalContextDefinitions.stream().collect(Collectors.toMap(ExternalContextDefinition::getName, c -> c)),
            null,
            functionSignatures,
            functions
        );
    }

    public static ContextDefinition contextDefinition(String name, List<ContextField> fields) {
        return contextDefinition(name, fields, List.of());
    }

    public static ContextDefinition rootContextDefinition(String name, List<ContextField> fields) {
        final ContextDefinition contextDefinition = contextDefinition(name, fields, List.of());
        contextDefinition.setRoot(true);
        return contextDefinition;
    }

    public static ContextDefinition dynamicContextDefinition(String name, List<ContextField> fields, List<String> parents) {
        return contextDefinition(name, false, fields, parents, List.of());
    }

    public static ContextDefinition contextDefinition(String name, List<ContextField> fields, List<String> parents) {
        return contextDefinition(name, true, fields, parents, List.of());
    }

    public static ContextDefinition rootContextDefinition(String name, List<ContextField> fields, List<String> parents) {
        final ContextDefinition contextDefinition = contextDefinition(name, true, fields, parents, List.of());
        contextDefinition.setRoot(true);
        return contextDefinition;
    }

    public static ExternalContext externalContext(String name,
                                                  Map<String, ExternalContext> externalContexts,
                                                  Map<String, ExternalContextDefinitionReference> definitions) {
        ExternalContext externalContext = externalContext();
        externalContext.setName(name);
        externalContext.setPhysicalNamespace(DEFAULT_NAMESPACE);
        externalContext.setContexts(externalContexts);
        externalContext.setExternalContextDefinitions(definitions);

        return externalContext;
    }

    public static ExternalContextDefinition externalContextDefinition(String name,
                                                                      List<ExternalContextDefinitionAttribute> attributes) {
        ExternalContextDefinition externalContextDefinition = factory.createExternalContextDefinition();
        externalContextDefinition.setName(name);
        externalContextDefinition.setAttributes(attributes.stream()
                .collect(Collectors.toMap(ExternalContextDefinitionAttribute::getName, c -> c)));

        return externalContextDefinition;
    }

    public static ExternalContextDefinitionAttribute externalContextDefinitionAttribute(String name,
                                                                                        ExternalContextDefinitionAttributeType type) {
        ExternalContextDefinitionAttribute externalContextDefinitionAttribute = factory.createExternalContextDefinitionAttribute();
        externalContextDefinitionAttribute.setName(name);
        externalContextDefinitionAttribute.setType(type);

        return externalContextDefinitionAttribute;
    }

    public static ExternalContextDefinitionAttributeType externalContextDefinitionAttributeType(String type,
                                                                                                boolean isPrimitive,
                                                                                                Cardinality cardinality) {
        ExternalContextDefinitionAttributeType externalContextDefinitionAttributeType = factory.createExternalContextDefinitionAttributeType();
        externalContextDefinitionAttributeType.setPrimitive(isPrimitive);
        externalContextDefinitionAttributeType.setCardinality(cardinality);
        externalContextDefinitionAttributeType.setType(type);

        return externalContextDefinitionAttributeType;
    }

    public static ExternalContextDefinitionReference createExternalContextDefinitionReference(String name) {
        ExternalContextDefinitionReference reference = factory.createExternalContextDefinitionReference();
        reference.setName(name);

        return reference;
    }

    public static ContextDefinition contextDefinition(String name,
                                                       List<ContextField> fields,
                                                       List<String> parents,
                                                       List<ContextNavigation> children) {
        return contextDefinition(name, true, fields, parents, children);
    }


    public static ContextDefinition rootContextDefinition(String name,
                                                      List<ContextField> fields,
                                                      List<String> parents,
                                                      List<ContextNavigation> children) {
        final ContextDefinition contextDefinition = contextDefinition(name, true, fields, parents, children);
        contextDefinition.setRoot(true);
        return contextDefinition;
    }

    public static ContextDefinition contextDefinition(String name,
                                                       boolean strict,
                                                       List<ContextField> fields,
                                                       List<String> parents,
                                                       List<ContextNavigation> children) {
        ContextDefinition contextDefinition = factory.createContextDefinition();
        contextDefinition.setPhysicalNamespace(DEFAULT_NAMESPACE);
        contextDefinition.setName(name);
        contextDefinition.setContextFields(fields.stream().collect(Collectors.toMap(ContextField::getName, f -> f)));
        contextDefinition.setChildren(children.stream().collect(Collectors.toMap(ContextNavigation::getTargetName, n -> n)));
        contextDefinition.setParentDefinitions(parents);
        contextDefinition.setStrict(strict);
        return contextDefinition;
    }

    public static ContextNavigation arrayChild(String name) {
        return child(name, Cardinality.MULTIPLE);
    }

    public static ContextNavigation child(String name) {
        return child(name, Cardinality.SINGLE);
    }

    public static ContextNavigation child(String name, Cardinality cardinality) {
        ContextNavigation contextNavigation = factory.createContextNavigation();
        contextNavigation.setTargetName(name);
        contextNavigation.setNavigationExpression(name);
        contextNavigation.setCardinality(cardinality);
        return contextNavigation;
    }

    public static ContextField arrayField(String name) {
        return arrayField(name, STRING);
    }

    public static ContextField arrayField(String name, PrimitiveFieldDataType type) {
        return arrayField(name, type.toString());
    }

    public static ContextField arrayField(String name, String type) {
        return arrayField(name, name, type);
    }

    public static ContextField arrayField(String name, String path, PrimitiveFieldDataType type) {
        return arrayField(name, path, type.toString());
    }

    public static ContextField arrayField(String name, String path, String type) {
        return field(name, path, type, Cardinality.MULTIPLE);
    }


    public static ExternalContextDefinitionAttribute attribute(String name, ExternalContextDefinitionAttributeType type) {
        ExternalContextDefinitionAttribute externalContextDefinitionAttribute = factory.createExternalContextDefinitionAttribute();
        externalContextDefinitionAttribute.setName(name);
        externalContextDefinitionAttribute.setType(type);

        return externalContextDefinitionAttribute;
    }

    public static ExternalContextDefinitionAttributeType type(String type, Cardinality cardinality, boolean isPrimitive) {
        ExternalContextDefinitionAttributeType contextDefinitionAttributeType = factory.createExternalContextDefinitionAttributeType();
        contextDefinitionAttributeType.setType(type);
        contextDefinitionAttributeType.setCardinality(cardinality);
        contextDefinitionAttributeType.setPrimitive(isPrimitive);

        return contextDefinitionAttributeType;
    }

    public static ContextField field(String name) {
        return field(name, STRING);
    }

    public static ContextField field(String name, PrimitiveFieldDataType type) {
        return field(name, type.toString());
    }

    public static ContextField field(String name, String type) {
        return field(name, name, type);
    }

    public static ContextField field(String name, String path, PrimitiveFieldDataType type) {
        return field(name, path, type.toString());
    }

    public static ContextField field(String name, String path, String type) {
        return field(name, path, type, Cardinality.SINGLE);
    }

    public static ContextField field(String name, String path, String type, Cardinality cardinality) {
        ContextField contextField = factory.createContextField();
        contextField.setName(name);
        contextField.setFieldPath(path);
        contextField.setCardinality(cardinality);
        contextField.setExternal(false);
        contextField.setFieldType(type);
        return contextField;
    }

    public static ContextField externalField(String name) {
        ContextField contextField = field(name);
        contextField.setExternal(true);
        return contextField;
    }

    public static Rule rule(String name, String context, String field) {
        AccessibilityPayload accessibilityPayload = factory.createAccessibilityPayload();
        Rule rule = factory.createRule();
        rule.setPhysicalNamespace(DEFAULT_NAMESPACE);
        rule.setName(name);
        rule.setContext(context);
        rule.setTargetPath(field);
        rule.setPayload(accessibilityPayload);
        return rule;
    }

    public static EntryPoint serverSideOnlyEntryPoint(String name, List<String> rules) {
        return entryPoint(name, true, rules, List.of());
    }

    public static EntryPoint entryPoint(String name, String rule) {
        return entryPoint(name, List.of(rule), List.of());
    }

    public static EntryPoint entryPoint(String name, List<String> rules) {
        return entryPoint(name, rules, List.of());
    }

    public static EntryPoint entryPoint(String name, List<String> rules, List<String> includes) {
        return entryPoint(name, false, rules, includes);
    }

    public static EntryPoint entryPoint(String name, boolean sso, List<String> rules, List<String> includes) {
        EntryPoint entryPoint = factory.createEntryPoint();
        entryPoint.setPhysicalNamespace(DEFAULT_NAMESPACE);
        entryPoint.setName(name);
        entryPoint.setRuleNames(rules);
        entryPoint.setIncludedEntryPointNames(includes);
        entryPoint.setServerSideOnly(sso);
        return entryPoint;
    }

    public static GenericTypeBound bound(String generic, String bound) {
        GenericTypeBound genericTypeBound = factory.createGenericTypeBound();
        genericTypeBound.setGeneric(generic);
        genericTypeBound.setBound(bound);
        return genericTypeBound;
    }

    public static FunctionSignature functionSignature(String name, String returnType, List<String> parameterTypes) {
        return functionSignature(name, returnType, parameterTypes, List.of());
    }

    public static FunctionSignature functionSignature(String name,
                                                      String returnType,
                                                      List<String> parameterTypes,
                                                      List<GenericTypeBound> bounds) {
        FunctionSignature functionSignature = factory.createFunctionSignature();
        functionSignature.setName(name);
        functionSignature.setReturnType(returnType);
        functionSignature.setParameterTypes(parameterTypes);
        functionSignature.setGenericTypeBounds(bounds);
        return functionSignature;
    }

    public static FunctionParameter parameter(String name, String type) {
        FunctionParameter parameter = factory.createFunctionParameter();
        parameter.setName(name);
        parameter.setType(type);
        return parameter;
    }
    public static Function function(String name, String body) {
        return function(name, List.of(), body);
    }
    public static Function function(String name, List<FunctionParameter> parameters, String body) {
        return function(name, Type.ANY.getName(), parameters, body);
    }
    public static Function function(String name, String returnType, List<FunctionParameter> parameters, String body) {
        return function(name, returnType, parameters, body, List.of());
    }
    public static Function function(String name, String returnType, List<FunctionParameter> parameters, String body,
                                    List<GenericTypeBound> bounds) {
        return function(name, returnType, parameters, body, bounds, null);
    }
    public static Function function(String name, String returnType, List<FunctionParameter> parameters, String body,
                                    List<GenericTypeBound> bounds, FunctionDocumentation documentation) {
        Function function = factory.createFunction();
        function.setName(name);
        function.setReturnType(returnType);
        function.setParameters(parameters);
        function.setGenericTypeBounds(bounds);
        Expression functionBody = factory.createExpression();
        functionBody.setExpressionString(body);
        function.setBody(functionBody);
        function.setDocumentation(documentation);
        return function;
    }
}
