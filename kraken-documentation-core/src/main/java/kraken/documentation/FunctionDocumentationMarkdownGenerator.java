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
package kraken.documentation;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.StringRenderer;

import kraken.el.functionregistry.documentation.FunctionDoc;
import kraken.el.functionregistry.documentation.LibraryDoc;

/**
 * A utility for creating Markdown documents from function documentation
 *
 * @author mulevicius
 */
public final class FunctionDocumentationMarkdownGenerator {

    private final STGroup template;

    public static final URL url =
        Thread.currentThread().getContextClassLoader().getResource("templates/function-documentation.stg");

    public FunctionDocumentationMarkdownGenerator(Map<String, String> sinceVersionMappings) {
        this.template = new STGroupFile(url, StandardCharsets.UTF_8.name(), '<', '>');
        this.template.registerRenderer(String.class, new CustomStringRenderer(sinceVersionMappings));
    }

    /**
     * Generates markdown document for a single function
     *
     * @param functionDoc
     * @return
     */
    public String generateFunctionDocumentation(FunctionDoc functionDoc) {
        ST st = template.getInstanceOf("functionDocumentation");
        st.add("function", functionDoc);
        return st.render();
    }

    /**
     * Generates markdown document from all function libraries. Includes table of contents.
     *
     * @param libraries
     * @return
     */
    public String generateLibraryPage(Collection<LibraryDoc> libraries) {
        ST st = template.getInstanceOf("page");
        st.add("libraries", libraries);
        return st.render();
    }

    /**
     * Generates markdown document from all function libraries. Does NOT include table of contents.
     *
     * @param libraries
     * @return
     */
    public String generateLibraries(Collection<LibraryDoc> libraries) {
        ST st = template.getInstanceOf("libraries");
        st.add("libraries", libraries);
        return st.render();
    }

    /**
     * Generates function declaration string of a function.
     *
     * @param functionDoc
     * @return
     */
    public String generateFunctionDeclaration(FunctionDoc functionDoc) {
        ST st = template.getInstanceOf("functionDeclaration");
        st.add("function", functionDoc);
        return st.render();
    }

    static class CustomStringRenderer extends StringRenderer {

        private final Map<String, String> sinceVersionMappings;

        CustomStringRenderer(Map<String, String> sinceVersionMappings) {
            this.sinceVersionMappings = sinceVersionMappings;
        }

        @Override
        public String toString(Object o, String formatString, Locale locale) {
            String str = (String) o;
            if(formatString == null){
                return str;
            }
            // renders text as a markdown link id by removing non word symbols and replacing spaces with dash
            if("linkId".equals(formatString)){
                return str.toLowerCase().replaceAll("^\\w\\s", "").replace(' ', '-');
            }
            // maps since function version
            if("mappedSinceVersion".equals(formatString)){
                return sinceVersionMappings.getOrDefault(str, str);
            }
            return super.toString(o, formatString, locale);
        }
    }

}
