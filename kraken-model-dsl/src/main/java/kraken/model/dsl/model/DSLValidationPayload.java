/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
package kraken.model.dsl.model;

import java.util.Objects;

/**
 * @author mulevicius
 */
public class DSLValidationPayload extends DSLPayload {

    private String code;

    private String message;

    private DSLSeverity severity;

    private boolean isOverridable;

    private String overrideGroup;

    public DSLValidationPayload(String code, String message, DSLSeverity severity, boolean isOverridable, String overrideGroup) {
        this.code = code;
        this.message = message;
        this.severity = Objects.requireNonNull(severity);
        this.isOverridable = isOverridable;
        this.overrideGroup = overrideGroup;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public DSLSeverity getSeverity() {
        return severity;
    }

    public boolean isOverridable() {
        return isOverridable;
    }

    public String getOverrideGroup() {
        return overrideGroup;
    }
}
