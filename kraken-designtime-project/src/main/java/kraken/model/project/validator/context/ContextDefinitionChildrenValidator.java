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
package kraken.model.project.validator.context;

import kraken.model.context.ContextDefinition;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;

/**
 * @author mulevicius
 */
public final class ContextDefinitionChildrenValidator {

    private final KrakenProject krakenProject;

    public ContextDefinitionChildrenValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
    }

    public void validate(ValidationSession session) {
        for(ContextDefinition contextDefinition : krakenProject.getContextDefinitions().values()) {
            contextDefinition.getChildren().values().stream()
                    .filter(c -> !krakenProject.getContextDefinitions().containsKey(c.getTargetName()))
                    .forEach(c -> session.add(new ValidationMessage(contextDefinition,
                            String.format("child '%s' is not valid because such context does not exist",
                                    c.getTargetName()),
                            Severity.ERROR)
                    ));

        }
    }

}
