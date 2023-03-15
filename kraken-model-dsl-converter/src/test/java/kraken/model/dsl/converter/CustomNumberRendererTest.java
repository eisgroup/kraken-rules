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
package kraken.model.dsl.converter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

/**
 * @author mulevicius
 */
public class CustomNumberRendererTest {

    private CustomNumberRenderer renderer;

    @Before
    public void setUp() throws Exception {
        this.renderer = new CustomNumberRenderer();
    }

    @Test
    public void shouldRenderPositiveIntegerAsNumber() {
        String renderedNumber = renderer.toString(10, "minMaxOrInteger", Locale.US);
        assertThat(renderedNumber, equalTo("10"));
    }

    @Test
    public void shouldRenderNegativeIntegerAsNumber() {
        String renderedNumber = renderer.toString(-10, "minMaxOrInteger", Locale.US);
        assertThat(renderedNumber, equalTo("-10"));
    }

    @Test
    public void shouldRenderMaxIntegerAsMax() {
        String renderedNumber = renderer.toString(Integer.MAX_VALUE, "minMaxOrInteger", Locale.US);
        assertThat(renderedNumber, equalTo("MAX"));
    }

    @Test
    public void shouldRenderMinIntegerAsMin() {
        String renderedNumber = renderer.toString(Integer.MIN_VALUE, "minMaxOrInteger", Locale.US);
        assertThat(renderedNumber, equalTo("MIN"));
    }

    @Test
    public void shouldRoundDecimalIfDoesNotFitInDecimal64() {
        String renderedNumber = renderer.toString(new BigDecimal("1234567890.1234567"), null, Locale.US);
        assertThat(renderedNumber, equalTo("1234567890.123457"));
    }
}
