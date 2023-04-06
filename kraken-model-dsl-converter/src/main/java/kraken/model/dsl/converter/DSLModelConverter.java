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
package kraken.model.dsl.converter;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import kraken.annotations.API;
import kraken.el.ast.builder.Literals;
import kraken.model.Metadata;
import kraken.model.context.Cardinality;
import kraken.model.context.ContextField;
import kraken.model.context.ContextNavigation;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.resource.Resource;
import kraken.model.state.AccessibilityPayload;
import kraken.model.state.VisibilityPayload;
import kraken.model.validation.AssertionPayload;
import kraken.model.validation.LengthPayload;
import kraken.model.validation.NumberSetPayload;
import kraken.model.validation.RegExpPayload;
import kraken.model.validation.SizePayload;
import kraken.model.validation.SizeRangePayload;
import kraken.model.validation.UsagePayload;
import kraken.model.validation.ValueListPayload;
import kraken.utils.Dates;

/**
 * Converts Rules model to Rules DSL model as a <code>string<code/>.
 * For templating {@link MapInternalsModelAdaptor} is used.
 * It adds different behavior to the template definition.
 *
 * @see MapInternalsModelAdaptor
 * @author psurinin
 */
@API
public class DSLModelConverter {

    private final DefaultErrorListener errorListener;
    private final STGroup template;

    public static final java.net.URL URL =
        Thread.currentThread().getContextClassLoader().getResource("templates/kraken-dsl-model.stg");

    public DSLModelConverter() {
        template = new STGroupFile(
            URL,
                StandardCharsets.UTF_8.name(),
                '<', '>'
        );
        PayloadRenderer payloadRenderer = new PayloadRenderer();

        // this adaptor will override "keys" and "values" properties for map in templates
        template.registerModelAdaptor(Map.class, new MapInternalsModelAdaptor());
        template.registerModelAdaptor(Metadata.class, (interp, self, o, property, propertyName) -> {
            Metadata metadata = (Metadata) o;
            var props = new HashMap<String, Object>();
            for (Map.Entry<String, Object> entry : metadata.asMap().entrySet()) {
                Object value = entry.getValue();
                if (value == null) {
                    continue;
                }
                if (value instanceof String) {

                    props.put(entry.getKey(), "\"" + Literals.deescape((String)value) + "\"");
                } else if (value instanceof LocalDate) {
                    props.put(entry.getKey(), Dates.convertLocalDateToISO((LocalDate) value));
                } else if (value instanceof LocalDateTime) {
                    props.put(entry.getKey(), Dates.convertLocalDateTimeToISO((LocalDateTime) value));
                } else {
                    props.put(entry.getKey(), value);
                }
            }
            return  props;
        });
        template.registerRenderer(DefaultValuePayload.class, payloadRenderer::defaultValuePayloadRenderer);
        template.registerRenderer(UsagePayload.class, payloadRenderer::usagePayloadRenderer);
        template.registerRenderer(SizePayload.class, payloadRenderer::sizePayloadRenderer);
        template.registerRenderer(RegExpPayload.class, payloadRenderer::regExpPayloadRenderer);
        template.registerRenderer(NumberSetPayload.class, payloadRenderer::numberSetPayloadRenderer);
        template.registerRenderer(AssertionPayload.class, payloadRenderer::assertionPayloadRenderer);
        template.registerRenderer(SizeRangePayload.class, payloadRenderer::sizeRangePayloadRenderer);
        template.registerRenderer(LengthPayload.class, payloadRenderer::lengthPayloadRenderer);
        template.registerRenderer(VisibilityPayload.class, payloadRenderer::visibilityPayloadRenderer);
        template.registerRenderer(AccessibilityPayload.class, payloadRenderer::accessibilityPayloadRenderer);
        template.registerRenderer(ValueListPayload.class, payloadRenderer::valueListPayloadRenderer);
        template.registerRenderer(Cardinality.class, ContextDefinitionRenderer::cardinalityRenderer);
        template.registerRenderer(String.class, new CustomStringRenderer());
        template.registerRenderer(Number.class, new CustomNumberRenderer());
        template.registerModelAdaptor(ContextNavigation.class, new ContextDefinitionRenderer.ContextNavigationAdapter());
        template.registerModelAdaptor(ContextField.class, new ContextDefinitionRenderer.ContextFieldAdapter());

        errorListener = new DefaultErrorListener();
        template.setListener(errorListener);
    }

    /**
     * Kraken Resource to Kraken DSL converter.
     * Used to render {@link Resource} to single dsl string.
     * Template used to render string - <code>.../template/kraken-dsl-model.stg</code>
     *
     * @param krakenResource to render.
     * @return DSL as string.
     * @since 1.1.0
     */
    public String convert(Resource krakenResource) {
        ST st = template.getInstanceOf("krakenResource");
        st.add("krakenResource", krakenResource);
        String dsl = st.render();
        if(!errorListener.getErrors().isEmpty()) {
            String msg = "Error while generating DSL: " + String.join(";", errorListener.getErrors());
            throw new IllegalStateException(msg);
        }
        return dsl;
    }

}
