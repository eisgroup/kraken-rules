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
import kraken.model.context.external.ExternalContextDefinitionAttribute;
import kraken.model.context.external.ExternalContextDefinitionAttributeType;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.dsl.model.DSLCardinality;
import kraken.model.dsl.model.DSLExternalContextDefinition;
import kraken.model.dsl.model.DSLExternalContextDefinitionField;
import kraken.model.dsl.model.DSLExternalContextDefinitionFieldType;
import kraken.model.dsl.model.DSLModel;
import kraken.model.factory.RulesModelFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Converter for extracting all {@code DSLExternalContextDefinition}'s from DSL model and converting
 * to {@code ExternalContextDefinition}'s.
 *
 * @author Tomas Dapkunas
 * @since 1.3.0
 */
class KrakenDSLModelExternalContextDefinitionConverter {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    private KrakenDSLModelExternalContextDefinitionConverter() {
    }

    static List<ExternalContextDefinition> convertExternalContextDefinitions(DSLModel dsl) {
        String namespace = dsl.getNamespace();

        return dsl.getExternalContextDefinitions().stream()
                .map(context -> convert(namespace, context))
                .collect(Collectors.toList());
    }

    private static ExternalContextDefinition convert(String namespace, DSLExternalContextDefinition context) {
        ExternalContextDefinition contextDefinition = factory.createExternalContextDefinition();
        contextDefinition.setName(context.getName());
        contextDefinition.setPhysicalNamespace(namespace);
        contextDefinition.setAttributes(context.getFields()
                .stream()
                .collect(Collectors.toMap(DSLExternalContextDefinitionField::getName, KrakenDSLModelExternalContextDefinitionConverter::convert)));

        return contextDefinition;
    }

    private static ExternalContextDefinitionAttribute convert(DSLExternalContextDefinitionField dslContextField) {
        ExternalContextDefinitionAttribute contextAttribute = factory.createExternalContextDefinitionAttribute();
        contextAttribute.setName(dslContextField.getName());
        contextAttribute.setType(convert(dslContextField.getType()));

        return contextAttribute;
    }

    private static ExternalContextDefinitionAttributeType convert(DSLExternalContextDefinitionFieldType dslContextFieldType) {
        ExternalContextDefinitionAttributeType contextAttributeType = factory.createExternalContextDefinitionAttributeType();
        contextAttributeType.setType(dslContextFieldType.getType());
        contextAttributeType.setCardinality(convert(dslContextFieldType.getCardinality()));
        contextAttributeType.setPrimitive(dslContextFieldType.isPrimitive());

        return contextAttributeType;
    }

    private static Cardinality convert(DSLCardinality dslCardinality) {
        return Cardinality.valueOf(dslCardinality.name());
    }

}
