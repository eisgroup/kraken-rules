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

import static kraken.model.project.validator.ValidationMessageBuilder.Message.CONTEXT_NAVIGATION_IS_SYSTEM_CONTEXT;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.CONTEXT_NAVIGATION_UNKNOWN_CHILDREN;

import java.util.Map;

import kraken.model.context.ContextDefinition;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.model.project.validator.ValidationMessageBuilder.Message;
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
        Map<String, ContextDefinition> allContextDefinitions = krakenProject.getContextDefinitions();

        allContextDefinitions.values()
            .forEach(contextDefinition -> contextDefinition.getChildren().values()
                .forEach(contextNavigation -> {
                    ContextDefinition childContext = allContextDefinitions.get(contextNavigation.getTargetName());

                    if (childContext == null) {
                        var m = ValidationMessageBuilder.create(CONTEXT_NAVIGATION_UNKNOWN_CHILDREN, contextDefinition)
                            .parameters(contextNavigation.getTargetName())
                            .build();

                        session.add(m);
                    } else if (childContext.isSystem()) {
                        var m = ValidationMessageBuilder.create(CONTEXT_NAVIGATION_IS_SYSTEM_CONTEXT, contextDefinition)
                            .parameters(contextNavigation.getTargetName())
                            .build();

                        session.add(m);
                    }
                })
            );
    }

}
