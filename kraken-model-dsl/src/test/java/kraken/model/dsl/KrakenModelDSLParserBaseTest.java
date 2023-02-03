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
package kraken.model.dsl;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import kraken.model.dsl.error.LineParseCancellationException;
import kraken.model.resource.Resource;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static kraken.model.dsl.KrakenDSLModelParser.parseResource;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

/**
 * Base test for {@link KrakenDSLModelParser} that verifies if context, rule and entryPoint blocks are parsed correctly
 *
 * @author mulevicius
 */
public class KrakenModelDSLParserBaseTest {

    @Test
    public void shouldParseEmpty() {
        Resource model = parseResource("");

        assertThat(model.getContextDefinitions(), empty());
        assertThat(model.getEntryPoints(), empty());
        assertThat(model.getRules(), empty());
    }

    @Test
    public void shouldParseNoContents() {
        Resource model = parseResource("Rules{ Rules{}} EntryPoints{ EntryPoints{} } Contexts{} Rules{} EntryPoints{} Contexts{ Contexts{}}");

        assertThat(model.getContextDefinitions(), empty());
        assertThat(model.getEntryPoints(), empty());
        assertThat(model.getRules(), empty());
    }

    @Test
    public void shouldParseComplexExpression() {
        Resource model = parseResource("Rules { Rule 'rule1' On RiskItem.itemName {Reset To Date('2018-01-01')}}");

        assertThat(model.getRules(), hasSize(1));
    }

    @Test
    public void shouldFailToParseComplexExpression() {
        assertThrows(LineParseCancellationException.class,
                () -> parseResource("Rules { Rule 'rule1' On RiskItem.itemName {Rese To Date('2018-01-01')}}"));
    }

    @Test
    public void shouldParseContents() throws IOException {
        URL url = getClass().getResource("KrakenModelDSLParserBaseTest.shouldParseContents.rules");
        String dsl = IOUtils.toString(url, Charset.forName("UTF-8"));

        Resource model = parseResource(dsl);

        assertThat(model.getContextDefinitions(), hasSize(2));
        assertThat(model.getEntryPoints(), hasSize(2));
        assertThat(model.getRules(), hasSize(2));

        assertThat(model.getRules().get(0).getMetadata().getProperty("packageCd"), equalTo("Barber"));
        assertThat(model.getRules().get(1).getMetadata().getProperty("packageCd"), equalTo("Pizza"));
        assertThat(model.getRules().get(1).getMetadata().getProperty("planCd"), equalTo("Premium"));

    }

}