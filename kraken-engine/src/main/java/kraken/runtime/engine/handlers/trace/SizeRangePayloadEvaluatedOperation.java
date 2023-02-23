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

import kraken.runtime.model.rule.payload.validation.SizeRangePayload;
import kraken.tracer.VoidOperation;

/**
 * Operation to be added to trace after size range payload evaluation is completed.
 * Describes payload evaluation details, field and state.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class SizeRangePayloadEvaluatedOperation implements VoidOperation {

    private final SizeRangePayload sizeRangePayload;
    private final Object fieldValue;
    private final boolean evaluationState;

    public SizeRangePayloadEvaluatedOperation(SizeRangePayload sizeRangePayload,
                                              Object fieldValue,
                                              boolean evaluationState) {
        this.sizeRangePayload = sizeRangePayload;
        this.fieldValue = fieldValue;
        this.evaluationState = evaluationState;
    }

    @Override
    public String describe() {
        var template = "Evaluated '%s' to %s. %s";
        var successTemplate = "Collection field size is within expected range.";
        var failureTemplate = "Expected size within %s and %s. Actual size is %s.";

        return String.format(template,
            sizeRangePayload.getType().getTypeName(),
            evaluationState,
            evaluationState
                ? successTemplate
                : String.format(failureTemplate, sizeRangePayload.getMin(), sizeRangePayload.getMax(), actualSize()));
    }

    private String actualSize() {
        return fieldValue instanceof Collection
            ? String.valueOf(((Collection<?>) fieldValue).size()) : "";
    }

}
