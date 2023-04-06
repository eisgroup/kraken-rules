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
package kraken.namespace;

import static kraken.namespace.Namespaces.isGlobal;
import static kraken.namespace.Namespaces.toFullName;
import static kraken.namespace.Namespaces.toNamespaceName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

public class NamespacesTest {
    @Test
    public void shouldParseNamespaceFromFull() {
        assertThat(toNamespaceName("foo:bar"), is("foo"));
    }

    @Test
    public void shouldReturnGlobalWhenNoNamespace() {
        assertThat(toNamespaceName("foo"), is(Namespaced.GLOBAL));
    }

    @Test
    public void shouldBeNullSafe() {
        assertThat(toNamespaceName(null), is(Namespaced.GLOBAL));
    }

    @Test
    public void shouldCreateFullName() {
        assertThat(toFullName("foo", "bar"), is("foo:bar"));
    }

    @Test
    public void shouldCheckIfNamespaceIsGlobal() {
        assertThat(isGlobal().test(Namespaced.GLOBAL), is(true));
        assertThat(isGlobal().test("foo"), is(false));
    }

    @Test
    public void shouldReturnEntryPointName() {
        assertThat(Namespaces.toSimpleName("epname"), is("epname"));
        assertThat(Namespaces.toSimpleName("ns:epname"), is("epname"));
    }
}