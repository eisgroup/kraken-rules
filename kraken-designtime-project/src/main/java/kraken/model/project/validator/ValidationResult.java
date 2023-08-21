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

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import static kraken.model.project.validator.Severity.ERROR;
import static kraken.model.project.validator.Severity.INFO;
import static kraken.model.project.validator.Severity.WARNING;
import static kraken.utils.MessageUtils.withSpaceBeforeEachLine;

import kraken.model.Dimension;
import kraken.model.Function;
import kraken.model.FunctionSignature;
import kraken.model.KrakenModelItem;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.entrypoint.EntryPoint;

/**
 * @author mulevicius
 */
public class ValidationResult {

    private final Map<KrakenModelItem, KrakenModelItemValidationResult> modelValidationResults;

    private final List<ValidationMessage> allMessages;
    private final List<ValidationMessage> errorMessages;
    private final List<ValidationMessage> warningMessages;
    private final List<ValidationMessage> infoMessages;

    public ValidationResult(List<ValidationMessage> allMessages) {
        this.allMessages = allMessages;

        this.errorMessages = allMessages.stream().filter(m -> m.getSeverity() == ERROR).collect(Collectors.toList());
        this.warningMessages = allMessages.stream().filter(m -> m.getSeverity() == WARNING).collect(Collectors.toList());
        this.infoMessages = allMessages.stream().filter(m -> m.getSeverity() == INFO).collect(Collectors.toList());

        this.modelValidationResults = allMessages.stream()
            .collect(Collectors.groupingBy(ValidationMessage::getItem)).values().stream()
            .map(KrakenModelItemValidationResult::new)
            .collect(Collectors.toMap(KrakenModelItemValidationResult::getKrakenModelItem, r -> r));
    }

    public List<ValidationMessage> getAllMessages() {
        return Collections.unmodifiableList(allMessages);
    }
    public boolean hasErrorMessages() {
        return !errorMessages.isEmpty();
    }
    public boolean hasWarningMessages() {
        return !warningMessages.isEmpty();
    }
    public boolean hasInfoMessages() {
        return !infoMessages.isEmpty();
    }
    public List<ValidationMessage> getErrorMessages() {
        return Collections.unmodifiableList(errorMessages);
    }
    public List<ValidationMessage> getWarningMessages() {
        return Collections.unmodifiableList(warningMessages);
    }
    public List<ValidationMessage> getInfoMessages() {
        return Collections.unmodifiableList(infoMessages);
    }

    public List<ValidationMessage> findMessagesFor(KrakenModelItem item) {
        return Optional.ofNullable(modelValidationResults.get(item))
            .map(KrakenModelItemValidationResult::getAllMessages)
            .orElse(List.of());
    }
    public List<ValidationMessage> findErrorMessagesFor(KrakenModelItem item) {
        return Optional.ofNullable(modelValidationResults.get(item))
            .map(KrakenModelItemValidationResult::getErrorMessages)
            .orElse(List.of());
    }
    public List<ValidationMessage> findWarningMessagesFor(KrakenModelItem item) {
        return Optional.ofNullable(modelValidationResults.get(item))
            .map(KrakenModelItemValidationResult::getWarningMessages)
            .orElse(List.of());
    }
    public List<ValidationMessage> findInfoMessagesFor(KrakenModelItem item) {
        return Optional.ofNullable(modelValidationResults.get(item))
            .map(KrakenModelItemValidationResult::getInfoMessages)
            .orElse(List.of());
    }

    public String formatErrorMessages() {
        return modelValidationResults.values().stream()
            .filter(KrakenModelItemValidationResult::hasErrorMessages)
            .map(itemResult -> formatMessage(itemResult.getKrakenModelItem(), itemResult.getErrorMessages()))
            .collect(Collectors.joining(System.lineSeparator()));
    }

    public void logAllFormattedMessages(Logger logger) {
        modelValidationResults.values().forEach(itemResult -> {
            if(itemResult.hasInfoMessages()) {
                logger.info(formatMessage(itemResult.getKrakenModelItem(), itemResult.getInfoMessages()));
            }
            if(itemResult.hasWarningMessages()) {
                logger.warn(formatMessage(itemResult.getKrakenModelItem(), itemResult.getWarningMessages()));
            }
            if(itemResult.hasErrorMessages()) {
                logger.error(formatMessage(itemResult.getKrakenModelItem(), itemResult.getErrorMessages()));
            }
        });
    }

    private String formatMessage(KrakenModelItem item, List<ValidationMessage> messages) {
        var joinedMessages = messages.stream()
            .map(m -> MessageFormat.format("[{0}] {1}", m.getCode(), m.getMessage()))
            .collect(Collectors.joining(System.lineSeparator()));

        return MessageFormat.format(
            "{0} ''{1}'' has validation messages:" + System.lineSeparator() + "{2}",
            krakenModelItemToTypeString(item),
            item.getName(),
            withSpaceBeforeEachLine(joinedMessages)
        );
    }

    private String krakenModelItemToTypeString(KrakenModelItem item) {
        if(item instanceof Rule) {
            return "Rule";
        }
        if(item instanceof EntryPoint) {
            return "Entry point";
        }
        if(item instanceof ContextDefinition) {
            return "Context definition";
        }
        if(item instanceof Function) {
            return "Function";
        }
        if(item instanceof FunctionSignature) {
            return "Function signature";
        }
        if(item instanceof Dimension) {
            return "Dimension";
        }
        if(item instanceof ExternalContext) {
            return "External context";
        }
        if(item instanceof ExternalContextDefinition) {
            return "External context definition";
        }
        throw new IllegalArgumentException("Unknown kraken model item type: " + item.getClass().getName());
    }


    private static class KrakenModelItemValidationResult {

        private final KrakenModelItem krakenModelItem;
        private final List<ValidationMessage> allMessages;
        private final List<ValidationMessage> errorMessages;
        private final List<ValidationMessage> warningMessages;
        private final List<ValidationMessage> infoMessages;

        KrakenModelItemValidationResult(List<ValidationMessage> allMessages) {
            this.krakenModelItem = allMessages.get(0).getItem();
            this.allMessages = allMessages;
            this.errorMessages = allMessages.stream().filter(m -> m.getSeverity() == ERROR).collect(Collectors.toList());
            this.warningMessages = allMessages.stream().filter(m -> m.getSeverity() == WARNING).collect(Collectors.toList());
            this.infoMessages = allMessages.stream().filter(m -> m.getSeverity() == INFO).collect(Collectors.toList());
        }

        public KrakenModelItem getKrakenModelItem() {
            return krakenModelItem;
        }
        public List<ValidationMessage> getAllMessages() {
            return Collections.unmodifiableList(allMessages);
        }
        public List<ValidationMessage> getErrorMessages() {
            return Collections.unmodifiableList(errorMessages);
        }
        public List<ValidationMessage> getWarningMessages() {
            return Collections.unmodifiableList(warningMessages);
        }
        public List<ValidationMessage> getInfoMessages() {
            return Collections.unmodifiableList(infoMessages);
        }
        public boolean hasErrorMessages() {
            return !errorMessages.isEmpty();
        }
        public boolean hasWarningMessages() {
            return !warningMessages.isEmpty();
        }
        public boolean hasInfoMessages() {
            return !infoMessages.isEmpty();
        }
    }
}
