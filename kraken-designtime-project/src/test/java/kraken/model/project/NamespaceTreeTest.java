/*
 *  Copyright 2021 EIS Ltd and/or one of its affiliates.
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
package kraken.model.project;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@code NamespaceTree} class.
 *
 * @author Tomas Dapkunas
 * @since 1.9.0
 */
public class NamespaceTreeTest {

    @Test
    public void shouldReturnCorrectNodesFromTree() {
        NamespaceTree namespaceTree = new NamespaceTree(
                createNode("rootNs",
                        createNode("includedNsOne"),
                        createNode("includedNsTwo",
                                createNode("includedNsThree"))));

        assertThat(namespaceTree.getRoot(), is(notNullValue()));
        assertThat(namespaceTree.getRoot().getName(), is("rootNs"));

        assertThat(namespaceTree.getNode("includedNsOne"), is(notNullValue()));

        NamespaceNode includedNsOne = namespaceTree.getNode("includedNsOne");

        assertThat(includedNsOne.getName(), is("includedNsOne"));
        assertThat(includedNsOne.getChildNodes(), hasSize(0));

        assertThat(namespaceTree.getNode("includedNsTwo"), is(notNullValue()));

        NamespaceNode includedNsTwo = namespaceTree.getNode("includedNsTwo");

        assertThat(includedNsTwo.getName(), is("includedNsTwo"));
        assertThat(includedNsTwo.getChildNodes(), hasSize(1));

        assertThat(namespaceTree.getNode("includedNsThree"), is(notNullValue()));

        NamespaceNode includedNsThree = namespaceTree.getNode("includedNsThree");

        assertThat(includedNsThree.getName(), is("includedNsThree"));
        assertThat(includedNsThree.getChildNodes(), hasSize(0));

        assertThat(namespaceTree.getNode("unknownNamespace"), is(nullValue()));
    }

    private NamespaceNode createNode(String name, NamespaceNode... childNodes) {
        return new NamespaceNode(name, Arrays.asList(childNodes));
    }

}
