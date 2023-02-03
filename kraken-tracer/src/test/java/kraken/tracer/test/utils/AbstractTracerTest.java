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
package kraken.tracer.test.utils;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;

import kraken.tracer.Operation;
import kraken.tracer.OperationNode;
import kraken.tracer.VoidOperation;

public abstract class AbstractTracerTest {

    @Before
    public void beforeTest() {
        TestingTraceObserver.getInstance().clear();
    }

    protected TestingTraceObserver getTestObserver() {
        return TestingTraceObserver.getInstance();
    }

    protected static class TestVoidOperation implements VoidOperation {

        private final String description;

        public TestVoidOperation(String description) {
            this.description = description;
        }

        @Override
        public String describe() {
            return description;
        }
    }

    protected static class TestOperation implements Operation<String> {

        private final String description;

        public TestOperation(String description) {
            this.description = description;
        }

        @Override
        public String describe() {
            return description;
        }

        @Override
        public String describeAfter(String result) {
            return result;
        }

    }

    /**
     * @implNote number of child matchers must always be equal to expected child count. It is also
     * expected that matchers will have the same order as expected child nodes.
     */
    @SafeVarargs
    protected final Matcher<OperationNode> operation(String description,
                                                     String descriptionAfter,
                                                     Object result,
                                                     Matcher<OperationNode>... childMatchers) {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(OperationNode node) {
                boolean isMatching = !VoidOperation.class.isAssignableFrom(node.getOperation().getClass())
                    && node.getOperation().describe().contains(description)
                    && node.getOperationResult().isPresent()
                    && node.getOperation().describeAfter(result).contains(descriptionAfter)
                    && childMatchers.length == node.getChildOperations().size();

                return doMatch(node, childMatchers, isMatching);
            }

            @Override
            public void describeTo(Description describe) {
                describe.appendText("Operation with description "
                    + "before='" + description + "', "
                    + "after='" + descriptionAfter + "' "
                    + "and child count " + childMatchers.length);
            }
        };
    }

    /**
     * @implNote number of child matchers must always be equal to expected child count. It is also
     * expected that matchers will have the same order as expected child nodes.
     */
    @SafeVarargs
    protected final Matcher<OperationNode> operation(Class clazz,
                                                     Matcher<OperationNode>... childMatchers) {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(OperationNode node) {
                boolean isMatching = node.getOperation().getClass().isAssignableFrom(clazz)
                    && !VoidOperation.class.isAssignableFrom(clazz)
                    && node.getOperationResult().isPresent()
                    && childMatchers.length == node.getChildOperations().size();

                return doMatch(node, childMatchers, isMatching);
            }

            @Override
            public void describeTo(Description describe) {
                describe.appendText("Operation: " + clazz.getSimpleName() + " with child count " + childMatchers.length);
            }
        };
    }

    /**
     * @implNote number of child matchers must always be equal to expected child count. It is also
     * expected that matchers will have the same order as expected child nodes.
     */
    @SafeVarargs
    protected final Matcher<OperationNode> voidOperation(String description,
                                                         Matcher<OperationNode>... childMatchers) {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(OperationNode node) {
                boolean isMatching = VoidOperation.class.isAssignableFrom(node.getOperation().getClass())
                    && node.getOperation().describe().contains(description)
                    && childMatchers.length == node.getChildOperations().size();

                return doMatch(node, childMatchers, isMatching);
            }

            @Override
            public void describeTo(Description describe) {
                describe.appendText("Operation with description before='" + description + "' "
                    + "and with child count " + childMatchers.length);
            }
        };
    }

    /**
     * @implNote number of child matchers must always be equal to expected child count. It is also
     * expected that matchers will have the same order as expected child nodes.
     */
    @SafeVarargs
    protected final Matcher<OperationNode> voidOperation(Class clazz, Matcher<OperationNode>... childMatchers) {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(OperationNode node) {
                boolean isMatching = node.getOperation().getClass().isAssignableFrom(clazz)
                    && VoidOperation.class.isAssignableFrom(clazz)
                    && childMatchers.length == node.getChildOperations().size();

                return doMatch(node, childMatchers, isMatching);
            }

            @Override
            public void describeTo(Description describe) {
                describe.appendText("Operation: " + clazz.getSimpleName() + " with child count " + childMatchers.length);
            }
        };
    }

    private boolean doMatch(OperationNode node, Matcher<OperationNode>[] childMatchers, boolean isMatching) {
        if (isMatching) {
            for (int i = childMatchers.length - 1; i >= 0; i--) {
                var matcher = childMatchers[i];
                var childNode = node.getChildOperations().get(i);

                assertThat(childNode, matcher);
            }
        }

        return isMatching;
    }

}
