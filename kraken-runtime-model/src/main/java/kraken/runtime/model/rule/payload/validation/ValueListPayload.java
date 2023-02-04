/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.runtime.model.rule.payload.validation;

import kraken.model.ValueList;
import kraken.model.payload.PayloadType;
import kraken.model.validation.ValidationSeverity;

/**
 * A payload that allows to assert that the collection of values defined in
 * {@link ValueList} contains field value. Can be applied on numerical and
 * string primitive data fields as defined in {@link ValueList.DataType}.
 *
 * @author Tomas Dapkunas
 * @since 1.43.0
 */
public final class ValueListPayload extends ValidationPayload {

    private final ValueList valueList;

    public ValueListPayload(ErrorMessage errorMessage,
                            ValidationSeverity severity,
                            boolean isOverridable,
                            String overrideGroup,
                            ValueList valueList) {
        super(errorMessage, severity, isOverridable, overrideGroup, PayloadType.VALUE_LIST);
        this.valueList = valueList;
    }

    public ValueList getValueList() {
        return valueList;
    }

}
