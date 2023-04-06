/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
package kraken.model.dsl;


import kraken.model.context.Cardinality;
import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.model.context.ContextNavigation;
import kraken.model.dsl.model.DSLCardinality;
import kraken.model.dsl.model.DSLContext;
import kraken.model.dsl.model.DSLContextChild;
import kraken.model.dsl.model.DSLContextField;
import kraken.model.dsl.model.DSLContexts;
import kraken.model.dsl.model.DSLModel;
import kraken.model.factory.RulesModelFactory;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mulevicius
 */
class KrakenDSLModelContextConverter {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    private KrakenDSLModelContextConverter() {
    }

    static List<ContextDefinition> convertContexts(DSLModel dsl) {
        List<ContextDefinition> contexts = convertContextBlocks(dsl.getNamespace(), dsl.getContextBlocks());
        contexts.addAll(convertContexts(dsl.getNamespace(), dsl.getContexts()));

        return Collections.unmodifiableList(contexts);
    }

    private static List<ContextDefinition> convertContexts(String namespace, Collection<DSLContext> contexts) {
        return contexts.stream()
                .map(context -> convert(namespace, context))
                .collect(Collectors.toList());
    }

    private static List<ContextDefinition> convertContextBlocks(String namespace, Collection<DSLContexts> contextBlocks) {
        return contextBlocks.stream()
                .flatMap(contextBlock -> convertContextBlock(namespace, contextBlock).stream())
                .collect(Collectors.toList());
    }

    private static Collection<ContextDefinition> convertContextBlock(String namespace, DSLContexts contexts) {
        Collection<ContextDefinition> contextDefinitions = convertContexts(namespace, contexts.getContexts());
        contextDefinitions.addAll(convertContextBlocks(namespace, contexts.getContextBlocks()));

        return Collections.unmodifiableCollection(contextDefinitions);
    }

    private static ContextDefinition convert(String namespace, DSLContext context) {
        ContextDefinition contextDefinition = factory.createContextDefinition();
        contextDefinition.setRoot(context.isRoot());
        contextDefinition.setSystem(context.isSystem());
        contextDefinition.setPhysicalNamespace(namespace);
        contextDefinition.setName(context.getName());
        contextDefinition.setStrict(context.isStrict());
        contextDefinition.setParentDefinitions(new ArrayList<>(context.getInheritedContexts()));

        contextDefinition.setContextFields(context.getFields()
                .stream()
                .collect(Collectors.toMap(DSLContextField::getName, field -> convert(field))));

        contextDefinition.setChildren(context.getChildren()
                .stream()
                .collect(Collectors.toMap(DSLContextChild::getName, child -> convert(child))));

        return contextDefinition;
    }

    private static ContextField convert(DSLContextField dslContextField) {
        ContextField contextField = factory.createContextField();
        contextField.setName(dslContextField.getName());
        contextField.setFieldPath(fieldPath(dslContextField));
        contextField.setCardinality(convert(dslContextField.getCardinality()));
        contextField.setFieldType(dslContextField.getType());
        contextField.setForbidReference(dslContextField.getForbidReference());
        contextField.setForbidTarget(dslContextField.getForbidTarget());
        return contextField;
    }

    private static ContextNavigation convert(DSLContextChild child) {
        ContextNavigation contextNavigation = factory.createContextNavigation();
        contextNavigation.setTargetName(child.getName());
        contextNavigation.setCardinality(convert(child.getCardinality()));
        contextNavigation.setNavigationExpression(navigationExpression(child));
        contextNavigation.setForbidReference(child.getForbidReference());
        return contextNavigation;
    }

    private static String navigationExpression(DSLContextChild child) {
        return child.getNavigationExpression() != null
                ? child.getNavigationExpression().getExpression()
                : Introspector.decapitalize(child.getName());
    }

    private static String fieldPath(DSLContextField dslContextField) {
        return dslContextField.getPath() != null
                ? dslContextField.getPath()
                : Introspector.decapitalize(dslContextField.getName());
    }

    private static Cardinality convert(DSLCardinality dslCardinality) {
        return Cardinality.valueOf(dslCardinality.name());
    }
}
