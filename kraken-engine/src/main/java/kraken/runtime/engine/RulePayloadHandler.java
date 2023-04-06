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
package kraken.runtime.engine;

import kraken.model.payload.PayloadType;
import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.Payload;

/**
 * Handles rule payload logic defined in specific {@link Payload} type
 *
 * @author rimas
 * @since 1.0
 */
@SuppressWarnings("WeakerAccess")
public interface RulePayloadHandler {

    /**
     * Returns class for payload type, supported by handler implementation.
     * Used to register payload handler in rule payload processor
     *
     * @return  supported class type, must extent {@link Payload}
     */
    PayloadType handlesPayloadType();

    /**
     * Executes rule logic specified in rule payload on provided data context instance
     * and produced payload result instance for this particular execution.
     *
     * @param rule
     * @param dataContext
     * @return
     */
    PayloadResult executePayload(RuntimeRule rule, DataContext dataContext, EvaluationSession session);

    /**
     *
     * @param payloadResult
     * @return payload evaluation result description that will be included in the logs.
     * Description must be one or more full sentences with proper punctuation.
     */
    String describePayloadResult(PayloadResult payloadResult);
}
