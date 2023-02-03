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

import static kraken.model.project.validator.Severity.ERROR;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessage;
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
                    String template = "parent '%s' is specified twice - please remove duplicated context";
                    String message = String.format(template, groupedDefinition.getKey());
                    session.add(new ValidationMessage(contextDefinition, message, ERROR));
                }
            }
            for(String inheritedContextName : contextDefinition.getParentDefinitions()) {
                ContextDefinition inheritedContextDefinition = krakenProject.getContextProjection(inheritedContextName);
                if(inheritedContextDefinition == null) {
                    String template = "parent '%s' is not valid because such ContextDefinition does not exist";
                    String message = String.format(template, inheritedContextName);
                    session.add(new ValidationMessage(contextDefinition, message, ERROR));
                } else {
                    if (inheritedContextDefinition.isSystem()) {
                        String template = "parent '%s' is not valid because such system"
                            + " ContextDefinition cannot be inherited";
                        String message = String.format(template, inheritedContextName);
                        session.add(new ValidationMessage(contextDefinition, message, ERROR));
                    }

                    if(contextDefinition.isStrict() && !inheritedContextDefinition.isStrict()) {
                        String template = "ContextDefinition is strict but inherited ContextDefinition '%s' is not";
                        String message = String.format(template, inheritedContextDefinition.getName());
                        session.add(new ValidationMessage(contextDefinition, message, ERROR));
                    }
                    for(ContextField inheritedField : inheritedContextDefinition.getContextFields().values()) {
                        if(contextDefinition.getContextFields().containsKey(inheritedField.getName())) {
                            ContextField contextField = contextDefinition.getContextFields().get(inheritedField.getName());
                            if(!areOverrideCompatible(contextField, inheritedField)) {
                                String template = "field '%s' is overridden but it has a different type in inherited ContextDefinition '%s'";
                                String message = String.format(template, contextField.getName(), inheritedContextName);
                                session.add(new ValidationMessage(contextDefinition, message, ERROR));
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
