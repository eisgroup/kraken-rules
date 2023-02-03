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

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsEmptyCollection.empty;

/**
 * @author mulevicius
 * @since 1.0.30
 */
public class SetFunctionsTest {

    @Test
    public void shouldCheckUnion() {
        assertThat(SetFunctions.union(null, null), empty());
        assertThat(SetFunctions.union(null, list()), empty());
        assertThat(SetFunctions.union(list(), list()), empty());
        assertThat(SetFunctions.union(list("a"), null), containsInAnyOrder("a"));
        assertThat(SetFunctions.union(list("a"), list("b")), containsInAnyOrder("a", "b"));
        assertThat(SetFunctions.union(list("a"), list("a", "a")), containsInAnyOrder("a"));
        assertThat(SetFunctions.union(list("a", "a"), list("a")), containsInAnyOrder("a"));
        assertThat(SetFunctions.union(list("a", "b", "c"), list("b", "c", "d")), containsInAnyOrder("a", "b", "c", "d"));
        assertThat(SetFunctions.union(list("a", null), list("b", null)), containsInAnyOrder("a", "b", null));
        assertThat(SetFunctions.union(list("a", null), list("b")), containsInAnyOrder("a", null, "b"));
    }

    @Test
    public void shouldCheckIntersection() {
        assertThat(SetFunctions.intersection(null, null), empty());
        assertThat(SetFunctions.intersection(null, list()), empty());
        assertThat(SetFunctions.intersection(list(), list()), empty());
        assertThat(SetFunctions.intersection(list("a"), null), empty());
        assertThat(SetFunctions.intersection(list("a"), list("b")), empty());
        assertThat(SetFunctions.intersection(list("a"), list("a", "a")), containsInAnyOrder("a"));
        assertThat(SetFunctions.intersection(list("a", "a"), list("a")), containsInAnyOrder("a"));
        assertThat(SetFunctions.intersection(list("a", "b", "c"), list("b", "c", "d")), containsInAnyOrder("b", "c"));
        assertThat(SetFunctions.intersection(list("a", null), list("b", null)), containsInAnyOrder((String)null));
        assertThat(SetFunctions.intersection(list("a", null), list("b")), empty());
    }

    @Test
    public void shouldCheckDifference() {
        assertThat(SetFunctions.difference(null, null), empty());
        assertThat(SetFunctions.difference(null, list()), empty());
        assertThat(SetFunctions.difference(list(), list()), empty());
        assertThat(SetFunctions.difference(list("a"), null), containsInAnyOrder("a"));
        assertThat(SetFunctions.difference(list("a"), list("b")), containsInAnyOrder("a"));
        assertThat(SetFunctions.difference(list("a"), list("a")), empty());
        assertThat(SetFunctions.difference(list("a"), list("a", "a")), empty());
        assertThat(SetFunctions.difference(list("a", "a"), list("a")), empty());
        assertThat(SetFunctions.difference(list("a", "b", "c"), list("b", "c", "d")), containsInAnyOrder("a"));
        assertThat(SetFunctions.difference(list("a", null), list("b", null)), containsInAnyOrder("a"));
        assertThat(SetFunctions.difference(list("a", null), list("b")), containsInAnyOrder("a", null));
    }

    @Test
    public void shouldCheckSymmetricDifference() {
        assertThat(SetFunctions.symmetricDifference(null, null), empty());
        assertThat(SetFunctions.symmetricDifference(null, list()), empty());
        assertThat(SetFunctions.symmetricDifference(list(), list()), empty());
        assertThat(SetFunctions.symmetricDifference(list("a"), null), containsInAnyOrder("a"));
        assertThat(SetFunctions.symmetricDifference(list("a"), list("b")), containsInAnyOrder("a", "b"));
        assertThat(SetFunctions.symmetricDifference(list("a"), list("a")), empty());
        assertThat(SetFunctions.symmetricDifference(list("a"), list("a", "a")), empty());
        assertThat(SetFunctions.symmetricDifference(list("a", "a"), list("a")), empty());
        assertThat(SetFunctions.symmetricDifference(list("a", "b", "c"), list("b", "c", "d")), containsInAnyOrder("a", "d"));
        assertThat(SetFunctions.symmetricDifference(list("a", null), list("b", null)), containsInAnyOrder("a", "b"));
        assertThat(SetFunctions.symmetricDifference(list("a", null), list("b")), containsInAnyOrder("a", null, "b"));
    }

    @Test
    public void shouldCheckDistinct() {
        assertThat(SetFunctions.distinct(null), empty());
        assertThat(SetFunctions.distinct(list()), empty());
        assertThat(SetFunctions.distinct(list("a", "a", "b")), containsInAnyOrder("a", "b"));
        assertThat(SetFunctions.distinct(list("a", "a", "b", null, null)), containsInAnyOrder("a", "b", null));
    }

    private <T> Collection<T> list(T... ts) {
        Collection<T> collection = new ArrayList<>();
        for(T t : ts) {
            collection.add(t);
        }
        return collection;
    }
}
