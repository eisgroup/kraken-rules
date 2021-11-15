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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.dto.bundle.EntryPointBundle;
import kraken.utils.GsonUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link KrakenDataLogger} implementation which logs rule evaluation data using
 * SLF4J logging framework. TRACE logging level should be enabled to log data.
 *
 * logback config example:
 *
 *  log everything:
 *
 *  <logger name="kraken.runtime.logging.Slf4jKrakenDataLogger.ResultLogger" level="trace">
 *    <appender-ref ref="STDOUT" />
 *  </logger>
 *
 *  log only input:
 *
 *  <logger name="kraken.runtime.logging.Slf4jKrakenDataLogger.ResultLogger.InputLogger" level="trace">
 *
 *  log only rules:
 *
 *   <logger name="kraken.runtime.logging.Slf4jKrakenDataLogger.ResultLogger.RulesLogger" level="trace">
 *
 *   log only output:
 *
 *   <logger name="kraken.runtime.logging.Slf4jKrakenDataLogger.ResultLogger.OutputLogger" level="trace">
 *
 * @author rimas
 * @since 1.0.6
 */
public class Slf4jKrakenDataLogger implements KrakenDataLogger {

    /**
     * Empty class for separate input data logging category
     */
    public class InputLogger {
    }

    /**
     * Empty class for separate rule data logging category
     */
    public class RulesLogger {
    }

    /**
     * Empty class for separate result data logging category
     */
    public class ResultLogger {
    }

    private final Gson gson = GsonUtils.prettyGson();

    private static final String CLASS_NAME = Slf4jKrakenDataLogger.class.getName();

    private static final Logger INPUT_LOGGER = LoggerFactory.getLogger(CLASS_NAME + "." + InputLogger.class.getSimpleName());
    private static final Logger RULE_LOGGER = LoggerFactory.getLogger(CLASS_NAME + "." + RulesLogger.class.getSimpleName());
    private static final Logger RESULT_LOGGER = LoggerFactory.getLogger(CLASS_NAME + "." + ResultLogger.class.getSimpleName());

    @Override
    public void logEvaluationInputData(String sessionToken,
                                              Object inputData,
                                              String entryPointName,
                                              Map<String, Object> context) {
        if (INPUT_LOGGER.isTraceEnabled()) {
            String dataBody = gson.toJson(inputData);
            String contextBody = gson.toJson(context);
            INPUT_LOGGER.trace("{} - input for {}: {}, context: {}", sessionToken, entryPointName, dataBody, contextBody);
        }
    }

    @Override
    public void logEvaluationSubtreeInputData(String sessionToken,
                                              Object inputData,
                                              Object node,
                                              String entryPointName,
                                              Map<String, Object> context) {
        if (INPUT_LOGGER.isTraceEnabled()) {
            String dataBody = gson.toJson(inputData);
            String nodeBody = gson.toJson(node);
            String contextBody = gson.toJson(context);
            INPUT_LOGGER.trace("{} - input for {}: {} with node: {}, context: {}", sessionToken, entryPointName, dataBody, nodeBody, contextBody);
        }
    }

    @Override
    public void logEffectiveRules(String sessionToken,
                                  EntryPointBundle entryPointBundle) {
        if (RULE_LOGGER.isTraceEnabled()) {
            String bundleBody = gson.toJson(entryPointBundle);
            RULE_LOGGER.trace("{} - rules: {}", sessionToken, bundleBody);
        }
    }

    @Override
    public void logEvaluationResults(String sessionToken,
                                     String entryPointName,
                                     EntryPointResult result) {
        if (RESULT_LOGGER.isTraceEnabled()) {
            String resultBody = gson.toJson(result.getFieldResults());
            RESULT_LOGGER.trace("{} - results for {}: {}", sessionToken, entryPointName, resultBody);
        }
    }

}
