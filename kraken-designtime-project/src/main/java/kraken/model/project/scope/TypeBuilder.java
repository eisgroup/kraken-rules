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
package kraken.model.project.scope;

import kraken.el.scope.SymbolTable;
import kraken.el.scope.symbol.VariableSymbol;
import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.Type;
import kraken.el.scope.type.TypeRef;
import kraken.el.scope.type.TypeRefResolver;
import kraken.model.context.*;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.project.KrakenProject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;

/**
 * @author mulevicius
 */
final class TypeBuilder {

    private TypeBuilder() {
    }

    /**
     * Builds a {@code Type} for given {@code ContextDefinition}.
     *
     * @param contextDefinition Context definition to build type for.
     * @param krakenProject     Kraken project.
     * @param typeRefResolver   Type reference function.
     * @return {@code Type} for context definition.
     */
    static Type buildType(ContextDefinition contextDefinition,
                          KrakenProject krakenProject,
                          TypeRefResolver typeRefResolver) {
        if (!contextDefinition.isStrict()) {
            return Type.ANY;
        }
        Map<String, VariableSymbol> properties = new HashMap<>();
        for (var field : krakenProject.getContextProjection(contextDefinition.getName()).getContextFields().values()) {
            if(BooleanUtils.isTrue(field.getForbidReference())) {
                continue;
            }
            Type type = toTypeFromDefinitionType(field.getFieldType(), typeRefResolver);
            Type symbolType = Cardinality.MULTIPLE == field.getCardinality()
                    ? ArrayType.of(type)
                    : type;
            VariableSymbol variableSymbol = new VariableSymbol(field.getName(), symbolType);
            properties.put(field.getName(), variableSymbol);
        }
        return new Type(
                contextDefinition.getName(),
                new SymbolTable(List.of(), properties),
                contextDefinition.getParentDefinitions()
                        .stream()
                        .map(type -> new TypeRef(type, typeRefResolver))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Builds a {@code Type} for given {@code ExternalContextDefinition}.
     *
     * @param externalContextDefinition External context definition to build type for.
     * @param typeRefResolver           Type reference function.
     * @return {@code Type} for external context definition.
     */
    static Type buildType(ExternalContextDefinition externalContextDefinition, TypeRefResolver typeRefResolver) {

        Map<String, VariableSymbol> properties = new HashMap<>();

        externalContextDefinition.getAttributes().values()
                .forEach(externalContextDefinitionAttribute -> {
                    Type type = toTypeFromDefinitionType(externalContextDefinitionAttribute.getType().getType(), typeRefResolver);
                    Type symbolType = Cardinality.MULTIPLE == externalContextDefinitionAttribute.getType().getCardinality()
                            ? ArrayType.of(type)
                            : type;

                    VariableSymbol variableSymbol = new VariableSymbol(externalContextDefinitionAttribute.getName(), symbolType);
                    properties.put(externalContextDefinitionAttribute.getName(), variableSymbol);
                });

        return new Type(
                externalContextDefinition.getName(),
                new SymbolTable(List.of(), properties)
        );
    }

    private static Type toPrimitiveType(PrimitiveFieldDataType primitiveFieldDataType) {
        switch (primitiveFieldDataType) {
            case DECIMAL:
            case INTEGER:
                return Type.NUMBER;
            case STRING:
                return Type.STRING;
            case BOOLEAN:
                return Type.BOOLEAN;
            case MONEY:
                return Type.MONEY;
            case DATE:
                return Type.DATE;
            case DATETIME:
                return Type.DATETIME;
            default:
                throw new IllegalStateException("Unknown primitive type encountered: " + primitiveFieldDataType);
        }
    }

    private static Type toTypeFromDefinitionType(String type, TypeRefResolver typeRefResolver) {
        if (PrimitiveFieldDataType.isPrimitiveType(type)) {
            return toPrimitiveType(PrimitiveFieldDataType.valueOf(type));
        } else if (SystemDataTypes.isSystemDataType(type)) {
            return toSystemType(SystemDataTypes.valueOf(type));
        } else {
            return new TypeRef(type, typeRefResolver);
        }
    }

    private static Type toSystemType(SystemDataTypes systemDataTypes) {
        if (SystemDataTypes.UNKNOWN == systemDataTypes) {
            return Type.UNKNOWN;
        }

        throw new IllegalStateException("Unknown system data type encountered: " + systemDataTypes);
    }

}
