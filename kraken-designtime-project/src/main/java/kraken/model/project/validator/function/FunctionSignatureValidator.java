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

import java.util.stream.Collectors;

import kraken.el.scope.symbol.FunctionSymbol;
import kraken.el.scope.type.Type;
import kraken.model.Function;
import kraken.model.FunctionSignature;
import kraken.model.GenericTypeBound;
import kraken.model.project.KrakenProject;
import kraken.model.project.scope.ScopeBuilder;
import kraken.model.project.scope.ScopeBuilderProvider;
import kraken.model.project.validator.Duplicates;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
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
                session.add(new ValidationMessage(
                    duplicates.get(0),
                    String.format(
                        "is not valid because there are more than one function signature defined: %s",
                        duplicates.stream().map(FunctionSignature::format).collect(Collectors.joining(", "))
                    ),
                    Severity.ERROR
                ));
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
                session.add(new ValidationMessage(
                    signature,
                    String.format(
                        "is not valid because there are more than one generic bound for the same generic type "
                            + "name '%s'",
                        duplicates.get(0).getGeneric()
                    ),
                    Severity.ERROR
                ));
            }
        );

        for(GenericTypeBound genericTypeBound : signature.getGenericTypeBounds()) {
            Type boundType = scopeBuilder.resolveTypeOf(genericTypeBound.getBound());
            if(boundType.isGeneric()) {
                session.add(new ValidationMessage(
                    signature,
                    String.format(
                        "is not valid because generic type bound '%s' for generic '%s' is itself a generic type",
                        genericTypeBound.getGeneric(),
                        genericTypeBound.getBound()
                    ),
                    Severity.ERROR
                ));
            }
        }
    }

    private void validateReturnType(FunctionSignature signature, FunctionSymbol symbol, ValidationSession session) {
        Type type = symbol.getType();
        String typeToken = signature.getReturnType();
        if (!type.isKnown()) {
            session.add(new ValidationMessage(
                signature,
                String.format("is not valid because return type '%s' does not exist", typeToken),
                Severity.ERROR
            ));
        }
        if(type.isUnion() && type.isGeneric()) {
            session.add(new ValidationMessage(
                signature,
                String.format(
                    "is not valid because return type '%s' is a mix of union type and generic type. "
                        + "Such type definition is not supported.",
                    typeToken
                ),
                Severity.ERROR
            ));
        }
    }

    private void validateParameters(FunctionSignature signature, FunctionSymbol symbol, ValidationSession session) {
        for(var parameter : symbol.getParameters()) {
            Type type = parameter.getType();
            String typeToken = signature.getParameterTypes().get(parameter.getParameterIndex());
            if(!type.isKnown()) {
                session.add(new ValidationMessage(
                    signature,
                    String.format("is not valid because parameter type '%s' does not exist", typeToken),
                    Severity.ERROR
                ));
            }
            if(type.isUnion() && type.isGeneric()) {
                session.add(new ValidationMessage(
                    signature,
                    String.format(
                        "is not valid because parameter type '%s' is a mix of union type and generic type. "
                            + "Such type definition is not supported.",
                        typeToken
                    ),
                    Severity.ERROR
                ));
            }
        }
    }
}