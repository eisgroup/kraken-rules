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

import kraken.model.payload.PayloadType;
import kraken.model.validation.UsageType;
import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.RulePayloadHandler;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.engine.result.UsagePayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.Payload;
import kraken.runtime.model.rule.payload.validation.UsagePayload;

import static kraken.runtime.utils.TargetPathUtils.resolveTargetPath;

import java.util.List;

/**
 * Payload handler implementation for {@link UsagePayload}s
 *
 * @author rimas
 * @since 1.0
 */
@SuppressWarnings("WeakerAccess")
public class UsagePayloadHandler implements RulePayloadHandler {

    private KrakenExpressionEvaluator evaluator;

    public UsagePayloadHandler(KrakenExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public PayloadResult executePayload(Payload payload, RuntimeRule rule, DataContext dataContext, EvaluationSession session) {
        UsagePayload usagePayload = (UsagePayload) payload;

        String path = resolveTargetPath(rule, dataContext);
        Object value = evaluator.evaluateGetProperty(path, dataContext.getDataObject());
        boolean valid = isMandatoryAndNotNull(value, usagePayload) || isEmptyAndNull(value, usagePayload);
        List<String> templateVariables = evaluator.evaluateTemplateVariables(usagePayload.getErrorMessage(), dataContext, session);
        return new UsagePayloadResult(valid, usagePayload, templateVariables);
    }

    @Override
    public PayloadType handlesPayloadType() {
        return PayloadType.USAGE;
    }

    private boolean isMandatoryAndNotNull(Object value, UsagePayload payload) {
        return UsageType.mandatory.equals(payload.getUsageType())
                && !PayloadHandlerUtils.isEmptyValue(value);
    }

    private boolean isEmptyAndNull(Object value, UsagePayload payload) {
        return UsageType.mustBeEmpty.equals(payload.getUsageType())
                && PayloadHandlerUtils.isEmptyValue(value);
    }
}
