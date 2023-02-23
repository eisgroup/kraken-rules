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

import kraken.runtime.utils.TemplateParameterRenderer;
import kraken.runtime.model.rule.payload.validation.LengthPayload;
import kraken.tracer.VoidOperation;

/**
 * Operation to be added to trace after length payload evaluation is completed.
 * Describes payload evaluation details and state.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class LengthPayloadEvaluatedOperation implements VoidOperation {

    private final LengthPayload lengthPayload;

    private final int actualLength;
    private final boolean evaluationState;

    public LengthPayloadEvaluatedOperation(LengthPayload lengthPayload,
                                           int actualLength,
                                           boolean evaluationState) {
        this.lengthPayload = lengthPayload;
        this.actualLength = actualLength;
        this.evaluationState = evaluationState;
    }

    @Override
    public String describe() {
        var template = "Evaluated '%s' to %s. Expected length '%s'. Actual length '%s'";

        return String.format(
            template,
            lengthPayload.getType().getTypeName(),
            evaluationState,
            lengthPayload.getLength(),
            actualLength
        );
    }

}
