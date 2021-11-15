/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.el.accelerated;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * @author mulevicius
 */
public class PropertyExpressionEvaluatorTest {

    private PropertyExpressionEvaluator propertyExpressionEvaluator;

    @Before
    public void setUp() throws Exception {
        this.propertyExpressionEvaluator = new PropertyExpressionEvaluator();
    }

    @Test
    public void shouldEvaluateSimplePropertyInMap() {
        Object result = propertyExpressionEvaluator.evaluate("property", Map.of("property", 10));

        assertThat(result, equalTo(10));
    }

    @Test
    public void shouldEvaluateMissingPropertyInMap() {
        Object result = propertyExpressionEvaluator.evaluate("property", Map.of());

        assertThat(result, nullValue());
    }

    @Test
    public void shouldEvaluateMissingPropertyInObject() {
        Object result = propertyExpressionEvaluator.evaluate("property", new Context());

        assertThat(result, nullValue());
    }

    class Context {
        private String property;

        public String getProperty() {
            return property;
        }
    }
}
