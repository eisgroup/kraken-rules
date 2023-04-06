/*
 *  Copyright 2023 EIS Ltd and/or one of its affiliates.
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

import kraken.runtime.engine.RulePayloadHandler;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.model.rule.payload.Payload;
import kraken.tracer.Operation;

/**
 * @author Mindaugas Ulevicius
 */
public class RulePayloadEvaluatedOperation implements Operation<PayloadResult> {

    private final Payload payload;

    private final RulePayloadHandler payloadHandler;

    public RulePayloadEvaluatedOperation(Payload payload, RulePayloadHandler payloadHandler) {
        this.payload = payload;
        this.payloadHandler = payloadHandler;
    }

    @Override
    public String describeAfter(PayloadResult payloadResult) {
        String template = "Evaluated %s. %s";
        return String.format(
            template,
            payload.getType().getTypeName(),
            payloadHandler.describePayloadResult(payloadResult)
        );
    }
}
