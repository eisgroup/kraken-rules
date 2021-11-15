/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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

import { DataContext } from "../contexts/data/DataContext";
import { PayloadResult } from "../results/PayloadResult";
import { ExecutionSession } from "../ExecutionSession";
import { Payloads, Rule } from "kraken-model";

/**
 * Handles rule payload logic defined in specific {@link Payloads.Payload} type
 */
export interface RulePayloadHandler {
    /**
     * Returns class for payload type, supported by handler implementation.
     * Used to register payload handler in rule payload processor
     *
     * @return  supported class type, must extend {@link Payloads.Payload}
     */
    handlesPayloadType(): Payloads.PayloadType;
    /**
     * Executes rule logic specified in payload instance on provided data context instance
     * and produced payload result instance for this particular execution.
     */
    executePayload(
        payload: Payloads.Payload,
        rule: Rule,
        dataContext: DataContext,
        session?: ExecutionSession
    ): PayloadResult;
}
