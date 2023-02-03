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

/**
 * Provides with results of a single trace.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class TraceResult {

    private final String traceId;
    private final OperationNode operationNode;

    public TraceResult(String traceId, OperationNode operationNode) {
        this.traceId = traceId;
        this.operationNode = operationNode;
    }

    /**
     * A unique identifier of a trace.
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * An operation node which holds reference to first operation
     * of a trace.
     */
    public OperationNode getOperationNode() {
        return operationNode;
    }

}
