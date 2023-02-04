/*
 *  Copyright 2023 EIS Ltd and/or one of its affiliates.
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

import java.math.BigDecimal;

import kraken.model.payload.PayloadType;
import kraken.model.validation.ValidationSeverity;

/**
 * @author Mindaugas Ulevicius
 */
public class NumberSetPayload extends ValidationPayload {

    private final BigDecimal min;
    private final BigDecimal max;
    private final BigDecimal step;

    public NumberSetPayload(BigDecimal min, BigDecimal max, BigDecimal step,
                            ErrorMessage errorMessage, ValidationSeverity severity,
                            boolean isOverridable, String overrideGroup) {
        super(errorMessage, severity, isOverridable, overrideGroup, PayloadType.NUMBER_SET);

        this.min = min;
        this.max = max;
        this.step = step;
    }

    public BigDecimal getMin() {
        return min;
    }

    public BigDecimal getMax() {
        return max;
    }

    public BigDecimal getStep() {
        return step;
    }
}
