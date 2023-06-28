/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.model.dsl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import kraken.model.Dimension;
import kraken.model.DimensionDataType;
import kraken.model.resource.Resource;

/**
 * @author Tomas Dapkunas
 * @since 1.48.0
 */
public class KrakenModelDSLParserDimensionTest {

    @Test
    public void shouldParseDimensions() {
        Resource resource = KrakenDSLModelParser.parseResource(
            "Dimension \"string dimension\" : String"
                + System.lineSeparator() +
                "Dimension \"date dimension\" : Date"
        );

        assertThat(resource.getDimensions(), hasSize(2));

        Dimension stringDimension = resource.getDimensions().get(0);
        assertThat(stringDimension.getName(), is("string dimension"));
        assertThat(stringDimension.getDataType(), is(DimensionDataType.STRING));

        Dimension dateDimension = resource.getDimensions().get(1);
        assertThat(dateDimension.getName(), is("date dimension"));
        assertThat(dateDimension.getDataType(), is(DimensionDataType.DATE));
    }

    @Test
    public void shouldThrowExceptionWhilePArsingDimensionWithInvalidDataType() {
        assertThrows(IllegalStateException.class, () -> KrakenDSLModelParser.parseResource(
            "Dimension \"string dimension\" : SomeType"
        ));
    }

}
