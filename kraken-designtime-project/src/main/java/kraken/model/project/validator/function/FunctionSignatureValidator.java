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

import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_SIGNATURE_DUPLICATE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_SIGNATURE_GENERIC_BOUND_DUPLICATE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_SIGNATURE_GENERIC_BOUND_IS_ITSELF_GENERIC;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_SIGNATURE_PARAMETER_TYPE_UNKNOWN;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_SIGNATURE_RETURN_TYPE_UNION_GENERIC_MIX;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_SIGNATURE_RETURN_TYPE_UNKNOWN;

import java.util.stream.Collectors;

import kraken.el.scope.symbol.FunctionSymbol;
import kraken.el.scope.type.Type;
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
 * Validates {@link FunctionSignature} defined in KrakenProject
 *
 * @author mulevicius
 */
public final class FunctionSignatureValidator {

    private final KrakenProject krakenProject;
    private final ScopeBuilder scopeBuilder;

    public FunctionSignatureValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
        this.scopeBuilder = ScopeBuilderProvider.forProject(krakenProject);
    }

    public void validate(ValidationSession session) {
        Duplicates.findAndDo(
            krakenProject.getFunctionSignatures(),
            FunctionSignature::toHeader,
            duplicates -> {
                var m = ValidationMessageBuilder.create(FUNCTION_SIGNATURE_DUPLICATE, duplicates.get(0))
                    .parameters(duplicates.stream().map(FunctionSignature::format).collect(Collectors.joining(", ")))
                    .build();
                session.add(m);
            }
        );

        for (FunctionSignature functionSignature : krakenProject.getFunctionSignatures()) {
            validateFunctionSignature(functionSignature, session);
        }
    }

    private void validateFunctionSignature(FunctionSignature signature, ValidationSession session) {
        session.addAll(NamespacedValidator.validate(signature));

        FunctionSymbol symbol = scopeBuilder.buildFunctionSymbol(signature);

        validateGenericTypeBounds(signature, symbol, session);
        validateReturnType(signature, symbol, session);
        validateParameters(signature, symbol, session);
    }

    private void validateGenericTypeBounds(FunctionSignature signature, FunctionSymbol symbol, ValidationSession session) {
        Duplicates.findAndDo(
            signature.getGenericTypeBounds(),
            GenericTypeBound::getGeneric,
            duplicates -> {
                var m = ValidationMessageBuilder.create(FUNCTION_SIGNATURE_GENERIC_BOUND_DUPLICATE, signature)
                    .parameters(duplicates.get(0).getGeneric())
                    .build();
                session.add(m);
            }
        );

        for(GenericTypeBound genericTypeBound : signature.getGenericTypeBounds()) {
            Type boundType = scopeBuilder.resolveTypeOf(genericTypeBound.getBound());
            if(boundType.isGeneric()) {
                var m = ValidationMessageBuilder.create(FUNCTION_SIGNATURE_GENERIC_BOUND_IS_ITSELF_GENERIC, signature)
                    .parameters(genericTypeBound.getGeneric(), genericTypeBound.getBound())
                    .build();
                session.add(m);
            }
        }
    }

    private void validateReturnType(FunctionSignature signature, FunctionSymbol symbol, ValidationSession session) {
        Type type = symbol.getType();
        String typeToken = signature.getReturnType();
        if (!type.isKnown()) {
            var m = ValidationMessageBuilder.create(FUNCTION_SIGNATURE_RETURN_TYPE_UNKNOWN, signature)
                .parameters(typeToken)
                .build();
            session.add(m);
        }
        if(type.isUnion() && type.isGeneric()) {
            var m = ValidationMessageBuilder.create(FUNCTION_SIGNATURE_RETURN_TYPE_UNION_GENERIC_MIX, signature)
                .parameters(typeToken)
                .build();
            session.add(m);
        }
    }

    private void validateParameters(FunctionSignature signature, FunctionSymbol symbol, ValidationSession session) {
        for(var parameter : symbol.getParameters()) {
            Type type = parameter.getType();
            String typeToken = signature.getParameterTypes().get(parameter.getParameterIndex());
            if(!type.isKnown()) {
                var m = ValidationMessageBuilder.create(FUNCTION_SIGNATURE_PARAMETER_TYPE_UNKNOWN, signature)
                    .parameters(typeToken)
                    .build();
                session.add(m);
            }
            if(type.isUnion() && type.isGeneric()) {
                var m = ValidationMessageBuilder.create(FUNCTION_SIGNATURE_PARAMETER_TYPE_UNKNOWN, signature)
                    .parameters(typeToken)
                    .build();
                session.add(m);
            }
        }
    }
}