/*
 *  Copyright 2020 EIS Ltd and/or one of its affiliates.
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

import static kraken.model.project.validator.ValidationMessageBuilder.Message.EXTERNAL_CONTEXT_CHILD_CLASH;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.EXTERNAL_CONTEXT_REFERENCE_MISSING;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.EXTERNAL_CONTEXT_ROOT_MISSING;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.EXTERNAL_CONTEXT_UNKNOWN_FIELD_TYPE;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import kraken.model.context.ContextDefinition;
import kraken.model.context.PrimitiveFieldDataType;
import kraken.model.context.SystemDataTypes;
import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.context.external.ExternalContextDefinitionReference;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.model.project.validator.ValidationSession;

/**
 * Validator for validating {@code ExternalContext} defined in kraken project.
 *
 * @author Tomas Dapkunas
 * @since 1.3.0
 */
public class ExternalContextValidator {

    private final KrakenProject krakenProject;

    public ExternalContextValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
    }

    public void validate(ValidationSession session) {
        if(krakenProject.getExternalContext() != null) {
            validateRootExternalContext(krakenProject.getExternalContext(), session);
        }
        var systemContextDefinitions = krakenProject.getContextDefinitions().values().stream()
            .filter(ContextDefinition::isSystem)
            .collect(Collectors.toMap(ContextDefinition::getName, c -> c));
        validateContextDefinitions(krakenProject.getExternalContextDefinitions(), systemContextDefinitions, session);
    }

    private void validateRootExternalContext(ExternalContext rootExternalContext, ValidationSession session) {
        if(!rootExternalContext.getContexts().isEmpty()) {
            if(rootExternalContext.getContexts().size() > 1 || rootExternalContext.getContexts().get("context") == null) {
                var m = ValidationMessageBuilder.create(EXTERNAL_CONTEXT_ROOT_MISSING, rootExternalContext)
                    .parameters(String.join(", ", rootExternalContext.getContexts().keySet()))
                    .build();
                session.add(m);
            }
        }
        validateChildrenContexts(rootExternalContext.getContexts(), session);
    }

    private void validateChildrenContexts(Map<String, ExternalContext> childContexts, ValidationSession session) {
        for(ExternalContext childContext : childContexts.values()) {
            validateReferencedExternalContexts(childContext, session);
            validateNamingClashes(childContext, session);
            validateChildrenContexts(childContext.getContexts(), session);
        }
    }

    private void validateReferencedExternalContexts(ExternalContext externalContext, ValidationSession session) {
        Set<String> nonExistingRefs = getNonExistingReferences(externalContext);
        if(nonExistingRefs.size() > 0) {
            var m = ValidationMessageBuilder.create(EXTERNAL_CONTEXT_REFERENCE_MISSING, externalContext)
                .parameters(String.join(System.lineSeparator(), nonExistingRefs))
                .build();
            session.add(m);
        }
    }

    private void validateNamingClashes(ExternalContext externalContext, ValidationSession session) {
        Set<String> clashingNames = getClashingNames(externalContext);

        if(clashingNames.size() > 0) {
            var m = ValidationMessageBuilder.create(EXTERNAL_CONTEXT_CHILD_CLASH, externalContext)
                .parameters(String.join(System.lineSeparator(), clashingNames))
                .build();
            session.add(m);
        }
    }

    private Set<String> getNonExistingReferences(ExternalContext externalContext) {
        Set<String> referencedCtxNames = externalContext.getExternalContextDefinitions()
                .values()
                .stream()
                .map(ExternalContextDefinitionReference::getName)
                .collect(Collectors.toSet());
        referencedCtxNames.removeAll(krakenProject.getExternalContextDefinitions().keySet());

        return referencedCtxNames;
    }

    private Set<String> getClashingNames(ExternalContext externalContext) {
        Set<String> result = new HashSet<>(externalContext.getContexts().keySet());
        result.retainAll(externalContext.getExternalContextDefinitions().keySet());

        return result;
    }

    private void validateContextDefinitions(
        Map<String, ExternalContextDefinition> externalContextDefinitions,
        Map<String, ContextDefinition> systemContextDefinitions,
        ValidationSession session
    ) {
        externalContextDefinitions.values().stream()
            .flatMap(ecd -> ecd.getAttributes().values().stream()
                .filter(attribute -> {
                    String type = attribute.getType().getType();

                    return !PrimitiveFieldDataType.isPrimitiveType(type)
                        && !SystemDataTypes.isSystemDataType(type)
                        && !systemContextDefinitions.containsKey(type)
                        && !externalContextDefinitions.containsKey(type);
                })
                .map(attribute ->
                    ValidationMessageBuilder.create(EXTERNAL_CONTEXT_UNKNOWN_FIELD_TYPE, ecd)
                    .parameters(attribute.getType().getType(), attribute.getName())
                    .build()))
            .forEach(validationMessage -> session.add(validationMessage));
    }

}
