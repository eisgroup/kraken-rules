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
package kraken.model.validation;

import kraken.model.ErrorMessage;

/**
 * Parent class for all validation payload subtypes, used to define validation logic
 * Defines error message and severity.
 *
 * @author rimas
 * @since 1.0
 */
public abstract class ValidationPayloadImpl implements ValidationPayload {

    /**
     * Error message to be used if validation fails
     */
    private ErrorMessage errorMessage;

    /**
     * Severity for validation failure
     */
    private ValidationSeverity severity;

    private boolean isOverridable;

    private String overrideGroup;

    // used for deserialization only
    private String type;

    public ValidationPayloadImpl() {
        this.type = getPayloadType().getTypeName();
    }

    @Override public void setErrorMessage(ErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    @Override public void setSeverity(ValidationSeverity severity) {
        this.severity = severity;
    }

    @Override public ValidationSeverity getSeverity() {
        return severity;
    }

    @Override
    public boolean isOverridable() {
        return this.isOverridable;
    }

    @Override
    public void setOverridable(boolean isOverridable) {
        this.isOverridable = isOverridable;
    }

    @Override
    public String getOverrideGroup() {
        return overrideGroup;
    }

    @Override
    public void setOverrideGroup(String overrideGroup) {
        this.overrideGroup = overrideGroup;
    }
}
