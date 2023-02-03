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
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@code CommonPathResolver} class.
 *
 * @author Tomas Dapkunas
 * @since 1.1.1
 */
public class CommonPathResolverTest {

    @Test
    public void shouldResolveCommonPAthBetweenDetailsAndRisk() {
        ContextPath source = new ContextPath.ContextPathBuilder()
                .addPathElements(List.of("Policy", "Risk", "Details")).build();
        ContextPath target = new ContextPath.ContextPathBuilder()
                .addPathElements(List.of("Policy", "Risk")).build();

        ContextPath common = CommonPathResolver.getCommonPath(source, target);

        assertThat(common.getPathAsString(), is("Policy.Risk"));
    }

    @Test
    public void shouldResolveCommonPAthBetweenDetailsAndOther() {
        ContextPath source = new ContextPath.ContextPathBuilder()
                .addPathElements(List.of("Policy", "Risk", "Details")).build();
        ContextPath target = new ContextPath.ContextPathBuilder()
                .addPathElements(List.of("Policy", "Risk", "Other")).build();

        ContextPath common = CommonPathResolver.getCommonPath(source, target);

        assertThat(common.getPathAsString(), is("Policy.Risk"));
    }

    @Test
    public void shouldResolveCommonPAthBetweenDetailsAndInsured() {
        ContextPath source = new ContextPath.ContextPathBuilder()
                .addPathElements(List.of("Policy", "Risk", "Details")).build();
        ContextPath target = new ContextPath.ContextPathBuilder()
                .addPathElements(List.of("Policy", "Insured")).build();

        ContextPath common = CommonPathResolver.getCommonPath(source, target);

        assertThat(common.getPathAsString(), is("Policy"));
    }

    @Test
    public void shouldResolveEmptyCommonPathIfNoCommonElementsExist() {
        ContextPath source = new ContextPath.ContextPathBuilder()
                .addPathElements(List.of("Policy", "Risk", "Details")).build();
        ContextPath target = new ContextPath.ContextPathBuilder()
                .addPathElements(List.of("Insured")).build();

        ContextPath common = CommonPathResolver.getCommonPath(source, target);

        assertThat(common.getPathAsString(), is(""));
    }

    @Test
    public void shouldResolveCommonPAthBetweenPathsContainingSameElements() {
        ContextPath source = new ContextPath.ContextPathBuilder()
                .addPathElements(List.of("Policy", "Risk", "Details")).build();
        ContextPath target = new ContextPath.ContextPathBuilder()
                .addPathElements(List.of("Policy", "Details", "Risk")).build();

        ContextPath common = CommonPathResolver.getCommonPath(source, target);

        assertThat(common.getPathAsString(), is("Policy"));
    }

}
