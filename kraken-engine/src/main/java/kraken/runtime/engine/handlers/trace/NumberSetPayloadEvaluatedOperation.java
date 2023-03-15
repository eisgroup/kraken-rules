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
package kraken.runtime.engine.handlers.trace;

import kraken.runtime.model.rule.payload.validation.NumberSetPayload;
import kraken.tracer.VoidOperation;

/**
 * @author Mindaugas Ulevicius
 */
public class NumberSetPayloadEvaluatedOperation implements VoidOperation {

    private final NumberSetPayload payload;
    private final Object fieldValue;
    private final boolean evaluationState;

    public NumberSetPayloadEvaluatedOperation(NumberSetPayload payload, Object fieldValue, boolean evaluationState) {
        this.payload = payload;
        this.fieldValue = fieldValue;
        this.evaluationState = evaluationState;
    }

    @Override
    public String describe() {
        return String.format(
            "Evaluated '%s' to %s. %s %s [%s, %s]%s.",
            payload.getType().getTypeName(),
            evaluationState,
            fieldValue,
            evaluationState ? "∈" : "∉",
            payload.getMin() != null ? payload.getMin() : "-∞",
            payload.getMax() != null ? payload.getMax() : "∞",
            payload.getStep() != null ? " with step " + payload.getStep() : ""
        );
    }
}
