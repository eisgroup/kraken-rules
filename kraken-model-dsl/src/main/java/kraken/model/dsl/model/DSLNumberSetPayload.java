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
package kraken.model.dsl.model;

import java.math.BigDecimal;

/**
 * @author Mindaugas Ulevicius
 */
public class DSLNumberSetPayload extends DSLValidationPayload {

    private final BigDecimal min;
    private final BigDecimal max;
    private final BigDecimal step;

    public DSLNumberSetPayload(BigDecimal min, BigDecimal max, BigDecimal step,
                               String code, String message, DSLSeverity severity,
                               boolean isOverridable, String overrideGroup) {
        super(code, message, severity, isOverridable, overrideGroup);

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
