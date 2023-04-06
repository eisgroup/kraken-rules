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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import kraken.tracer.id.DefaultTraceIdProvider;
import kraken.tracer.id.TraceIdProvider;
import kraken.tracer.observer.TraceObserver;

/**
 * The entry point to tracing. Provides functionality for starting and completing trace session,
 * operation and notifying registered trace observers. By default {@code Tracer} is disabled,
 * use {@code TracerToggle} SPI to enabled it when needed.
 *
 * <p>{@code Tracer} is a thread-safe class. Current trace instance after starting  is internally
 * stored in {@code ThreadLocal} variable which means that trace must start and complete on the
 * same thread. Context switching is not supported.
 *
 * <p>The trace is created and started upon the first call to any of provided {@link #doOperation}
 * methods. Any subsequent calls will add operations to current trace until first operation is
 * completed.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public class Tracer {

    private static final ThreadLocal<Trace> CURRENT_TRACE = new ThreadLocal<>();

    private static final List<TraceObserver> OBSERVERS = new CopyOnWriteArrayList<>();

    private static final TraceIdProvider TRACE_ID_PROVIDER;

    private static final Boolean TRACE_ENABLED;

    static {
        TRACE_ID_PROVIDER = ServiceLoader.load(TraceIdProvider.class).stream()
            .map(Provider::get)
            .reduce((ttOne, ttTwo) -> {
                throw new IllegalArgumentException("More than one TraceIdProvider implementation is found in classpath: "
                    + ttOne.getClass().getSimpleName() + " + and + " + ttTwo.getClass().getSimpleName() +
                    ". Only one TraceIdProvider implementation is allowed.");
            })
            .orElse(new DefaultTraceIdProvider());

        TracerConfigurer tracerConfigurer = ServiceLoader.load(TracerConfigurer.class).stream()
            .map(Provider::get)
            .reduce((ttOne, ttTwo) -> {
                throw new IllegalArgumentException("More than one TracerConfigurer implementation is found in classpath: "
                    + ttOne.getClass().getSimpleName() + " + and + " + ttTwo.getClass().getSimpleName() +
                    ". Only one TracerConfigurer implementation is allowed.");
            })
            .orElse(null);

        if (tracerConfigurer != null && tracerConfigurer.isEnabled()) {
            TRACE_ENABLED = true;
            OBSERVERS.addAll(tracerConfigurer.traceObservers());
        } else {
            TRACE_ENABLED = false;
        }
    }

    /**
     * Adds given operation to a currently active trace. If trace is not yet started, then a new
     * trace instance is created - in such a case operation is considered to be a root operation.
     *
     * <p>Operations started using this method are considered to be instantly complete, they cannot
     * have any nested operations.
     *
     * <p>In cases when operation started by this method is also a root operation, trace is also
     * completed instantly. Any further calls to any of {@link #doOperation} methods will initiate
     * a new trace.
     *
     * <p>Usage Example:
     * <blockquote>
     * <pre>{@code
     *     <...>
     *     Tracer.doOperation(new SomeOperation());
     *     <...>
     * }</pre>
     * </blockquote>
     *
     * @param operation Operation to add to trace.
     */
    public static void doOperation(VoidOperation operation) {
        if (!TRACE_ENABLED) {
            return;
        }

        var trace = safeGetOrCreate();

        try {
            trace.addOperation(operation);
        } catch (Exception e) {
            trace.handleError(e);
            throw e;
        } finally {
            safeComplete(trace);
        }
    }

    /**
     * Adds given operation to a currently active trace. If trace is not yet started, then a new
     * trace instance is created - in such a case operation is considered to be a root operation.
     *
     * <p>Operations added using this method may have nested operation(s). Operation wraps around
     * given executable code block - all the operations started within code block will be added
     * as child elements to this operation upon completion. Operation is completed after executable
     * code block is ran.
     *
     * <p>In cases when operation started by this method is also a root operation, trace is completed
     * once this operation completes. Any further calls to any of {@link #doOperation} methods will
     * initiate a new trace.
     *
     * <p>Convenient to wrap around code block which has no {@code return} statement. For code blocks
     * with {@code return} statement use {@link #doOperation(Operation, Supplier)}.
     *
     * <p>Usage Example:
     * <blockquote>
     * <pre>{@code
     *     Tracer.doOperation(new SomeOperation(), () -> {
     *         <...>
     *         // Child operation
     *         Tracer.doOperation(new AnotherOperation());
     *         <...>
     *         // Another child operation
     *         Tracer.doOperation(new YetAnotherOperation(), () -> {
     *             <...>
     *         });
     *     }))
     * }</pre>
     * </blockquote>
     *
     * @param operation  Operation to add to trace.
     * @param executable Executable code block.
     */
    public static void doOperation(VoidOperation operation, Runnable executable) {
        if (!TRACE_ENABLED) {
            executable.run();
            return;
        }

        var trace = safeGetOrCreate();

        try {
            trace.addOperation(operation);
            executable.run();
        } catch (Exception e) {
            trace.handleError(e);
            throw e;
        } finally {
            safeComplete(trace);
        }
    }

    /**
     * Adds given operation to a currently active trace. If trace is not yet started, then a new
     * trace instance is created - in such a case operation is considered to be a root operation.
     *
     * <p>Operations added using this method may have nested operation(s). Operation wraps around
     * given executable code block - all the operations started within code block will be added
     * as child elements to this operation upon completion. Operation is completed after executable
     * code block is ran.
     *
     * <p>In cases when operation started by this method is also a root operation, trace is completed
     * once this operation completes. Any further calls to any of {@link #doOperation} methods will
     * initiate a new trace.
     *
     * <p>Convenient to wrap around code block which has {@code return} statement. For code blocks
     * with no {@code return} statement use {@link #doOperation(VoidOperation, Runnable)}.
     *
     * <p>Usage Example:
     * <blockquote>
     * <pre>{@code
     *     return Tracer.doOperation(new SomeOperation(), () -> {
     *         <...>
     *         // Child operation
     *         Tracer.doOperation(new AnotherOperation());
     *         <...>
     *         // Another child operation
     *         Tracer.doOperation(new YetAnotherOperation(), () -> {
     *             <...>
     *         });
     *
     *         return result;
     *     }))
     * }</pre>
     * </blockquote>
     *
     * @param operation  Operation to add to trace.
     * @param executable Executable code block.
     * @return T value of type returned by executable code block.
     */
    public static <T> T doOperation(Operation<T> operation, Supplier<T> executable) {
        if (!TRACE_ENABLED) {
            return executable.get();
        }

        var trace = safeGetOrCreate();

        try {
            trace.addOperation(operation);
            T returnValue = executable.get();
            trace.setOperationResult(returnValue);

            return returnValue;
        } catch (Exception e) {
            trace.handleError(e);
            throw e;
        } finally {
            safeComplete(trace);
        }
    }

    protected static Trace safeGetOrCreate() {
        Trace trace = CURRENT_TRACE.get();

        if (trace == null) {
            trace = new Trace(TRACE_ID_PROVIDER.nextId(), OBSERVERS.iterator());
            CURRENT_TRACE.set(trace);
        }

        return trace;
    }
    protected static void safeComplete(Trace currentTrace) {
        currentTrace.completeOperation();

        if (currentTrace.isCompleted()) {
            CURRENT_TRACE.remove();
        }
    }

    public static boolean isTracingEnabled() {
        return TRACE_ENABLED;
    }

    /*
     * ------------------------------------------------
     * Trace should never be exposed outside of Tracer.
     * ------------------------------------------------
     */

    protected static class Trace {

        private final Deque<OperationNode> operationNodes;
        private final Iterator<TraceObserver> traceObservers;

        private final String traceId;
        private Throwable processedError;

        public Trace(String traceId, Iterator<TraceObserver> traceObservers) {
            this.traceId = traceId;
            this.traceObservers = traceObservers;
            this.operationNodes = new ArrayDeque<>();
        }

        public void setOperationResult(Object operationResult) {
            var current = operationNodes.peek();

            if (current == null) {
                throw new IllegalStateException(
                    "Programmer error. Trying to set operation result to NULL operation node.");
            }

            current.setOperationResult(operationResult);
        }

        public void addOperation(Operation<?> operation) {
            var current = operationNodes.peek();
            var newOperation = new OperationNode(operation);

            if (current != null) {
                current.addChild(newOperation);
            }

            operationNodes.push(newOperation);
        }

        @SuppressWarnings("ReferenceEquality")
        public void handleError(Throwable e) {
            if (processedError == null || processedError != e) {
                var current = operationNodes.peek();

                if (current != null) {
                    current.addChild(new OperationNode(new ErrorOperation(e)));
                }

                processedError = e;
            }
        }

        public void completeOperation() {
            var currNode = operationNodes.pop();

            if (operationNodes.isEmpty()) {
                completeTrace(currNode);
            }
        }

        public boolean isCompleted() {
            return operationNodes.isEmpty();
        }

        private void completeTrace(OperationNode node) {
            TraceResult result = new TraceResult(traceId, node);
            traceObservers.forEachRemaining(traceObserver -> traceObserver.observe(result));
        }

    }

}
