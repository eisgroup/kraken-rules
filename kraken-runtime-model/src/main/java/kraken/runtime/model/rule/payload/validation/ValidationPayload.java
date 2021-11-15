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
package kraken.runtime.model.rule.payload.validation;

import kraken.model.payload.PayloadType;
import kraken.model.validation.ValidationSeverity;
import kraken.runtime.model.rule.payload.Payload;

/**
 * @author psurinin@eisgroup.com
 * @since 1.1.0
 */
public abstract class ValidationPayload implements Payload {

    private final ErrorMessage errorMessage;
    private final ValidationSeverity severity;
    private final boolean isOverridable;
    private final String overrideGroup;
    private final PayloadType type;

    public ValidationPayload(
            ErrorMessage errorMessage,
            ValidationSeverity severity,
            boolean isOverridable,
            String overrideGroup,
            PayloadType type
    ) {
        this.errorMessage = errorMessage;
        this.severity = severity;
        this.isOverridable = isOverridable;
        this.overrideGroup = overrideGroup;
        this.type = type;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    public ValidationSeverity getSeverity() {
        return severity;
    }

    public boolean isOverridable() {
        return isOverridable;
    }

    public String getOverrideGroup() {
        return overrideGroup;
    }

    public PayloadType getType() {
        return type;
    }
}
