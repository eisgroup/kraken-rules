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
package kraken.runtime.engine.result;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nullable;

import kraken.annotations.API;
import kraken.runtime.model.rule.payload.validation.NumberSetPayload;

/**
 * @author Mindaugas Ulevicius
 */
@API
public class NumberSetPayloadResult extends ValidationPayloadResult {

    private final BigDecimal min;
    private final BigDecimal max;
    private final BigDecimal step;

    public NumberSetPayloadResult(Boolean success,
                                  NumberSetPayload payload,
                                  List<Object> templateVariables) {
        super(success, payload, templateVariables);

        this.min = payload.getMin();
        this.max = payload.getMax();
        this.step = payload.getStep();
    }

    @Nullable
    public BigDecimal getMin() {
        return min;
    }

    @Nullable
    public BigDecimal getMax() {
        return max;
    }

    @Nullable
    public BigDecimal getStep() {
        return step;
    }
}
