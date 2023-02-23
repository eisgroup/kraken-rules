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
package kraken.runtime.engine.handlers.trace;

import kraken.runtime.model.rule.payload.validation.ValueListPayload;
import kraken.runtime.utils.TemplateParameterRenderer;
import kraken.tracer.VoidOperation;

/**
 * @author Tomas Dapkunas
 * @since 1.43.0
 */
public final class ValueListPayloadEvaluatedOperation implements VoidOperation {

    private final ValueListPayload valueListPayload;
    private final Object fieldValue;
    private final boolean evaluationState;

    public ValueListPayloadEvaluatedOperation(ValueListPayload valueListPayload,
                                              Object fieldValue,
                                              boolean evaluationState) {
        this.valueListPayload = valueListPayload;
        this.fieldValue = fieldValue;
        this.evaluationState = evaluationState;
    }

    @Override
    public String describe() {
        var value = TemplateParameterRenderer.render(fieldValue);
        var template = "Evaluated '%s' to '%s'. Field value '%s'%s.";
        var payloadResultStatus = evaluationState
            ? ""
            : String.format(" is not one of [ %s ]", valueListPayload.getValueList().valuesAsString());

        return String.format(
            template,
            valueListPayload.getType().getTypeName(),
            evaluationState,
            value,
            payloadResultStatus
        );
    }

}
