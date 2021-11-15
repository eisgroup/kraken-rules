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
package kraken.model.project.validator;

import java.text.MessageFormat;
import java.util.Objects;

import kraken.annotations.API;
import kraken.model.FunctionSignature;
import kraken.model.KrakenModelItem;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.context.external.ExternalContextDefinitionReference;
import kraken.model.entrypoint.EntryPoint;

/**
 * Represents a single message of validation for a single instance of
 * {@link Rule}, {@link EntryPoint} or {@link ContextDefinition}
 *
 * @author mulevicius
 */
@API
public final class ValidationMessage {

    private final KrakenModelItem item;

    private final String message;

    private final Severity severity;

    /**
     *
     * @param item is an instance of {@link Rule}, {@link EntryPoint} or {@link ContextDefinition} that is validated
     * @param message is a human readable text that informs the user about validation result
     * @param severity of a validation result
     */
    public ValidationMessage(KrakenModelItem item, String message, Severity severity) {
        this.item = Objects.requireNonNull(item);
        this.message = Objects.requireNonNull(message);
        this.severity = Objects.requireNonNull(severity);
    }

    public KrakenModelItem getItem() {
        return item;
    }

    public String getMessage() {
        return message;
    }

    public Severity getSeverity() {
        return severity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationMessage that = (ValidationMessage) o;
        return item.getName().equals(that.item.getName()) &&
                message.equals(that.message) &&
                severity == that.severity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(item.getName(), message, severity);
    }

    @Override
    public String toString() {
        String format = "[{0}] {1} - ''{2}'': {3}";
        return MessageFormat.format(format, severity, modelTypeString(), item.getName(), message);
    }

    private String modelTypeString() {
        if(item instanceof Rule) {
            return "Rule";
        }
        if(item instanceof EntryPoint) {
            return "EntryPoint";
        }
        if(item instanceof ContextDefinition) {
            return "ContextDefinition";
        }
        if(item instanceof ExternalContextDefinition) {
            return "External ContextDefinition";
        }
        if(item instanceof ExternalContextDefinitionReference) {
            return "External ContextDefinition Reference";
        }
        if(item instanceof FunctionSignature) {
            return "Function Signature";
        }
        throw new IllegalStateException("Unknown Kraken Model Type encountered: " + item.getClass());
    }
}
