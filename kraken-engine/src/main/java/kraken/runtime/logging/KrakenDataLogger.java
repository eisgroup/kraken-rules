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
package kraken.runtime.logging;

import java.util.Map;

import kraken.runtime.RuleEngine;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.dto.bundle.EntryPointBundle;

/**
 * SPI to facilitate logging of input and results of {@link RuleEngine}. Provides three methods to log
 * input data and parameters, resolved entry point bundle and entry point evaluation results.<br/>
 * All three invocations will be called sequentially at appropriate time during evaluation by rule engine.
 * Related log entries can be tracked by tracing down {{sessionToken}}, which will be shared from log entries
 * from same validation call.
 * <br/></br/>
 *
 * <b>Note:</b> as whole entry point bundle can be logged out containing full rule definitions, this
 * be used to expose rule definitions, and should be treated with caution
 *
 * @author rimas
 * @since 1.0.6
 */
public interface KrakenDataLogger {

    /**
     * Log input data, passed to rule engine.
     *
     * @param sessionToken
     * @param inputData
     * @param entryPointName
     * @param context
     */
    void logEvaluationInputData(String sessionToken, Object inputData, String entryPointName, Map<String, Object> context);

    void logEvaluationSubtreeInputData(String sessionToken, Object inputData, Object node, String entryPointName, Map<String, Object> context);

    void logEffectiveRules(String sessionToken, EntryPointBundle entryPointBundle);

    void logEvaluationResults(String sessionToken, String entryPointName, EntryPointResult result);
}
