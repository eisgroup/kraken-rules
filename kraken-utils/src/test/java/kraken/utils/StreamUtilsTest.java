/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
package kraken.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static kraken.utils.StreamUtils.consume;
import static kraken.utils.StreamUtils.distinctByKey;
import static kraken.utils.StreamUtils.orderedMapCollector;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class StreamUtilsTest {

    @Test
    public void shouldConsume() {
        Collection<ValueHolder> before = Arrays.asList(new ValueHolder("foo"), new ValueHolder("foo"));

        Collection<ValueHolder> result = before.stream()
                .map(consume(r -> r.a = "bar"))
                .collect(Collectors.toList());

        assertThat(result, is(before));
        assertThat(result, containsInAnyOrder(hasProperty("a", is("bar")),
                                              hasProperty("a", is("bar"))));
    }

    @Test
    public void shouldCollectToOrderedMap() {
        Map<String, ValueHolder> result = Stream.of(new ValueHolder("foo"), new ValueHolder("bar"))
                .collect(orderedMapCollector(ValueHolder::getA, Function.identity()));

        Iterator<ValueHolder> iterator = result.values().iterator();

        assertThat(iterator.next().getA(), is("foo"));
        assertThat(iterator.next().getA(), is("bar"));
    }

    @Test
    public void shouldFilterByKey() {
        Collection<String> result = Stream.of(new ValueHolder("foo"),
                                              new ValueHolder("bar"),
                                              new ValueHolder("foo"),
                                              new ValueHolder("foo"))
                .filter(distinctByKey(ValueHolder::getA))
                .map(ValueHolder::getA)
                .collect(Collectors.toList());

        assertThat(result, hasSize(2));
        assertThat(result, containsInAnyOrder("foo", "bar"));
    }

    public static final class ValueHolder {
        private String a;

        private ValueHolder(String a) {
            this.a = a;
        }

        public String getA() {
            return a;
        }
    }
}