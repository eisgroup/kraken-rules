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
package kraken.el.functionregistry.functions;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author mulevicius
 * @since 1.0.30
 */
public class GenericValueFunctionsTest {

    @Test
    public void shouldCheckIfValueIsEmpty() {
        assertThat(GenericValueFunctions.isEmpty(null), is(true));
        assertThat(GenericValueFunctions.isEmpty(""), is(true));
        assertThat(GenericValueFunctions.isEmpty(Collections.emptyList()), is(true));
        assertThat(GenericValueFunctions.isEmpty(Collections.emptySet()), is(true));
        assertThat(GenericValueFunctions.isEmpty(List.of(1)), is(false));
        assertThat(GenericValueFunctions.isEmpty(0), is(false));
        assertThat(GenericValueFunctions.isEmpty("string"), is(false));
    }
}
