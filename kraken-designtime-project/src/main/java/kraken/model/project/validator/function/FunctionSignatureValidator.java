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

import kraken.el.scope.type.Type;
import kraken.model.FunctionSignature;
import kraken.model.project.KrakenProject;
import kraken.model.project.scope.ScopeBuilder;
import kraken.model.project.scope.ScopeBuilderProvider;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;
import kraken.model.project.validator.namespaced.NamespacedValidator;

/**
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
        for (FunctionSignature functionSignature : krakenProject.getFunctionSignatures()) {
            session.addAll(NamespacedValidator.validate(functionSignature));

            Type returnType = scopeBuilder.resolveTypeOf(functionSignature.getReturnType());
            if (!returnType.isKnown()) {
                session.add(new ValidationMessage(
                    functionSignature,
                    String.format(
                        "Function Signature '%s' is not valid because return type '%s' does not exist",
                        FunctionSignature.format(functionSignature),
                        functionSignature.getReturnType()
                    ),
                    Severity.ERROR
                ));
            }
            functionSignature.getParameterTypes().stream()
                .distinct()
                .filter(parameterType -> !scopeBuilder.resolveTypeOf(parameterType).isKnown())
                .map(parameterType -> String.format(
                    "Function Signature '%s' is not valid because parameter type '%s' does not exist",
                    FunctionSignature.format(functionSignature),
                    parameterType))
                .map(errorMessage -> new ValidationMessage(functionSignature, errorMessage, Severity.ERROR))
                .forEach(session::add);
        }
    }
}
