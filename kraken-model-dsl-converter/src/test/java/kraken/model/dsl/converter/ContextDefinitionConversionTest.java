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
package kraken.model.dsl.converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import kraken.model.context.Cardinality;
import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.model.context.ContextNavigation;
import kraken.model.factory.RulesModelFactory;
import kraken.model.resource.Resource;
import kraken.model.resource.builder.ResourceBuilder;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ContextDefinitionConversionTest {

    private final static RulesModelFactory RULES_MODEL_FACTORY = RulesModelFactory.getInstance();

    private final DSLModelConverter converter = new DSLModelConverter();

    @Test
    public void shouldConvertRootContext() {
        final String zone = convert(rootContext("Policy"));
        assertEquals(
                        "Root Context Policy {" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator(),
                zone
        );
    }

    @Test
    public void shouldConvertSeveralContextDefinitions() {
        final String zone = convert(context("Zone"));
        assertEquals(
                        "Context Zone {" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator(),
                zone
        );
    }

    private String convert(ContextDefinition contextDefinition){
        Resource resource = ResourceBuilder.getInstance()
                .addContextDefinition(contextDefinition)
                .build();
        return converter.convert(resource);
    }

    @Test
    public void shouldConvertContext() {
        ContextDefinition contextDefinition = RULES_MODEL_FACTORY.createContextDefinition();
        contextDefinition.setName("ContextDefinitionName");
        contextDefinition.setParentDefinitions(Collections.singleton("ContextDefinitionNameParent"));
        contextDefinition.setStrict(true);
        contextDefinition.setPhysicalNamespace("PhysicalName");
        ContextField contextField1 = createContextField("contextField1", "INTEGER", "contextField1", Cardinality.SINGLE);
        ContextField contextField2 = createContextField("contextField2", "String", "contextField1", Cardinality.MULTIPLE);
        ContextField contextField3 = createContextField(true, "contextField3", "DECIMAL", "contextField3", Cardinality.MULTIPLE);
        ContextField contextField4 = createContextField("contextField4", "ReplacementCostEndorsement", "contextField4", Cardinality.MULTIPLE);
        contextDefinition.setContextFields(toLinkedMap(ContextField::getName, contextField1, contextField2, contextField3, contextField4));

        ContextNavigation contextNavigation1 = createContextNavigation("contextNavigation1", "expression", Cardinality.SINGLE);
        ContextNavigation contextNavigation2 = createContextNavigation("contextNavigation2", "expression", Cardinality.MULTIPLE);
        contextDefinition.setChildren(toLinkedMap(ContextNavigation::getTargetName, contextNavigation1, contextNavigation2));

        String convertedContext = convert(contextDefinition);
        assertEquals(
                        "Context ContextDefinitionName Is ContextDefinitionNameParent {" +
                        System.lineSeparator() +
                        "    Integer contextField1" +
                        System.lineSeparator() +
                        "    String* contextField2 : contextField1" +
                        System.lineSeparator() +
                        "    External Decimal* contextField3" +
                        System.lineSeparator() +
                        "    ReplacementCostEndorsement* contextField4" +
                        System.lineSeparator() +
                        "    Child contextNavigation1 : expression" +
                        System.lineSeparator() +
                        "    Child* contextNavigation2 : expression" +
                        System.lineSeparator() +
                        "}" +
                        System.lineSeparator(),
                        convertedContext
        );
    }

    @Test
    public void shouldConvertContextNotStrictWithParent() {
        ContextDefinition contextDefinition = RULES_MODEL_FACTORY.createContextDefinition();
        contextDefinition.setName("ContextDefinitionNonStrictWithParent");
        contextDefinition.setParentDefinitions(Collections.singleton("Parent"));
        contextDefinition.setStrict(false);
        String convertedContext = convert(contextDefinition);
        assertEquals(
                        "@NotStrict" +
                        System.lineSeparator() +
                        "Context ContextDefinitionNonStrictWithParent Is Parent {" +
                        System.lineSeparator() +
                        "}" +
                        System.lineSeparator(),
                convertedContext
        );
    }

    @Test
    public void shouldConvertContextWithChildren() {
        ContextDefinition contextDefinition = RULES_MODEL_FACTORY.createContextDefinition();
        contextDefinition.setName("ContextDefinitionWithChild");
        contextDefinition.setStrict(false);
        contextDefinition.setPhysicalNamespace("PhysicalNameForContext");
        ContextNavigation contextNavigation1 = createContextNavigation("child1", "targetPath", Cardinality.SINGLE);
        ContextNavigation contextNavigation2 = createContextNavigation("child2", "child2", Cardinality.MULTIPLE);
        ContextNavigation contextNavigation3 = createContextNavigation("Child3", "child3", Cardinality.MULTIPLE);
        contextDefinition.setChildren(toLinkedMap(ContextNavigation::getTargetName, contextNavigation1, contextNavigation2, contextNavigation3));
        String convertedContext = convert(contextDefinition);
        assertEquals(
                        "@NotStrict" +
                        System.lineSeparator() +
                        "Context ContextDefinitionWithChild {" +
                        System.lineSeparator() +
                        "    Child child1 : targetPath" +
                        System.lineSeparator() +
                        "    Child* child2" +
                        System.lineSeparator() +
                        "    Child* Child3" +
                        System.lineSeparator() +
                        "}" +
                        System.lineSeparator(),
                        convertedContext
        );
    }

    @Test
    public void shouldConvertContextWithField() {
        ContextDefinition contextDefinition = RULES_MODEL_FACTORY.createContextDefinition();
        contextDefinition.setName("Policy");
        contextDefinition.setStrict(true);
        ContextNavigation contextNavigation1 = createContextNavigation("Coverage", "all.my.coverages", Cardinality.MULTIPLE);
        ContextNavigation contextNavigation2 = createContextNavigation("RiskItem", "riskItem", Cardinality.SINGLE);
        contextDefinition.setChildren(toLinkedMap(ContextNavigation::getTargetName, contextNavigation1, contextNavigation2));
        String convertedContext = convert(contextDefinition);

        assertEquals(
                        "Context Policy {" +
                        System.lineSeparator() +
                        "    Child* Coverage : all.my.coverages" +
                        System.lineSeparator() +
                        "    Child RiskItem" +
                        System.lineSeparator() +
                        "}" +
                        System.lineSeparator(),
                convertedContext
        );
    }

    @Test
    public void shouldConvertContextWithFields() {
        ContextDefinition contextDefinition = RULES_MODEL_FACTORY.createContextDefinition();
        contextDefinition.setName("ContextWithFields");
        contextDefinition.setParentDefinitions(List.of("Parent1", "Parent1"));
        contextDefinition.setStrict(false);
        ContextField contextField1 = createContextField("field1", "Integer", "field1", Cardinality.SINGLE);
        ContextField contextField2 = createContextField("field2", "String", "notMatchingPath", Cardinality.MULTIPLE);
        ContextField contextField3 = createContextField("Field3", "Boolean", "field3", Cardinality.SINGLE);
        contextDefinition.setContextFields(toLinkedMap(ContextField::getName, contextField1, contextField2, contextField3));

        String convertedContext = convert(contextDefinition);
        assertEquals(
                        "@NotStrict" +
                        System.lineSeparator() +
                        "Context ContextWithFields Is Parent1, Parent1 {" +
                        System.lineSeparator() +
                        "    Integer field1" +
                        System.lineSeparator() +
                        "    String* field2 : notMatchingPath" +
                        System.lineSeparator() +
                        "    Boolean Field3 : field3" +
                        System.lineSeparator() +
                        "}" +
                        System.lineSeparator(),
                convertedContext
        );
    }

    private ContextDefinition context(String name) {
        ContextDefinition ctx = RULES_MODEL_FACTORY.createContextDefinition();
        ctx.setName(name);
        ctx.setStrict(true);
        ctx.setPhysicalNamespace("whatever");
        return ctx;
    }

    private ContextDefinition rootContext(String name) {
        ContextDefinition ctx = context(name);
        ctx.setRoot(true);
        return ctx;
    }

    private ContextField createContextField(String name, String fieldType, String fieldPath, Cardinality cardinality) {
        return createContextField(false, name, fieldType, fieldPath, cardinality);
    }

    private ContextField createContextField(boolean external, String name, String fieldType, String fieldPath, Cardinality cardinality) {
        ContextField contextField = RULES_MODEL_FACTORY.createContextField();
        contextField.setName(name);
        contextField.setFieldType(fieldType);
        contextField.setFieldPath(fieldPath);
        contextField.setCardinality(cardinality);
        contextField.setExternal(external);
        return contextField;
    }

    private ContextNavigation createContextNavigation(String targetName, String expression, Cardinality cardinality) {
        ContextNavigation contextNavigation = RULES_MODEL_FACTORY.createContextNavigation();
        contextNavigation.setTargetName(targetName);
        contextNavigation.setNavigationExpression(expression);
        contextNavigation.setCardinality(cardinality);
        return contextNavigation;
    }

    private <K, V> Map<K, V> toLinkedMap(Function<V, K> keyMapper, V... values) {
        return Arrays.stream(values)
                .collect(Collectors.toMap(keyMapper, Function.identity(), (v1,v2) -> { throw new IllegalStateException(); }, LinkedHashMap::new));
    }
}
