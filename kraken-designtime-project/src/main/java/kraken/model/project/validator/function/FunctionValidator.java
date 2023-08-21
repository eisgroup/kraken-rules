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
package kraken.model.project.validator.function;

import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_GENERIC_BOUND_DUPLICATE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_GENERIC_BOUND_IS_ITSELF_GENERIC;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_NAME_DUPLICATE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_NAME_DUPLICATE_WITH_SIGNATURE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_NATIVE_DUPLICATE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_PARAMETER_DUPLICATE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_PARAMETER_TYPE_UNION_GENERIC_MIX;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_PARAMETER_TYPE_UNKNOWN;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_RETURN_TYPE_UNION_GENERIC_MIX;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_RETURN_TYPE_UNKNOWN;

import java.util.Set;
import java.util.stream.Collectors;

import kraken.el.KrakenKel;
import kraken.el.functionregistry.FunctionHeader;
import kraken.el.functionregistry.FunctionRegistry;
import kraken.el.scope.symbol.FunctionSymbol;
import kraken.el.scope.type.Type;
import kraken.model.Function;
import kraken.model.FunctionParameter;
import kraken.model.FunctionSignature;
import kraken.model.GenericTypeBound;
import kraken.model.project.KrakenProject;
import kraken.model.project.scope.ScopeBuilder;
import kraken.model.project.scope.ScopeBuilderProvider;
import kraken.model.project.validator.Duplicates;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.model.project.validator.ValidationSession;
import kraken.model.project.validator.namespaced.NamespacedValidator;

/**
 * Validates {@link Function} defined in KrakenProject
 *
 * @author mulevicius
 */
public final class FunctionValidator {

    private final KrakenProject krakenProject;
    private final ScopeBuilder scopeBuilder;

    private final Set<String> functionSignatureNames;
    private final Set<String> nativeFunctionNames;

    public FunctionValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
        this.scopeBuilder = ScopeBuilderProvider.forProject(krakenProject);

        this.functionSignatureNames = krakenProject.getFunctionSignatures().stream()
            .map(FunctionSignature::getName)
            .collect(Collectors.toSet());

        this.nativeFunctionNames = FunctionRegistry.getNativeFunctions(KrakenKel.EXPRESSION_TARGET).keySet().stream()
            .map(FunctionHeader::getName)
            .collect(Collectors.toSet());
    }

    public void validate(ValidationSession session) {
        Duplicates.findAndDo(
            krakenProject.getFunctions(),
            duplicates -> session.add(ValidationMessageBuilder.create(FUNCTION_NAME_DUPLICATE, duplicates.get(0))
                .parameters(duplicates.get(0).getName())
                .build())
        );

        for (Function function : krakenProject.getFunctions()) {
            validateFunction(function, session);
        }
    }

    private void validateFunction(Function function, ValidationSession session) {
        session.addAll(NamespacedValidator.validate(function));

        if(functionSignatureNames.contains(function.getName())) {
            var m = ValidationMessageBuilder.create(FUNCTION_NAME_DUPLICATE_WITH_SIGNATURE, function)
                .parameters(function.getName())
                .build();
            session.add(m);
        }

        if(nativeFunctionNames.contains(function.getName())) {
            var m = ValidationMessageBuilder.create(FUNCTION_NATIVE_DUPLICATE, function)
                .parameters(function.getName())
                .build();
            session.add(m);
        }

        FunctionSymbol symbol = scopeBuilder.buildFunctionSymbol(function);

        validateGenericTypeBounds(function, symbol, session);
        validateReturnType(function, symbol, session);
        validateParameters(function, symbol, session);
    }

    private void validateGenericTypeBounds(Function function, FunctionSymbol symbol, ValidationSession session) {
        Duplicates.findAndDo(
            function.getGenericTypeBounds(),
            GenericTypeBound::getGeneric,
            duplicates -> {
                var m = ValidationMessageBuilder.create(FUNCTION_GENERIC_BOUND_DUPLICATE, function)
                    .parameters(duplicates.get(0).getGeneric())
                    .build();
                session.add(m);
            }
        );

        for(GenericTypeBound genericTypeBound : function.getGenericTypeBounds()) {
            Type boundType = scopeBuilder.resolveTypeOf(genericTypeBound.getBound());
            if(boundType.isGeneric()) {
                var m = ValidationMessageBuilder.create(FUNCTION_GENERIC_BOUND_IS_ITSELF_GENERIC, function)
                    .parameters(genericTypeBound.getBound(), genericTypeBound.getGeneric())
                    .build();
                session.add(m);
            }
        }
    }

    private void validateReturnType(Function function, FunctionSymbol symbol, ValidationSession session) {
        Type type = symbol.getType();
        String typeToken = function.getReturnType();
        if (!type.isKnown()) {
            var m = ValidationMessageBuilder.create(FUNCTION_RETURN_TYPE_UNKNOWN, function)
                .parameters(typeToken)
                .build();
            session.add(m);
        }
        if(type.isUnion() && type.isGeneric()) {
            var m = ValidationMessageBuilder.create(FUNCTION_RETURN_TYPE_UNION_GENERIC_MIX, function)
                .parameters(typeToken)
                .build();
            session.add(m);
        }
    }

    private void validateParameters(Function function, FunctionSymbol symbol, ValidationSession session) {
        Duplicates.findAndDo(
            function.getParameters(),
            FunctionParameter::getName,
            duplicates -> {
                var m = ValidationMessageBuilder.create(FUNCTION_PARAMETER_DUPLICATE, function)
                    .parameters(duplicates.get(0).getName())
                    .build();
                session.add(m);
            }
        );

        for(var parameter : symbol.getParameters()) {
            Type type = parameter.getType();
            String typeToken = function.getParameters().get(parameter.getParameterIndex()).getType();
            if(!type.isKnown()) {
                var m = ValidationMessageBuilder.create(FUNCTION_PARAMETER_TYPE_UNKNOWN, function)
                    .parameters(typeToken)
                    .build();
                session.add(m);
            }
            if(type.isUnion() && type.isGeneric()) {
                var m = ValidationMessageBuilder.create(FUNCTION_PARAMETER_TYPE_UNION_GENERIC_MIX, function)
                    .parameters(typeToken)
                    .build();
                session.add(m);
            }
        }
    }
}
