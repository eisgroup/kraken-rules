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

import kraken.runtime.model.rule.payload.derive.DefaultValuePayload;
import kraken.runtime.utils.TemplateParameterRenderer;
import kraken.tracer.VoidOperation;

/**
 * Operation to be added to trace after default value payload evaluation is completed.
 * Describes payload evaluation details.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class DefaultValuePayloadEvaluatedOperation implements VoidOperation {

    private final DefaultValuePayload defaultValuePayload;

    private final Object originalValue;
    private final Object updatedValue;

    public DefaultValuePayloadEvaluatedOperation(DefaultValuePayload defaultValuePayload,
                                                 Object originalValue,
                                                 Object updatedValue) {
        this.defaultValuePayload = defaultValuePayload;
        this.originalValue = originalValue;
        this.updatedValue = updatedValue;
    }

    @Override
    public String describe() {
        var template = "Evaluated '%s'. Before value: '%s'. After value: '%s'.";
        var before = TemplateParameterRenderer.render(originalValue);
        var after = TemplateParameterRenderer.render(updatedValue);

        return String.format(template, defaultValuePayload.getType().getTypeName(), before, after);
    }

}
