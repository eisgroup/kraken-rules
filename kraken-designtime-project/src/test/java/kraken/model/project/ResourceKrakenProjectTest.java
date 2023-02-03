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
package kraken.model.project;

import static kraken.model.project.KrakenProjectMocks.contextDefinition;
import static kraken.model.project.KrakenProjectMocks.krakenProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import java.util.List;

import org.junit.Test;

/**
 * @author mulevicius
 */
public class ResourceKrakenProjectTest {

    @Test
    public void shouldCreateContextProjectionWithoutParentDefinitionDuplicates() {
        var root = contextDefinition("Root", List.of(), List.of("BaseRoot", "Base"));
        var baseRoot = contextDefinition("BaseRoot", List.of(), List.of("Base"));
        var base = contextDefinition("Base", List.of(), List.of());

        var krakenProject = krakenProject(List.of(root, baseRoot, base), List.of(), List.of());

        var rootProjection = krakenProject.getContextProjection("Root");
        assertThat(rootProjection.getParentDefinitions(), hasSize(2));
        assertThat(rootProjection.getParentDefinitions(), contains("BaseRoot", "Base"));
    }
}
