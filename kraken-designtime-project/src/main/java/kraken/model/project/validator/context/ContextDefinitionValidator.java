/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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

import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.model.context.ContextNavigation;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;
import kraken.model.project.validator.namespaced.NamespacedValidator;

/**
 * @author mulevicius
 */
public class ContextDefinitionValidator {

    private final KrakenProject krakenProject;

    private final ContextDefinitionChildrenValidator contextDefinitionChildrenValidator;
    private final ContextDefinitionInheritanceValidator contextDefinitionInheritanceValidator;

    public ContextDefinitionValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;

        this.contextDefinitionChildrenValidator = new ContextDefinitionChildrenValidator(krakenProject);
        this.contextDefinitionInheritanceValidator = new ContextDefinitionInheritanceValidator(krakenProject);
    }

    public void validate(ValidationSession session) {
        for(ContextDefinition contextDefinition : krakenProject.getContextDefinitions().values()) {
            validate(contextDefinition, session);
        }

        if(!session.hasContextDefinitionError()) {
            contextDefinitionChildrenValidator.validate(session);
            contextDefinitionInheritanceValidator.validate(session);
        }
    }

    private void validate(ContextDefinition contextDefinition, ValidationSession session) {
        if(contextDefinition.getName() == null) {
            session.add(new ValidationMessage(contextDefinition, "name is not defined", ERROR));
        }
        session.addAll(NamespacedValidator.validate(contextDefinition));

        if (contextDefinition.isSystem()) {
            validateSystemContext(contextDefinition, session);
        } else {
            validateModeledContext(contextDefinition, session);
        }
    }

    private void validateModeledContext(ContextDefinition contextDefinition, ValidationSession session) {
        validateContextFields(contextDefinition, session);
        validateContextChildren(contextDefinition, session);
    }

    private void validateSystemContext(ContextDefinition contextDefinition, ValidationSession session) {
        validateContextFields(contextDefinition, session);

        if (contextDefinition.getParentDefinitions() != null && contextDefinition.getParentDefinitions().size() > 0) {
            String message = "Parent contexts are not allowed for system context definitions.";
            session.add(new ValidationMessage(contextDefinition, message, ERROR));
        }

        if (contextDefinition.getChildren() != null && contextDefinition.getChildren().size() > 0) {
            String message = "Child contexts are not allowed for system context definitions.";
            session.add(new ValidationMessage(contextDefinition, message, ERROR));
        }
    }

    private void validateContextChildren(ContextDefinition contextDefinition, ValidationSession session) {
        contextDefinition.getChildren().entrySet().stream()
            .filter(e -> !e.getKey().equals(e.getValue().getTargetName()))
            .forEach(e -> session.add(new ValidationMessage(contextDefinition,
                "children map has key that is different from ContextNavigation.targetName", ERROR)));

        for(ContextNavigation contextNavigation : contextDefinition.getChildren().values()) {
            if(contextNavigation.getTargetName() == null) {
                session.add(new ValidationMessage(contextDefinition,
                    "ContextNavigation.targetName is missing", ERROR));
            }
            if(contextNavigation.getCardinality() == null) {
                session.add(new ValidationMessage(contextDefinition,
                    "ContextNavigation.cardinality is missing", ERROR));
            }
            if(contextNavigation.getNavigationExpression() == null) {
                session.add(new ValidationMessage(contextDefinition,
                    "ContextNavigation.navigationExpression is missing", ERROR));
            }
        }
    }

    private void validateContextFields(ContextDefinition contextDefinition, ValidationSession session) {
        contextDefinition.getContextFields().entrySet().stream()
            .filter(e -> !e.getKey().equals(e.getValue().getName()))
            .forEach(e -> session.add(new ValidationMessage(contextDefinition,
                "ContextFields map has has key that is different from ContextField.name", ERROR)));

        for (ContextField contextField : contextDefinition.getContextFields().values()) {
            if (contextField.getName() == null) {
                session.add(new ValidationMessage(contextDefinition,
                    "ContextField.name is missing", ERROR));
            }
            if (contextField.getFieldType() == null) {
                session.add(new ValidationMessage(contextDefinition,
                    "ContextField.fieldType is missing", ERROR));
            }
            if (contextField.getCardinality() == null) {
                session.add(new ValidationMessage(contextDefinition,
                    "ContextField.cardinality is missing", ERROR));
            }
            if (contextField.getFieldPath() == null) {
                session.add(new ValidationMessage(contextDefinition,
                    "ContextField.fieldPath is missing", ERROR));
            }
        }
    }

}
