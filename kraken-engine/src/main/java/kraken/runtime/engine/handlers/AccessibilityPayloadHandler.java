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
import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.RulePayloadHandler;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.result.AccessibilityPayloadResult;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.Payload;
import kraken.runtime.model.rule.payload.ui.AccessibilityPayload;

/**
 * Payload handler implementation to process {@link AccessibilityPayload}s
 *
 * @author rimas
 * @since 1.0
 */
public class AccessibilityPayloadHandler implements RulePayloadHandler {

    @Override
    public PayloadType handlesPayloadType() {
        return PayloadType.ACCESSIBILITY;
    }

    @Override
    public PayloadResult executePayload(Payload payload, RuntimeRule rule, DataContext dataContext, EvaluationSession session) {
        AccessibilityPayload ap = (AccessibilityPayload) payload;
        return new AccessibilityPayloadResult(ap.getAccessible());
    }
}
