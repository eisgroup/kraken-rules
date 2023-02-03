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
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
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
            duplicates -> session.add(new ValidationMessage(
                duplicates.get(0),
                String.format(
                    "is not valid because there are more than one function defined with the same name: '%s'",
                    duplicates.get(0).getName()
                ),
                Severity.ERROR
            ))
        );

        for (Function function : krakenProject.getFunctions()) {
            validateFunction(function, session);
        }
    }

    private void validateFunction(Function function, ValidationSession session) {
        session.addAll(NamespacedValidator.validate(function));

        if(functionSignatureNames.contains(function.getName())) {
            session.add(new ValidationMessage(
                function,
                String.format(
                    "is not valid because function signature with the same name '%s' is defined",
                    function.getName()
                ),
                Severity.ERROR
            ));
        }

        if(nativeFunctionNames.contains(function.getName())) {
            session.add(new ValidationMessage(
                function,
                String.format(
                    "is not valid because native function with name '%s' already exists",
                    function.getName()
                ),
                Severity.ERROR
            ));
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
                session.add(new ValidationMessage(
                    function,
                    String.format(
                        "is not valid because there are more than one generic bound for the same generic type "
                            + "name '%s'",
                        duplicates.get(0).getGeneric()
                    ),
                    Severity.ERROR
                ));
            }
        );

        for(GenericTypeBound genericTypeBound : function.getGenericTypeBounds()) {
            Type boundType = scopeBuilder.resolveTypeOf(genericTypeBound.getBound());
            if(boundType.isGeneric()) {
                session.add(new ValidationMessage(
                    function,
                    String.format(
                        "is not valid because generic type bound '%s' for generic '%s' is itself a generic type",
                        genericTypeBound.getBound(),
                        genericTypeBound.getGeneric()
                    ),
                    Severity.ERROR
                ));
            }
        }
    }

    private void validateReturnType(Function function, FunctionSymbol symbol, ValidationSession session) {
        Type type = symbol.getType();
        String typeToken = function.getReturnType();
        if (!type.isKnown()) {
            session.add(new ValidationMessage(
                function,
                String.format(
                    "is not valid because return type '%s' does not exist",
                    typeToken
                ),
                Severity.ERROR
            ));
        }
        if(type.isUnion() && type.isGeneric()) {
            session.add(new ValidationMessage(
                function,
                String.format(
                    "is not valid because return type '%s' is a mix of union type and generic type. "
                        + "Such type definition is not supported.",
                    typeToken
                ),
                Severity.ERROR
            ));
        }
    }

    private void validateParameters(Function function, FunctionSymbol symbol, ValidationSession session) {
        Duplicates.findAndDo(
            function.getParameters(),
            FunctionParameter::getName,
            duplicates -> session.add(new ValidationMessage(
                function,
                String.format(
                    "is not valid because there are more than one parameter with the same name defined: '%s'",
                    duplicates.get(0).getName()
                ),
                Severity.ERROR
            ))
        );

        for(var parameter : symbol.getParameters()) {
            Type type = parameter.getType();
            String typeToken = function.getParameters().get(parameter.getParameterIndex()).getType();
            if(!type.isKnown()) {
                session.add(new ValidationMessage(
                    function,
                    String.format("is not valid because parameter type '%s' does not exist", typeToken),
                    Severity.ERROR
                ));
            }
            if(type.isUnion() && type.isGeneric()) {
                session.add(new ValidationMessage(
                    function,
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
