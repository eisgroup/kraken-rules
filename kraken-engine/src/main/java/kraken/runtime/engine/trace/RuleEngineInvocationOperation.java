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
package kraken.runtime.engine.trace;

import com.google.gson.Gson;

import kraken.model.dimensions.DimensionSetResolverHolder;
import kraken.runtime.EvaluationConfig;
import kraken.runtime.engine.EntryPointResult;
import kraken.tracer.Operation;
import kraken.tracer.Tracer;
import kraken.utils.Dates;
import kraken.utils.GsonUtils;

/**
 * Operation to be added to trace to wrap entry point evaluation logic.
 * Describes input given to evaluate entry point and evaluation result.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class RuleEngineInvocationOperation implements Operation<EntryPointResult> {

    private static final Gson gson = GsonUtils.prettyGson();

    private final String entryPointName;

    private final String rootNodeJson;
    private final String nodeJson;
    private final EvaluationConfig evaluationConfig;

    public RuleEngineInvocationOperation(String entryPointName,
                                         Object rootNode,
                                         EvaluationConfig evaluationConfig) {
        this.entryPointName = entryPointName;
        // performance optimization to avoid creating json when tracing is not enabled in application
        this.rootNodeJson = Tracer.isTracingEnabled() ? gson.toJson(rootNode) : "";
        this.nodeJson = "";
        this.evaluationConfig = evaluationConfig;
    }

    public RuleEngineInvocationOperation(String entryPointName,
                                         Object rootNode,
                                         Object node,
                                         EvaluationConfig evaluationConfig) {
        this.entryPointName = entryPointName;
        // performance optimization to avoid creating json when tracing is not enabled in application
        this.rootNodeJson = Tracer.isTracingEnabled() ? gson.toJson(rootNode) : "";
        this.nodeJson = Tracer.isTracingEnabled() ? gson.toJson(node) : "";
        this.evaluationConfig = evaluationConfig;
    }

    @Override
    public String describe() {
        var dimensionSetResolverName = DimensionSetResolverHolder.getInstance().getClass().getName();
        return String.format("Rule engine called to evaluate entry point '%s'", entryPointName) + System.lineSeparator()
            + "DimensionSetResolver: " + dimensionSetResolverName + System.lineSeparator()
            + formatInputDataAndConfiguration();
    }

    @Override
    public String describeAfter(EntryPointResult result) {
        return "Rule engine call completed. Entry point evaluation timestamp "
            + Dates.convertLocalDateTimeToISO(result.getEvaluationTimeStamp());
    }

    private String formatInputDataAndConfiguration() {
        var builder = new StringBuilder()
            .append("Entity:")
            .append(System.lineSeparator())
            .append(rootNodeJson);

        if (!nodeJson.isEmpty()) {
            builder
                .append(System.lineSeparator())
                .append("Restriction entity node:")
                .append(System.lineSeparator())
                .append(gson.toJson(nodeJson));
        }

        return builder
            .append(System.lineSeparator())
            .append("Configuration:")
            .append(System.lineSeparator())
            .append(gson.toJson(evaluationConfig))
            .toString();
    }

}
