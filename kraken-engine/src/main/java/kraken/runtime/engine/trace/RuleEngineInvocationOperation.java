/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime.engine.trace;

import com.google.gson.Gson;

import kraken.model.dimensions.DimensionSetResolverHolder;
import kraken.runtime.EvaluationConfig;
import kraken.runtime.engine.EntryPointResult;
import kraken.tracer.Operation;
import kraken.utils.GsonUtils;

/**
 * Operation to be added to trace to wrap entry point evaluation logic.
 * Describes input given to evaluate entry point and evaluation result.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class RuleEngineInvocationOperation implements Operation<EntryPointResult> {

    private final Gson gson = GsonUtils.prettyGson();

    private final String entryPointName;

    private final Object rootNode;
    private final Object node;
    private final EvaluationConfig evaluationConfig;

    public RuleEngineInvocationOperation(String entryPointName,
                                         Object rootNode,
                                         EvaluationConfig evaluationConfig) {
        this.entryPointName = entryPointName;
        this.rootNode = rootNode;
        this.node = null;
        this.evaluationConfig = evaluationConfig;
    }

    public RuleEngineInvocationOperation(String entryPointName,
                                         Object rootNode,
                                         Object node,
                                         EvaluationConfig evaluationConfig) {
        this.entryPointName = entryPointName;
        this.rootNode = rootNode;
        this.node = node;
        this.evaluationConfig = evaluationConfig;
    }

    @Override
    public String describe() {
        var template = "Rule engine called to evaluate entry point '%s'. DimensionSetResolver: %s. Data and configuration: %s";
        return String.format(
            template,
            entryPointName,
            DimensionSetResolverHolder.getInstance().getClass().getName(),
            formatParameters()
        );
    }

    @Override
    public String describeAfter(EntryPointResult result) {
        var template = "Rule engine call completed. Entry point evaluation timestamp '%s'";

        return String.format(template, result.getEvaluationTimeStamp());
    }

    private String formatParameters() {
        var builder = new StringBuilder()
            .append(System.lineSeparator())
            .append("Root data: ")
            .append(gson.toJson(rootNode));

        if (node != null) {
            builder
                .append(System.lineSeparator())
                .append("Node data: ")
                .append(gson.toJson(node));
        }

        return builder
            .append(System.lineSeparator())
            .append("Evaluation configuration: ")
            .append(gson.toJson(evaluationConfig))
            .toString();
    }

}
