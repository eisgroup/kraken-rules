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
package kraken.tracer.observer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kraken.tracer.OperationNode;
import kraken.tracer.TraceResult;

/**
 * An implementation of {@code TraceObserver} which iterates through trace result
 * and outputs operation description to logs at the <b>trace</b> level.
 *
 * <p>Can be obtained using {@link Slf4jTraceObserver#INSTANCE} variable.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class Slf4jTraceObserver implements TraceObserver {

    public static final String TRACER_NAME = "KRAKEN_TRACER";
    public static final TraceObserver INSTANCE = new Slf4jTraceObserver();

    private static final Logger LOGGER = LoggerFactory.getLogger(TRACER_NAME);

    Slf4jTraceObserver() {
    }

    @Override
    public void observe(TraceResult result) {
        StringBuilder builder = new StringBuilder();

        builder
            .append(System.lineSeparator())
            .append("--- Started Logging Trace Results For Trace ID: ")
            .append(result.getTraceId())
            .append(System.lineSeparator());

        traverseAndAppend(builder, result.getOperationNode(), 0);

        builder
            .append("--- Finished Logging Trace Results for Trace ID: ")
            .append(result.getTraceId());

        LOGGER.trace(builder.toString());
    }

    private void traverseAndAppend(StringBuilder builder, OperationNode operationNode, int depth) {
        String before = operationNode.getOperation().describe();
        String after = operationNode.getOperationResult()
            .map(result -> operationNode.getOperation().describeAfter(result))
            .orElse("");

        String beforeSymbol = after.isEmpty() ? "--" : "->";
        appendDescription(builder, before, depth, beforeSymbol);

        operationNode.getChildOperations()
            .forEach(child -> traverseAndAppend(builder, child, depth + 1));

        String afterSymbol = before.isEmpty() ? "--" : "<-";
        appendDescription(builder, after, depth, afterSymbol);
    }

    private void appendDescription(StringBuilder builder, String description, int depth, String symbol) {
        String indention = spaces((symbol.length() + 1) * depth);
        String symbolReplacement = spaces(symbol.length());

        List<String> lines = description.lines().collect(Collectors.toList());
        for (int i = 0; i < lines.size(); i++) {
            builder.append(indention);
            if(i == 0) {
                builder.append(symbol);
            } else {
                builder.append(symbolReplacement);
            }
            builder.append(" ");
            builder.append(lines.get(i));
            builder.append(System.lineSeparator());
        }
    }

    private String spaces(int length) {
        char[] spaces = new char[length];
        Arrays.fill(spaces, ' ');
        return String.valueOf(spaces);
    }

}
