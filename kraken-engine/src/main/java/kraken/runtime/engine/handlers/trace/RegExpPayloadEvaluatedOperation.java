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

import kraken.runtime.model.rule.payload.validation.RegExpPayload;
import kraken.runtime.utils.TemplateParameterRenderer;
import kraken.tracer.VoidOperation;

/**
 * Operation to be added to trace after regular expression payload evaluation is completed.
 * Describes payload evaluation details and state.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class RegExpPayloadEvaluatedOperation implements VoidOperation {

    private final RegExpPayload regExpPayload;
    private final Object fieldValue;
    private final boolean matchResult;

    public RegExpPayloadEvaluatedOperation(RegExpPayload regExpPayload,
                                           Object fieldValue,
                                           boolean matchResult) {
        this.regExpPayload = regExpPayload;
        this.fieldValue = fieldValue;
        this.matchResult = matchResult;
    }

    @Override
    public String describe() {
        var template = "Evaluated '%s' to %s. Value '%s' %s regular expression '%s'.";
        var value = TemplateParameterRenderer.render(fieldValue);

        return String.format(template,
            regExpPayload.getType().getTypeName(),
            matchResult,
            value,
            matchResult ? "matches" : "does not match",
            regExpPayload.getRegExp());
    }

}
