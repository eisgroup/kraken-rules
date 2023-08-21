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

import static kraken.model.project.validator.ValidationMessageBuilder.Message.CONTEXT_FIELD_CARDINALITY_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.CONTEXT_FIELD_NAME_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.CONTEXT_FIELD_PATH_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.CONTEXT_FIELD_TYPE_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.CONTEXT_FIELD_WRONG_MAP_KEY;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.CONTEXT_NAME_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.CONTEXT_NAVIGATION_CARDINALITY_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.CONTEXT_NAVIGATION_EXPRESSION_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.CONTEXT_NAVIGATION_TARGET_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.CONTEXT_NAVIGATION_WRONG_MAP_KEY;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.CONTEXT_NAVIGATION_IN_SYSTEM_CONTEXT;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.CONTEXT_PARENT_IN_SYSTEM_CONTEXT;

import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.model.context.ContextNavigation;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessageBuilder;
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
            var m = ValidationMessageBuilder.create(CONTEXT_NAME_IS_NULL, contextDefinition).build();
            session.add(m);
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
            var m = ValidationMessageBuilder.create(CONTEXT_PARENT_IN_SYSTEM_CONTEXT, contextDefinition).build();
            session.add(m);
        }

        if (contextDefinition.getChildren() != null && contextDefinition.getChildren().size() > 0) {
            var m = ValidationMessageBuilder.create(CONTEXT_NAVIGATION_IN_SYSTEM_CONTEXT, contextDefinition).build();
            session.add(m);
        }
    }

    private void validateContextChildren(ContextDefinition contextDefinition, ValidationSession session) {
        contextDefinition.getChildren().entrySet().stream()
            .filter(e -> !e.getKey().equals(e.getValue().getTargetName()))
            .forEach(e -> session.add(
                ValidationMessageBuilder.create(CONTEXT_NAVIGATION_WRONG_MAP_KEY, contextDefinition).build()
            ));

        for(ContextNavigation contextNavigation : contextDefinition.getChildren().values()) {
            if(contextNavigation.getTargetName() == null) {
                var m = ValidationMessageBuilder.create(CONTEXT_NAVIGATION_TARGET_IS_NULL, contextDefinition).build();
                session.add(m);
            }
            if(contextNavigation.getCardinality() == null) {
                var m = ValidationMessageBuilder.create(CONTEXT_NAVIGATION_CARDINALITY_IS_NULL, contextDefinition).build();
                session.add(m);
            }
            if(contextNavigation.getNavigationExpression() == null) {
                var m = ValidationMessageBuilder.create(CONTEXT_NAVIGATION_EXPRESSION_IS_NULL, contextDefinition).build();
                session.add(m);
            }
        }
    }

    private void validateContextFields(ContextDefinition contextDefinition, ValidationSession session) {
        contextDefinition.getContextFields().entrySet().stream()
            .filter(e -> !e.getKey().equals(e.getValue().getName()))
            .forEach(e -> session.add(
                ValidationMessageBuilder.create(CONTEXT_FIELD_WRONG_MAP_KEY, contextDefinition).build()
            ));

        for (ContextField contextField : contextDefinition.getContextFields().values()) {
            if (contextField.getName() == null) {
                var m = ValidationMessageBuilder.create(CONTEXT_FIELD_NAME_IS_NULL, contextDefinition).build();
                session.add(m);
            }
            if (contextField.getFieldType() == null) {
                var m = ValidationMessageBuilder.create(CONTEXT_FIELD_TYPE_IS_NULL, contextDefinition).build();
                session.add(m);
            }
            if (contextField.getCardinality() == null) {
                var m = ValidationMessageBuilder.create(CONTEXT_FIELD_CARDINALITY_IS_NULL, contextDefinition).build();
                session.add(m);
            }
            if (contextField.getFieldPath() == null) {
                var m = ValidationMessageBuilder.create(CONTEXT_FIELD_PATH_IS_NULL, contextDefinition).build();
                session.add(m);
            }
        }
    }

}
