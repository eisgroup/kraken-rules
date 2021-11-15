/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.engine.handlers;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import kraken.model.payload.PayloadType;
import kraken.model.validation.SizeOrientation;
import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.RulePayloadHandler;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.engine.result.SizePayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.Payload;
import kraken.runtime.model.rule.payload.validation.SizePayload;

import static kraken.runtime.utils.TargetPathUtils.resolveTargetPath;

/**
 * @author psurinin
 */
public class SizePayloadHandler implements RulePayloadHandler {

    private KrakenExpressionEvaluator evaluator;

    public SizePayloadHandler(KrakenExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public PayloadType handlesPayloadType() {
        return PayloadType.SIZE;
    }

    @Override
    public PayloadResult executePayload(Payload payload, RuntimeRule rule, DataContext dataContext, EvaluationSession session) {
        final SizePayload sizePayload = (SizePayload) payload;
        Object value = evaluator.evaluateGetProperty(resolveTargetPath(rule, dataContext), dataContext.getDataObject());
        List<String> templateVariables = evaluator.evaluateTemplateVariables(sizePayload.getErrorMessage(), dataContext, session);
        Function<Boolean, SizePayloadResult> result = success -> new SizePayloadResult(success, sizePayload, templateVariables);
        if (value == null) {
            value = Collections.emptyList();
        }
        if (value instanceof Collection) {
            final Collection collection = (Collection) value;
            final SizeOrientation orientation = sizePayload.getOrientation();
            final int size = sizePayload.getSize();
            switch (orientation) {
                case MIN:
                    return result.apply(collection.size() >= size);
                case MAX:
                    return result.apply(collection.size() <= size);
                case EQUALS:
                    return result.apply(collection.size() == size);
            }
        }
        return result.apply(true);
    }
}
