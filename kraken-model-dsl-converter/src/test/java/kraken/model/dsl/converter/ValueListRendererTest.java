/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.model.dsl.converter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import kraken.model.ValueList;

/**
 * @author Tomas Dapkunas
 * @since 1.43.0
 */
public class ValueListRendererTest {

    private final ValueListRenderer valueListRenderer = new ValueListRenderer();

    @Test
    public void shouldRenderStringElementCollection() {
        ValueList valueList = ValueList.fromString(List.of("valueOne", "value Two", "value, three", "value's"));

        String result = valueListRenderer.toString(valueList, null, Locale.getDefault());

        assertThat(result, is("\"valueOne\", \"value Two\", \"value, three\", \"value's\""));
    }

    @Test
    public void shouldRenderDecimalElementCollection() {
        ValueList valueList = ValueList.fromNumber(List.of(
            new BigDecimal("10"),
            new BigDecimal("100.00"),
            new BigDecimal("105.05"),
            new BigDecimal("1E+3")
        ));

        String result = valueListRenderer.toString(valueList, null, Locale.getDefault());

        assertThat(result, is("10, 100, 105.05, 1000"));
    }

}
