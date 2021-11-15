/*
 *  Copyright 2020 EIS Ltd and/or one of its affiliates.
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
package kraken.model.dsl.visitor;

import kraken.model.context.PrimitiveFieldDataType;
import kraken.model.dsl.KrakenDSL;
import kraken.model.dsl.KrakenDSLBaseVisitor;
import kraken.model.dsl.model.DSLCardinality;
import kraken.model.dsl.model.DSLExternalContextDefinition;
import kraken.model.dsl.model.DSLExternalContextDefinitionField;
import kraken.model.dsl.model.DSLExternalContextDefinitionFieldType;

import java.util.stream.Collectors;

/**
 * A visitor for visiting External Context Definition parser rule context.
 *
 * @author Tomas Dapkunas
 * @since 1.3.0
 */
public class DSLExternalContextDefinitionVisitor extends KrakenDSLBaseVisitor<DSLExternalContextDefinition> {

    @Override
    public DSLExternalContextDefinition visitExternalContextDefinition(KrakenDSL.ExternalContextDefinitionContext ctx) {
        String contextName = ctx.contextName().getText();

        return new DSLExternalContextDefinition(contextName, ctx.externalContextField().stream()
                .map(this::toContextField)
                .collect(Collectors.toList()));
    }

    private DSLExternalContextDefinitionField toContextField(KrakenDSL.ExternalContextFieldContext fieldContext) {
       return new DSLExternalContextDefinitionField(fieldContext.fieldName.getText(),
               toFieldType(fieldContext));
    }

    private DSLExternalContextDefinitionFieldType toFieldType(KrakenDSL.ExternalContextFieldContext fieldContext) {
        String type = fieldContext.fieldType().getText();
        DSLCardinality cardinality = fieldContext.OP_MULT() != null ? DSLCardinality.MULTIPLE : DSLCardinality.SINGLE;

        return PrimitiveFieldDataType.isPrimitiveType(type.toUpperCase()) ?
                new DSLExternalContextDefinitionFieldType(type.toUpperCase(), true, cardinality) :
                new DSLExternalContextDefinitionFieldType(type, false, cardinality);
    }

}
