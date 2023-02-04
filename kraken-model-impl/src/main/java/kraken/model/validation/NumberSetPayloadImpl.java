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
package kraken.model.validation;

import java.math.BigDecimal;

/**
 * @author Mindaugas Ulevicius
 */
public class NumberSetPayloadImpl extends ValidationPayloadImpl implements NumberSetPayload {

    private BigDecimal min;
    private BigDecimal max;
    private BigDecimal step;

    @Override
    public BigDecimal getMin() {
        return min;
    }

    @Override
    public void setMin(BigDecimal min) {
        this.min = min;
    }

    @Override
    public BigDecimal getMax() {
        return max;
    }

    @Override
    public void setMax(BigDecimal max) {
        this.max = max;
    }

    @Override
    public BigDecimal getStep() {
        return step;
    }

    @Override
    public void setStep(BigDecimal step) {
        this.step = step;
    }
}
