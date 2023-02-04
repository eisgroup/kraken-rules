/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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

import java.util.Collection;

import kraken.runtime.model.rule.payload.validation.SizePayload;
import kraken.tracer.VoidOperation;

/**
 * Operation to be added to trace after size payload evaluation is completed.
 * Describes payload evaluation details and state.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class SizePayloadEvaluatedOperation implements VoidOperation {

    private final SizePayload sizePayload;
    private final Object fieldValue;
    private final boolean evaluationState;

    public SizePayloadEvaluatedOperation(SizePayload payload,
                                         Object fieldValue,
                                         boolean evaluationState) {
        this.sizePayload = payload;
        this.fieldValue = fieldValue;
        this.evaluationState = evaluationState;
    }

    @Override
    public String describe() {
        var template = "Evaluated '%s' to %s. Expected size %s - actual size is %s.";

        return String.format(template,
            sizePayload.getType().getTypeName(),
            evaluationState,
            getExpectedSize(),
            getActualSize());
    }

    private String getExpectedSize() {
        switch (sizePayload.getOrientation()) {
            case MIN:
                return "no less than " + sizePayload.getSize();
            case MAX:
                return "no more than " + sizePayload.getSize();
            case EQUALS:
                return "equal to " + sizePayload.getSize();
            default:
                return "unknown";
        }
    }

    private String getActualSize() {
        return fieldValue instanceof Collection ? String.valueOf(((Collection<?>) fieldValue).size()) : "";
    }

}
