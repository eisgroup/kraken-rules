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

import kraken.annotations.API;
import kraken.model.Payload;
import kraken.model.ErrorMessage;

@API
public interface ValidationPayload extends Payload {
    void setErrorMessage(ErrorMessage errorMessage);

    ErrorMessage getErrorMessage();

    void setSeverity(ValidationSeverity severity);

    ValidationSeverity getSeverity();

    /**
     * Override flag
     */
    boolean isOverridable();

    void setOverridable(boolean isOverridable);

    String getOverrideGroup();

    void setOverrideGroup(String overrideGroup);
}
