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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import static kraken.model.project.validator.Severity.ERROR;
import static kraken.model.project.validator.Severity.INFO;
import static kraken.model.project.validator.Severity.WARNING;

/**
 * @author mulevicius
 */
public class ValidationResult {

    private List<ValidationMessage> validationMessages;

    public ValidationResult(List<ValidationMessage> validationMessages) {
        this.validationMessages = Objects.requireNonNull(validationMessages);
    }

    public List<ValidationMessage> getErrors() {
        return validationMessages.stream().filter(m -> m.getSeverity() == ERROR).collect(Collectors.toList());
    }

    public List<ValidationMessage> getWarnings() {
        return validationMessages.stream().filter(m -> m.getSeverity() == WARNING).collect(Collectors.toList());
    }

    public List<ValidationMessage> getInfo() {
        return validationMessages.stream().filter(m -> m.getSeverity() == INFO).collect(Collectors.toList());
    }

    public List<ValidationMessage> getValidationMessages() {
        return Collections.unmodifiableList(validationMessages);
    }

    public void logMessages(Logger logger) {
        validationMessages.stream().forEach(m -> {
            if (m.getSeverity() == INFO) {
                logger.info(m.toString());
            }
            if (m.getSeverity() == WARNING) {
                logger.warn(m.toString());
            }
            if (m.getSeverity() == ERROR) {
                logger.error(m.toString());
            }
        });
    }

    @Override
    public String toString() {
        return validationMessages.stream().map(ValidationMessage::toString).collect(Collectors.joining(System.lineSeparator()));
    }
}
