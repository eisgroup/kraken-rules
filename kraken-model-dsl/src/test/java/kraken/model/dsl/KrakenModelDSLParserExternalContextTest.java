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

import kraken.model.context.external.ExternalContext;
import kraken.model.resource.Resource;

import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Test;

import static kraken.model.dsl.KrakenDSLModelParser.parseResource;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Unit tests to test parsing of {@code ExternalContext} elements.
 *
 * @author Tomas Dapkunas
 * @since 1.3.0
 */
public class KrakenModelDSLParserExternalContextTest {

    @Test
    public void shouldParseExternalContextWithMultipleChildContexts() {
        Resource model = parseResource("Namespace Test " +
                "ExternalContext {" +
                "  context : {" +
                "    bounded: BoundedDefinition" +
                "  }," +
                "  anotherContext : {" +
                "    anotherBounded: AnotherBoundedDefinition" +
                "  }" +
                "}" +
                "ExternalEntity BoundedDefinition {}" +
                "ExternalEntity AnotherBoundedDefinition {}");

        ExternalContext externalContext = model.getExternalContext();
        assertThat(externalContext.getName(), is("ExternalContext_root"));

        assertNotNull(externalContext.getContexts().get("context"));
        ExternalContext context = externalContext.getContexts().get("context");
        assertThat(context.getName(), is("ExternalContext_context"));
        assertNotNull(context.getExternalContextDefinitions().get("bounded"));
        assertNotNull(externalContext.getContexts().get("anotherContext"));
        ExternalContext anotherContext = externalContext.getContexts().get("anotherContext");
        assertThat(anotherContext.getName(), is("ExternalContext_anotherContext"));
        assertNotNull(anotherContext.getExternalContextDefinitions().get("anotherBounded"));
    }

    @Test
    public void shouldParseExternalContextWithNestedChildContextsAndBoundedDefinitions() {
        Resource model = parseResource("Namespace Test " +
                "ExternalContext {" +
                "  context : {" +
                "    securityContext: SecurityContext," +
                "    external : {" +
                "      prev: PreviousRevisionProjection" +
                "    }" +
                "  }" +
                "}" +
                "ExternalEntity SecurityContext {}" +
                "ExternalEntity PreviousRevisionProjection {}");

        ExternalContext externalContext = model.getExternalContext();
        assertThat(externalContext.getName(), is("ExternalContext_root"));

        assertNotNull(externalContext.getContexts().get("context"));
        ExternalContext context = externalContext.getContexts().get("context");
        assertThat(context.getName(), is("ExternalContext_context"));
        assertNotNull(context.getExternalContextDefinitions().get("securityContext"));
        assertNotNull(context.getContexts().get("external"));
        ExternalContext external = context.getContexts().get("external");
        assertThat(external.getName(), is("ExternalContext_context_external"));
        assertNotNull(external.getExternalContextDefinitions().get("prev"));
    }

    @Test
    public void shouldParseEmptyExternalContext() {
        Resource model = parseResource("Namespace Test " +
            "ExternalContext {" +
            "}");

        ExternalContext externalContext = model.getExternalContext();

        assertThat(externalContext.getContexts().values(), empty());
        assertThat(externalContext.getExternalContextDefinitions().values(), empty());
    }
}
