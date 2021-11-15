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

import static kraken.model.project.validator.Severity.ERROR;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import kraken.model.KrakenModelItem;
import kraken.model.context.PrimitiveFieldDataType;
import kraken.model.context.SystemDataTypes;
import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.context.external.ExternalContextDefinitionReference;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessage;
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
        validateContextDefinitions(krakenProject.getExternalContextDefinitions(), session);
    }

    private void validateRootExternalContext(ExternalContext rootExternalContext, ValidationSession session) {
        if(!rootExternalContext.getContexts().isEmpty()) {
            if(rootExternalContext.getContexts().size() > 1 || rootExternalContext.getContexts().get("context") == null) {
                String msg = String.format(
                    "Root ExternalContext definition should be empty or have ONE element named 'context', but found: %s",
                    String.join(", ", rootExternalContext.getContexts().keySet())
                );

                session.add(new ValidationMessage(rootExternalContext, msg, ERROR));
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
            session.add(error(externalContext, String.format(
                "External context definitions referenced in External Context definition " +
                    "should be available in kraken project, but following referenced values are not found: %s",
                String.join(System.lineSeparator(), nonExistingRefs))));
        }
    }

    private void validateNamingClashes(ExternalContext externalContext, ValidationSession session) {
        Set<String> clashingNames = getClashingNames(externalContext);

        if(clashingNames.size() > 0) {
            session.add(error(externalContext, String.format(
                "Naming clash between external context definitions and child external context " +
                    "found, clashing values: %s",
                String.join(System.lineSeparator(), clashingNames))));
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
        ValidationSession session
    ) {
        externalContextDefinitions.values().stream()
            .flatMap(ecd -> ecd.getAttributes().values().stream()
                .filter(attribute -> {
                    String type = attribute.getType().getType();

                    return !PrimitiveFieldDataType.isPrimitiveType(type)
                        && !SystemDataTypes.isSystemDataType(type)
                        && !externalContextDefinitions.containsKey(type);})
                .map(attribute -> error(ecd,
                    String.format("type '%s' of field '%s' is unknown or not supported",
                        attribute.getType().getType(),
                        attribute.getName()))))
            .forEach(validationMessage -> session.add(validationMessage));
    }

    private ValidationMessage error(KrakenModelItem krakenModelItem, String message) {
        return new ValidationMessage(krakenModelItem, message, ERROR);
    }
}
