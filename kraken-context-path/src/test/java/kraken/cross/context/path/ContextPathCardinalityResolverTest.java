/*
 *  Copyright 2020 EIS Ltd and/or one of its affiliates.
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
package kraken.cross.context.path;

import kraken.context.path.ContextPath;
import kraken.model.context.Cardinality;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@code ContextPathCardinalityResolver} class.
 *
 * @author Tomas Dapkunas
 * @since 1.1.1
 */
public class ContextPathCardinalityResolverTest {

    @Test
    public void shouldResolveToSingleCardinalityIfAllNodesAreSingle() {
        // Parent.Child.GrandChild
        ContextCardinalityResolver cardinalityResolver = mock(ContextCardinalityResolver.class);
        Mockito.when(cardinalityResolver.getCardinality("Parent", "Child")).thenReturn(Cardinality.SINGLE);
        Mockito.when(cardinalityResolver.getCardinality("Child", "GrandChild")).thenReturn(Cardinality.SINGLE);

        Cardinality result = ContextPathCardinalityResolver.create(cardinalityResolver)
                .resolve(createPath("Parent", "Child", "GrandChild"));

        assertThat(result, is(Cardinality.SINGLE));
    }

    @Test
    public void shouldResolveToMultipleCardinalityIfMiddleNodeMultiple() {
        // Parent.Child*.GrandChild
        ContextCardinalityResolver cardinalityResolver = mock(ContextCardinalityResolver.class);
        Mockito.when(cardinalityResolver.getCardinality("Parent", "Child")).thenReturn(Cardinality.MULTIPLE);
        Mockito.when(cardinalityResolver.getCardinality("Child", "GrandChild")).thenReturn(Cardinality.SINGLE);

        Cardinality result = ContextPathCardinalityResolver.create(cardinalityResolver)
                .resolve(createPath("Parent", "Child", "GrandChild"));

        assertThat(result, is(Cardinality.MULTIPLE));
    }

    @Test
    public void shouldResolveToMultipleCardinalityIfLeafNodeMultiple() {
        // Parent.Child.GrandChild*
        ContextCardinalityResolver cardinalityResolver = mock(ContextCardinalityResolver.class);
        Mockito.when(cardinalityResolver.getCardinality("Parent", "Child")).thenReturn(Cardinality.SINGLE);
        Mockito.when(cardinalityResolver.getCardinality("Child", "GrandChild")).thenReturn(Cardinality.MULTIPLE);

        Cardinality result = ContextPathCardinalityResolver.create(cardinalityResolver)
                .resolve(createPath("Parent", "Child", "GrandChild"));

        assertThat(result, is(Cardinality.MULTIPLE));
    }

    @Test
    public void shouldResolveToMultipleCardinalityIfMultipleNodesMultiple() {
        // Parent.Child*.GrandChild*
        ContextCardinalityResolver cardinalityResolver = mock(ContextCardinalityResolver.class);
        Mockito.when(cardinalityResolver.getCardinality("Parent", "Child")).thenReturn(Cardinality.MULTIPLE);
        Mockito.when(cardinalityResolver.getCardinality("Child", "GrandChild")).thenReturn(Cardinality.MULTIPLE);

        Cardinality result = ContextPathCardinalityResolver.create(cardinalityResolver)
                .resolve(createPath("Parent", "Child", "GrandChild"));

        assertThat(result, is(Cardinality.MULTIPLE));
    }

    private ContextPath createPath(String targetNode, String... remaining) {
        ContextPath.ContextPathBuilder contextPath = new ContextPath.ContextPathBuilder(targetNode);
        Arrays.stream(remaining).forEach(s -> contextPath.addPathElement(s));

        return contextPath.build();
    }

}
