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

import kraken.runtime.model.rule.payload.validation.UsagePayload;
import kraken.runtime.utils.TemplateParameterRenderer;
import kraken.tracer.VoidOperation;

/**
 * Operation to be added to trace after usage payload evaluation is completed.
 * Describes payload evaluation details, field and state.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class UsagePayloadEvaluatedOperation implements VoidOperation {

    private final UsagePayload usagePayload;

    private final Object fieldValue;
    private final boolean evaluationState;

    public UsagePayloadEvaluatedOperation(UsagePayload usagePayload,
                                          Object fieldValue,
                                          boolean evaluationState) {
        this.usagePayload = usagePayload;
        this.fieldValue = fieldValue;
        this.evaluationState = evaluationState;
    }

    @Override
    public String describe() {
        var value = TemplateParameterRenderer.render(fieldValue);
        var template = "Evaluated '%s' to %s. %s";

        switch (usagePayload.getUsageType()) {
            case mandatory:
                var successTemplate = "Field is mandatory and it has value '%s'.";
                var failureTemplate = "Field is mandatory but it has no value.";

                return String.format(template,
                    usagePayload.getType().getTypeName(),
                    evaluationState,
                    evaluationState ? String.format(successTemplate, value) : failureTemplate);
            case mustBeEmpty:
                var successTemplateEmpty = "Field must be empty and it has no value.";
                var failureTemplateEmpty = "Field must be empty but it has value '%s'.";

                return String.format(template,
                    usagePayload.getType().getTypeName(),
                    evaluationState,
                    evaluationState ? successTemplateEmpty : String.format(failureTemplateEmpty, value));
            default:
                return String.format(template, usagePayload.getType().getTypeName(), evaluationState, "");
        }
    }
}
