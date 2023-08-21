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
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import kraken.annotations.API;
import kraken.model.KrakenModelItem;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
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

    private final String code;

    private final String message;

    private final String messageTemplate;

    private final Object[] messageTemplateParameters;

    private final Severity severity;

    /**
     *
     * @param item is an instance of {@link Rule}, {@link EntryPoint} or {@link ContextDefinition} that is validated
     * @param message is a human readable text that informs the user about validation result
     * @param severity of a validation result
     * @deprecated since 1.50.0, use {@link #ValidationMessage(KrakenModelItem, String, String, String, Object[], Severity)} instead
     */
    @Deprecated(since = "1.50.0", forRemoval = true)
    public ValidationMessage(KrakenModelItem item, String message, Severity severity) {
        this(
            item,
            UUID.nameUUIDFromBytes(message.getBytes()).toString().substring(0, 8), // generated UUID as message code
            MessageFormat.format(message.replace("'", "''"), new Object[]{}),
            message.replace("'", "''"), // escape single commas for MessageFormat
            new Object[]{},
            severity
        );
    }

    public ValidationMessage(@Nonnull KrakenModelItem item,
                             @Nonnull String code,
                             @Nonnull String message,
                             @Nonnull String messageTemplate,
                             @Nullable Object[] messageTemplateParameters,
                             @Nonnull Severity severity) {
        this.item = Objects.requireNonNull(item);
        this.code = Objects.requireNonNull(code);
        this.message = Objects.requireNonNull(message);
        this.messageTemplate = Objects.requireNonNull(messageTemplate);
        this.messageTemplateParameters = messageTemplateParameters;
        this.severity = Objects.requireNonNull(severity);
    }

    @Nonnull
    public KrakenModelItem getItem() {
        return item;
    }

    @Nonnull
    public String getCode() {
        return code;
    }

    @Nonnull
    public String getMessage() {
        return message;
    }

    @Nonnull
    public String getMessageTemplate() {
        return messageTemplate;
    }

    @Nullable
    public Object[] getMessageTemplateParameters() {
        return messageTemplateParameters;
    }

    @Nonnull
    public Severity getSeverity() {
        return severity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationMessage that = (ValidationMessage) o;
        return item.getName().equals(that.item.getName()) && code.equals(that.code) && message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item.getName(), code, message);
    }

}
