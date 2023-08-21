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

import static kraken.model.project.validator.ValidationMessageBuilder.Message.CONTEXT_PARENT_DUPLICATE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.CONTEXT_PARENT_IS_SYSTEM_CONTEXT;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.CONTEXT_PARENT_UNKNOWN;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.CONTEXT_PARENT_WRONG_FIELD_TYPE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.CONTEXT_PARENT_WRONG_STRICTNESS;

import java.util.List;
import java.util.stream.Collectors;

import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.model.project.validator.ValidationSession;

/**
 * @author mulevicius
 */
public final class ContextDefinitionInheritanceValidator {

    private final KrakenProject krakenProject;

    public ContextDefinitionInheritanceValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
    }

    public void validate(ValidationSession session) {
        for(ContextDefinition contextDefinition : krakenProject.getContextDefinitions().values()) {
            var groupedDefinitions = contextDefinition.getParentDefinitions().stream()
                .collect(Collectors.groupingBy(n -> n, Collectors.collectingAndThen(Collectors.toList(), List::size)));
            for(var groupedDefinition : groupedDefinitions.entrySet()) {
                if(groupedDefinition.getValue() > 1) {
                    var m = ValidationMessageBuilder.create(CONTEXT_PARENT_DUPLICATE, contextDefinition)
                        .parameters(groupedDefinition.getKey())
                        .build();
                    session.add(m);
                }
            }
            for(String inheritedContextName : contextDefinition.getParentDefinitions()) {
                ContextDefinition inheritedContextDefinition = krakenProject.getContextProjection(inheritedContextName);
                if(inheritedContextDefinition == null) {
                    var m = ValidationMessageBuilder.create(CONTEXT_PARENT_UNKNOWN, contextDefinition)
                        .parameters(inheritedContextName)
                        .build();
                    session.add(m);
                } else {
                    if (inheritedContextDefinition.isSystem()) {
                        var m = ValidationMessageBuilder.create(CONTEXT_PARENT_IS_SYSTEM_CONTEXT, contextDefinition)
                            .parameters(inheritedContextName)
                            .build();
                        session.add(m);
                    }

                    if(contextDefinition.isStrict() && !inheritedContextDefinition.isStrict()) {
                        var m = ValidationMessageBuilder.create(CONTEXT_PARENT_WRONG_STRICTNESS, contextDefinition)
                            .parameters(inheritedContextName)
                            .build();
                        session.add(m);
                    }
                    for(ContextField inheritedField : inheritedContextDefinition.getContextFields().values()) {
                        if(contextDefinition.getContextFields().containsKey(inheritedField.getName())) {
                            ContextField contextField = contextDefinition.getContextFields().get(inheritedField.getName());

                            if(!areOverrideCompatible(contextField, inheritedField)) {
                                var m = ValidationMessageBuilder.create(CONTEXT_PARENT_WRONG_FIELD_TYPE, contextDefinition)
                                    .parameters(contextField.getName(), inheritedContextName)
                                    .build();
                                session.add(m);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean areOverrideCompatible(ContextField field1, ContextField field2) {
        return field1.getFieldPath().equals(field2.getFieldPath())
                && field1.getCardinality() == field2.getCardinality();
    }
}
