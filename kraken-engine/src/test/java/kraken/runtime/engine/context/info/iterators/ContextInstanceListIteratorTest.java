/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.engine.context.info.iterators;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ContextInstanceListIteratorTest {

    List<String> values = Arrays.asList("one", "two", "three");

    @Test
    public void testIterator() {
        ContextInstanceListIterator<String> iter = new ContextInstanceListIterator<>(values);

        assertThat(iter.hasNext(), is(true));
        assertThat(iter.next(), is(equalTo("one")));
        assertThat(iter.key(), is(equalTo(0)));

        assertThat(iter.hasNext(), is(true));
        assertThat(iter.next(), is(equalTo("two")));
        assertThat(iter.key(), is(equalTo(1)));

        assertThat(iter.hasNext(), is(true));
        assertThat(iter.next(), is(equalTo("three")));
        assertThat(iter.key(), is(equalTo(2)));

        assertThat(iter.hasNext(), is(false));

    }
}