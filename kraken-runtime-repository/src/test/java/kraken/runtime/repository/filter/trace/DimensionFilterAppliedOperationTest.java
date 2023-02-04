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
package kraken.runtime.repository.filter.trace;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import kraken.runtime.repository.filter.DimensionFilter;

/**
 * Unit tests for {@code DimensionFilterAppliedOperation} class.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
@RunWith(MockitoJUnitRunner.class)
public class DimensionFilterAppliedOperationTest {

    @Mock
    private DimensionFilter dimensionFilter;

    @Test
    public void shouldCreateCorrectDescriptionForDimensionFilterApplied() {

        var dimFilterOp = new DimensionFilterAppliedOperation(dimensionFilter, List.of("one", "two"), List.of("one"));

        assertThat(dimFilterOp.describe(),
            is("Dimension filter '" + dimensionFilter.getClass().getSimpleName() + "' applied."
                + " Versions before filter - 2, versions after filter - 1."));
    }

}
