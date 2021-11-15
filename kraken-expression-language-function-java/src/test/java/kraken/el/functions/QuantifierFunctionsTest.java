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
package kraken.el.functions;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author mulevicius
 */
public class QuantifierFunctionsTest {

    @Test
    public void shouldCheckAny() {
        assertThat(QuantifierFunctions.any(null), is(false));
        assertThat(QuantifierFunctions.any(list()), is(false));
        assertThat(QuantifierFunctions.any(list(false, false)), is(false));
        assertThat(QuantifierFunctions.any(list(true, false)), is(true));
        assertThat(QuantifierFunctions.any(list(true, true)), is(true));

        assertThat(QuantifierFunctions.any(list(true, null, null)), is(true));
        assertThat(QuantifierFunctions.any(list(null, null)), is(false));
    }

    @Test
    public void shouldCheckAll() {
        assertThat(QuantifierFunctions.all(null), is(true));
        assertThat(QuantifierFunctions.all(list()), is(true));
        assertThat(QuantifierFunctions.all(list(false, false)), is(false));
        assertThat(QuantifierFunctions.all(list(true, false)), is(false));
        assertThat(QuantifierFunctions.all(list(true, true)), is(true));

        assertThat(QuantifierFunctions.all(list(true, null, null)), is(false));
        assertThat(QuantifierFunctions.all(list(null, null)), is(false));
    }

    private <T> Collection<T> list(T... ts) {
        Collection<T> collection = new ArrayList<>();
        for(T t : ts) {
            collection.add(t);
        }
        return collection;
    }
}
