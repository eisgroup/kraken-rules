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
package kraken.model.project.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kraken.model.FunctionSignature;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.entrypoint.EntryPoint;

/**
 * Holds state of ongoing kraken project validation and allows to query that state to determine the sequence of
 * validation.
 *
 * @author mulevicius
 */
public class ValidationSession {

    private final List<ValidationMessage> validationMessages = new ArrayList<>();

    private final List<ValidationMessage> contextValidationMessages = new ArrayList<>();
    private final List<ValidationMessage> externalContextValidationMessages = new ArrayList<>();
    private final List<ValidationMessage> functionSignatureValidationMessages = new ArrayList<>();

    private final List<ValidationMessage> ruleValidationMessages = new ArrayList<>();
    private final List<ValidationMessage> entryPointValidationMessages = new ArrayList<>();

    public void addAll(List<ValidationMessage> messages) {
        for(ValidationMessage message : messages) {
            add(message);
        }
    }

    public void add(ValidationMessage message) {
        validationMessages.add(message);

        if(message.getItem() instanceof Rule) {
            ruleValidationMessages.add(message);
        } else if(message.getItem() instanceof EntryPoint) {
            entryPointValidationMessages.add(message);
        } else if(message.getItem() instanceof ContextDefinition) {
            contextValidationMessages.add(message);
        } else if(message.getItem() instanceof ExternalContextDefinition
            || message.getItem() instanceof ExternalContext) {
            externalContextValidationMessages.add(message);
        } else if(message.getItem() instanceof FunctionSignature) {
            functionSignatureValidationMessages.add(message);
        } else {
            throw new IllegalStateException("Unknown Kraken Model Type encountered: " + message.getItem().getClass());
        }
    }

    public boolean hasContextDefinitionError() {
        return contextValidationMessages.stream().anyMatch(m -> m.getSeverity() == Severity.ERROR);
    }

    public boolean hasExternalContextError() {
        return externalContextValidationMessages.stream().anyMatch(m -> m.getSeverity() == Severity.ERROR);
    }

    public boolean hasFunctionSignatureError() {
        return functionSignatureValidationMessages.stream().anyMatch(m -> m.getSeverity() == Severity.ERROR);
    }

    public boolean hasEntryPointError() {
        return entryPointValidationMessages.stream().anyMatch(m -> m.getSeverity() == Severity.ERROR);
    }

    public boolean hasRuleError() {
        return ruleValidationMessages.stream().anyMatch(m -> m.getSeverity() == Severity.ERROR);
    }

    public ValidationResult result() {
        return new ValidationResult(validationMessages);
    }

    public List<ValidationMessage> getValidationMessages() {
        return Collections.unmodifiableList(validationMessages);
    }

}
