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

import kraken.runtime.model.rule.payload.ui.VisibilityPayload;
import kraken.tracer.VoidOperation;

/**
 * Operation to be added to trace after visibility payload evaluation is completed.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class VisibilityPayloadEvaluatedOperation implements VoidOperation {

    private final VisibilityPayload visibilityPayload;

    public VisibilityPayloadEvaluatedOperation(VisibilityPayload visibilityPayload) {
        this.visibilityPayload = visibilityPayload;
    }

    @Override
    public String describe() {
        var template = "Evaluated '%s'. The field is set to be hidden.";

        return String.format(template, visibilityPayload.getType().getTypeName());
    }

}
