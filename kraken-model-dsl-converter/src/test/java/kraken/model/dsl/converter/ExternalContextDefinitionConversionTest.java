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

import kraken.model.context.Cardinality;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.context.external.ExternalContextDefinitionAttribute;
import kraken.model.context.external.ExternalContextDefinitionAttributeType;
import kraken.model.factory.RulesModelFactory;
import kraken.model.resource.builder.ResourceBuilder;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;

/**
 * Unit tests to test conversion of {@code ExternalContextDefinition}'s to string.
 *
 * @author Tomas Dapkunas
 * @since 1.3.0
 */
public class ExternalContextDefinitionConversionTest {

    private static final String SEPARATOR = System.lineSeparator();
    private static final RulesModelFactory RULES_MODEL_FACTORY = RulesModelFactory.getInstance();

    private final DSLModelConverter converter = new DSLModelConverter();

    @Test
    public void shouldConvertSingleExternalContexts() {
        ExternalContextDefinition externalContext = createExternalContext(
                "ExternalContextDefinition",
                "PhysicalNamespace",
                createAttribute(
                        "stringAttribute",
                        createAttributeType("STRING", true, Cardinality.MULTIPLE)
                ),
                createAttribute(
                        "keys",
                        createAttributeType("STRING", true, Cardinality.MULTIPLE)
                ),
                createAttribute(
                        "values",
                        createAttributeType("STRING", true, Cardinality.MULTIPLE)
                )

        );

        String convertedString = convert(externalContext);

        assertEquals(
                "ExternalEntity ExternalContextDefinition {" + SEPARATOR +
                        "    STRING* keys" + SEPARATOR +
                        "    STRING* values" + SEPARATOR +
                        "    STRING* stringAttribute" + SEPARATOR +
                        "}" + SEPARATOR + SEPARATOR, convertedString);
    }

    @Test
    public void shouldConvertMultipleExternalContexts() {
        ExternalContextDefinition eCtx = createExternalContext("ExternalContextDefinition", "PhysicalNamespace",
                createAttribute("otherContext",
                        createAttributeType("OtherContext", false, Cardinality.SINGLE)));

        ExternalContextDefinition othCtx = createExternalContext("OtherContext", "PhysicalNamespace",
                createAttribute("stringAttribute",
                        createAttributeType("STRING", true, Cardinality.SINGLE)));

        String convertedString = convert(eCtx, othCtx);

        assertEquals(
                        "ExternalEntity ExternalContextDefinition {" + SEPARATOR +
                        "    OtherContext otherContext" + SEPARATOR +
                        "}" + SEPARATOR + SEPARATOR +
                        "ExternalEntity OtherContext {" + SEPARATOR +
                        "    STRING stringAttribute" + SEPARATOR +
                        "}" + SEPARATOR + SEPARATOR, convertedString);
    }

    private String convert(ExternalContextDefinition... externalContextDefinition) {
        return converter.convert(ResourceBuilder.getInstance()
                .addExternalContextDefinitions(Arrays.asList(externalContextDefinition))
                .build());
    }

    private ExternalContextDefinition createExternalContext(String name, String namespace, ExternalContextDefinitionAttribute... attributes) {
        ExternalContextDefinition eCtx = RULES_MODEL_FACTORY.createExternalContextDefinition();
        eCtx.setName(name);
        eCtx.setPhysicalNamespace(namespace);

        Arrays.stream(attributes)
                .forEach(attribute -> eCtx.getAttributes().put(attribute.getName(), attribute));

        return eCtx;
    }

    private ExternalContextDefinitionAttribute createAttribute(String attributeName, ExternalContextDefinitionAttributeType type) {
        ExternalContextDefinitionAttribute attribute = RULES_MODEL_FACTORY.createExternalContextDefinitionAttribute();
        attribute.setName(attributeName);
        attribute.setType(type);

        return attribute;
    }

    private ExternalContextDefinitionAttributeType createAttributeType(String typeName, Boolean isPrimitive, Cardinality cardinality) {
        ExternalContextDefinitionAttributeType type = RULES_MODEL_FACTORY.createExternalContextDefinitionAttributeType();
        type.setType(typeName);
        type.setPrimitive(isPrimitive);
        type.setCardinality(cardinality);

        return type;
    }

}
