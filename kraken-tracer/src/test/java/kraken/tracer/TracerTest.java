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

import org.junit.Test;

import kraken.tracer.test.utils.AbstractTracerTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@code Tracer} class.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public class TracerTest extends AbstractTracerTest {

    @Test
    public void shouldCompleteOperationAndTraceImmediately() {
        Tracer.doOperation(new TestVoidOperation("Root"));

        TraceResult result = getTestObserver().getResult();

        assertThat(result.getTraceId(), notNullValue());
        assertThat(result.getOperationNode(), voidOperation("Root"));
        assertThat(getTestObserver().getNumberOfInvocations(), is(1));
    }

    @Test
    public void shouldStartRootOperationAndNestSubsequentOperations() {
        Tracer.doOperation(new TestVoidOperation("Root"), () -> {
            Tracer.doOperation(new TestVoidOperation("First"));
            Tracer.doOperation(new TestVoidOperation("Second"),
                () -> Tracer.doOperation(new TestVoidOperation("Third")));

            Tracer.doOperation(new TestVoidOperation("Fourth"));
        });

        TraceResult result = getTestObserver().getResult();

        assertThat(result.getTraceId(), notNullValue());
        assertThat(getTestObserver().getResult().getOperationNode(),
            voidOperation("Root",
                voidOperation("First"),
                voidOperation("Second",
                    voidOperation("Third")
                ),
                voidOperation("Fourth")
            )
        );

        assertThat(getTestObserver().getNumberOfInvocations(), is(1));
    }

    @Test
    public void shouldStartRootOperationAndNestSubsequentOperationsSupplier() {
        var firstResult = "First completed";
        var thirdResult = "Third completed";

        Tracer.doOperation(new TestOperation("First"), () -> {
            Tracer.doOperation(new TestVoidOperation("Second"));
            Tracer.doOperation(new TestOperation("Third"), () -> {
                Tracer.doOperation(new TestVoidOperation("Fourth"));

                return thirdResult;
            });

            Tracer.doOperation(new TestVoidOperation("Fifth"));

            return firstResult;
        });

        TraceResult result = getTestObserver().getResult();

        assertThat(result.getTraceId(), notNullValue());
        assertThat(getTestObserver().getResult().getOperationNode(),
            operation("First", firstResult, firstResult,
                voidOperation("Second"),
                operation("Third", thirdResult, thirdResult,
                    voidOperation("Fourth")
                ),
                voidOperation("Fifth")
            )
        );
        assertThat(getTestObserver().getNumberOfInvocations(), is(1));
    }

    @Test
    public void shouldHandleErrorAndCompleteOpenOperations() {
        try {
            Tracer.doOperation(new TestVoidOperation("First"), () -> {
                Tracer.doOperation(new TestVoidOperation("Second"));
                Tracer.doOperation(new TestVoidOperation("Third"), () -> {
                    doThrow("ThirdException");
                    Tracer.doOperation(new TestVoidOperation("Fourth"));
                });

                Tracer.doOperation(new TestVoidOperation("Fifth"),
                    () -> Tracer.doOperation(new TestVoidOperation("Sixth")));
            });
        } catch (Exception e) {
            // recovers
        }

        assertThat(getTestObserver().getResult().getOperationNode(),
            voidOperation("First",
                voidOperation("Second"),
                voidOperation("Third",
                    voidOperation("ThirdException")
                )
            )
        );

        assertThat(getTestObserver().getNumberOfInvocations(), is(1));
    }

    @Test
    public void shouldHandleMultipleErrorsAndCompleteOpenOperations() {
        try {
            Tracer.doOperation(new TestVoidOperation("First"), () ->
                Tracer.doOperation(new TestVoidOperation("Second"), () -> {

                try {
                    Tracer.doOperation(new TestVoidOperation("Third"), () -> {
                        doThrow("ExceptionThird");

                        Tracer.doOperation(new TestVoidOperation("Fourth"));
                    });
                } catch (Exception e) {
                    // recovers
                }

                doThrow("ExceptionSecond");
            }));
        } catch (Exception e) {
            // recovers
        }

        assertThat(getTestObserver().getResult().getOperationNode(),
            voidOperation("First",
                voidOperation("Second",
                    voidOperation("Third",
                        voidOperation("ExceptionThird")
                    ),
                    voidOperation("ExceptionSecond")
                )
            )
        );

        assertThat(getTestObserver().getNumberOfInvocations(), is(1));
    }

    @Test
    public void shouldStartMultipleTraces() {
        Tracer.doOperation(new TestVoidOperation("First"),
            () -> Tracer.doOperation(new TestVoidOperation("Second")));

        TraceResult firstTrace = getTestObserver().getResult();

        Tracer.doOperation(new TestVoidOperation("First"),
            () -> Tracer.doOperation(new TestVoidOperation("Second")));

        TraceResult secondTrace = getTestObserver().getResult();

        assertThat(firstTrace.getTraceId().equals(secondTrace.getTraceId()), is(false));
        assertThat(getTestObserver().getNumberOfInvocations(), is(2));
    }

    private void doThrow(String message) {
        throw new IllegalArgumentException(message);
    }

}
