/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.tracer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A child aware wrapper for {@code Operation}. Hold reference to original operation and
 * allows to retrieve its child operations.
 *
 * @author Tomas Dapkunas
 * @see TraceResult
 * @since 1.33.0
 */
public final class OperationNode {

    private final Operation operation;
    private final List<OperationNode> childOperations;
    private Object operationResult;

    public OperationNode(Operation operation) {
        this.operation = operation;
        this.childOperations = new ArrayList<>();
    }

    void addChild(OperationNode operationNode) {
        childOperations.add(operationNode);
    }

    void setOperationResult(Object operationResult) {
        if (this.operationResult != null) {
            throw new IllegalStateException("Programmer error. Operation result is already set.");
        }

        this.operationResult = operationResult;
    }

    /**
     * Returns a wrapped original operation.
     */
    public Operation getOperation() {
        return operation;
    }

    public Optional<Object> getOperationResult() {
        return Optional.ofNullable(operationResult);
    }

    /**
     * Returns all child operation nodes.
     */
    public List<OperationNode> getChildOperations() {
        return childOperations;
    }

    @Override
    public String toString() {
        return "OperationNode{" +
            "before=" + operation.describe() +
            getOperationResult().map(r -> ", after=" + operation.describeAfter(r)).orElse("") +
            ", childOperations=" + childOperations +
            '}';
    }

}
