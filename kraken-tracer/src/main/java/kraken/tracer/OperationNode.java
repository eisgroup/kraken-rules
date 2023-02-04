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
