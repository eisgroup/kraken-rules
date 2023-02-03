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

import kraken.model.Function;
import kraken.model.FunctionParameter;
import kraken.model.ParameterDocumentation;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.Duplicates;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;

/**
 * Validates documentation defined on {@link Function} in KrakenProject
 *
 * @author mulevicius
 */
public final class FunctionDocumentationValidator {

    private final KrakenProject krakenProject;

    public FunctionDocumentationValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
    }

    public void validate(ValidationSession session) {
        for (Function function : krakenProject.getFunctions()) {
            validateFunctionDocumentation(function, session);
        }
    }

    private void validateFunctionDocumentation(Function function, ValidationSession session) {
        if(function.getDocumentation() == null) {
            return;
        }
        var documentation = function.getDocumentation();

        Duplicates.findAndDo(
            documentation.getParameterDocumentations(),
            ParameterDocumentation::getParameterName,
            duplicates -> session.add(
                new ValidationMessage(
                    function,
                    "Error in function documentation. Found more than one description for parameter: "
                        + duplicates.get(0).getParameterName(),
                    Severity.ERROR
                )
            )
        );

        var parameters = function.getParameters().stream()
            .collect(Collectors.toMap(FunctionParameter::getName, p -> p));

        for(ParameterDocumentation parameterDocumentation : documentation.getParameterDocumentations()) {
            if(!parameters.containsKey(parameterDocumentation.getParameterName())) {
                String availableParametersString = String.join(", ", parameters.keySet());
                session.add(
                    new ValidationMessage(
                        function,
                        "Error in function documentation. Parameter is documented but does not exist: "
                            + parameterDocumentation.getParameterName()
                            + ". Function has defined only these parameters: "
                            + availableParametersString,
                        Severity.ERROR
                    )
                );
            }
        }
    }
}
