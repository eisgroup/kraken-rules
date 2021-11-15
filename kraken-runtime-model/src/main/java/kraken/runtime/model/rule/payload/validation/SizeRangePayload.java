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

/**
 * @author psurinin@eisgroup.com
 * @since 1.1.0
 */
public class SizeRangePayload extends ValidationPayload {

    private final int min;
    private final int max;

    public SizeRangePayload(
            ErrorMessage errorMessage,
            ValidationSeverity severity,
            boolean isOverridable,
            String overrideGroup,
            int min,
            int max
    ) {
        super(errorMessage, severity, isOverridable, overrideGroup, PayloadType.SIZE_RANGE);

        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
