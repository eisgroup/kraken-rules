/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime.engine.result.reducers.validation.trace;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import kraken.model.validation.ValidationSeverity;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.result.reducers.validation.ValidationResult;
import kraken.runtime.engine.result.reducers.validation.ValidationStatus;
import kraken.tracer.Operation;

/**
 * Operation to be added to trace before entry point result reduction.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class EntryPointResultReducingOperation implements Operation<ValidationStatus> {

    private final EntryPointResult entryPointResult;

    public EntryPointResultReducingOperation(EntryPointResult entryPointResult) {
        this.entryPointResult = entryPointResult;
    }

    @Override
    public String describe() {
        var template = "Reducing results of entry point evaluated at %s.";

        return String.format(template, entryPointResult.getEvaluationTimeStamp().toString());
    }

    @Override
    public String describeAfter(ValidationStatus result) {
        var template = "Reduced entry point results: %s";

        return String.format(template, System.lineSeparator() + describeFieldResults(result));
    }

    private String describeFieldResults(ValidationStatus validationStatus) {
        List<ValidationResult> allResults = new ArrayList<>();
        allResults.addAll(validationStatus.getErrorResults());
        allResults.addAll(validationStatus.getWarningResults());
        allResults.addAll(validationStatus.getInfoResults());

        return allResults.stream()
            .collect(Collectors.groupingBy(validationResult -> validationResult.getContextFieldInfo().toString()))
            .entrySet()
            .stream()
            .map(fieldEntry -> "Field " + fieldEntry.getKey() + " has: "
                + System.lineSeparator() + describeResults(fieldEntry.getValue()))
            .collect(Collectors.joining(System.lineSeparator()));
    }

    private String describeResults(List<ValidationResult> results) {
        return results.stream()
            .map(validationResult -> "Rule '" + validationResult.getRuleName() + "' with severity - "
                + getSeverity(validationResult.getSeverity()))
            .collect(Collectors.joining(System.lineSeparator()));
    }

    private String getSeverity(ValidationSeverity validationSeverity) {
        switch (validationSeverity) {
            case critical:
                return "error";
            case warning:
                return "warning";
            case info:
                return "info";
            default:
                return "";
        }
    }

}
