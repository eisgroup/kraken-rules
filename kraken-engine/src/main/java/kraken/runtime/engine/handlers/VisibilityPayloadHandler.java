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
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.engine.result.VisibilityPayloadResult;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.ui.VisibilityPayload;

/**
 * Payload handler implementation to process {@link VisibilityPayload}s
 *
 * @author rimas
 * @since 1.0
 */
public class VisibilityPayloadHandler implements RulePayloadHandler {

    @Override
    public PayloadType handlesPayloadType() {
        return PayloadType.VISIBILITY;
    }

    @Override
    public PayloadResult executePayload(RuntimeRule rule, DataContext dataContext, EvaluationSession session) {
        VisibilityPayload vp = (VisibilityPayload) rule.getPayload();
        return new VisibilityPayloadResult(vp.getVisible());
    }

    @Override
    public String describePayloadResult(PayloadResult payloadResult) {
        return ((VisibilityPayloadResult) payloadResult).getVisible()
            ? "The field is not set to be hidden."
            : "The field is set to be hidden.";
    }

}
