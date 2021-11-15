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
package kraken.model.dsl;

import java.util.Set;
import java.util.stream.Collectors;

import kraken.model.dsl.error.LineParseCancellationException;
import kraken.model.resource.Resource;
import kraken.model.resource.RuleImport;
import org.junit.Test;

import static kraken.model.dsl.KrakenDSLModelParser.parseResource;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author mulevicius
 */
public class KrakenModelDSLParserNamespaceTest {

    @Test
    public void shouldParseNamespaceOnly() {
        Resource model = parseResource("Namespace foo Rules{ Rules{}} EntryPoints{ EntryPoints{} } Contexts{} Rules{} EntryPoints{} Contexts{ Contexts{}}");

        assertThat(model.getNamespace(), is("foo"));
    }

    @Test(expected = LineParseCancellationException.class)
    public void shouldFailToParseNamespaceOnly() {
        parseResource("Namespae foo Rules{ Rules{}}");
    }


    @Test(expected = DSLParsingException.class)
    public void shouldNotAllowIncludesWithoutNamespace() {
        parseResource("Include foo Include bar Include baz Rules{ Rules{}} EntryPoints{ EntryPoints{} } Contexts{} Rules{} EntryPoints{} Contexts{ Contexts{}}");
    }

    @Test(expected = DSLParsingException.class)
    public void shouldNotAllowImportsWithoutNamespace() {
        parseResource("Import Rule \"foo\" from baz Rules{ Rules{}} EntryPoints{ EntryPoints{} } Contexts{} Rules{} EntryPoints{} Contexts{ Contexts{}}");
    }

    @Test
    public void shouldParseNamespaceAndIncludes() {
        Resource model = parseResource(
                "Namespace foo " +
                        "Include bar " +
                        "Include baz " +
                        "Rules{ Rules{}} EntryPoints{ EntryPoints{} } Contexts{} Rules{} EntryPoints{} Contexts{ Contexts{}}"
        );

        assertThat(model.getNamespace(), is("foo"));
        assertThat(model.getIncludes(), containsInAnyOrder("bar", "baz"));
    }

    @Test
    public void shouldParseNamespaceIncludesAndImports() {
        Resource model = parseResource(
                "Namespace foo " +
                        "Include bar " +
                        "Include a.b.baz " +
                        "Import Rule \"A\", \"C\" from B " +
                        "Rules{ Rules{}} EntryPoints{ EntryPoints{} } Contexts{} Rules{} EntryPoints{} Contexts{ Contexts{}}"
        );

        assertThat(model.getNamespace(), is("foo"));
        assertThat(model.getIncludes(), containsInAnyOrder("bar", "a.b.baz"));

        Set<String> imported = model.getRuleImports().stream()
                .filter(ri -> ri.getNamespace().equals("B"))
                .map(RuleImport::getRuleName)
                .collect(Collectors.toSet());

        assertThat(imported, hasItems("A", "C"));
    }

    @Test
    public void shouldParseNamespaceAndImports() {
        Resource model = parseResource(
                "Namespace a.b.foo " +
                        "Import Rule \"bar\" from A " +
                        "Import Rule \"baz\" from a.b.C " +
                        "Rules{ Rules{}} EntryPoints{ EntryPoints{} } Contexts{} Rules{} EntryPoints{} Contexts{ Contexts{}}"
        );

        assertThat(model.getNamespace(), is("a.b.foo"));

        Set<String> importedFromA = model.getRuleImports().stream()
                .filter(ri -> ri.getNamespace().equals("A"))
                .map(RuleImport::getRuleName)
                .collect(Collectors.toSet());

        Set<String> importedFromC = model.getRuleImports().stream()
                .filter(ri -> ri.getNamespace().equals("a.b.C"))
                .map(RuleImport::getRuleName)
                .collect(Collectors.toSet());

        assertThat(importedFromA,  hasItems("bar"));
        assertThat(importedFromC,  hasItems("baz"));
    }
}
