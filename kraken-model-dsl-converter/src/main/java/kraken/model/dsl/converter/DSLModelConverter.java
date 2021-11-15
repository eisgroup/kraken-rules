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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import kraken.annotations.API;
import kraken.model.Metadata;
import kraken.model.context.Cardinality;
import kraken.model.context.ContextField;
import kraken.model.context.ContextNavigation;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.factory.RulesModelFactory;
import kraken.model.resource.Resource;
import kraken.model.state.AccessibilityPayload;
import kraken.model.state.VisibilityPayload;
import kraken.model.validation.*;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

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

    private static final DefaultErrorListener ERROR_LISTENER;
    private static final STGroup TEMPLATE_GROUP;

    static {
        TEMPLATE_GROUP = new STGroupFile(
                Thread.currentThread().getContextClassLoader().getResource("templates/kraken-dsl-model.stg"),
                StandardCharsets.UTF_8.name(),
                '<', '>'
        );
        // this adaptor will override "keys" and "values" properties for map in templates
        TEMPLATE_GROUP.registerModelAdaptor(Map.class, new MapInternalsModelAdaptor());
        TEMPLATE_GROUP.registerModelAdaptor(Metadata.class, (interp, self, o, property, propertyName) -> {
            Metadata metadata = (Metadata) o;
            var props = new HashMap<String, Object>();
            for (Map.Entry<String, Object> entry : metadata.asMap().entrySet()) {
                Consumer<Object> put = value -> props.put(entry.getKey(), value);
                Object value = entry.getValue();
                if (value == null) {
                    continue;
                }
                if (value instanceof String) {
                    put.accept("\"" + value + "\"");
                } else if (value instanceof LocalDate) {
                    put.accept(((LocalDate) value).format(DateTimeFormatter.ISO_LOCAL_DATE));
                } else if (value instanceof LocalDateTime) {
                    String dateTime = ZonedDateTime.of((LocalDateTime) value, ZoneId.systemDefault())
                            .truncatedTo(ChronoUnit.MILLIS)
                            .format(DateTimeFormatter.ISO_INSTANT);
                    put.accept(dateTime);
                } else put.accept(value);
            }
            return  props;
        });
        TEMPLATE_GROUP.registerRenderer(DefaultValuePayload.class, PayloadRenderer::defaultValuePayloadRenderer);
        TEMPLATE_GROUP.registerRenderer(UsagePayload.class, PayloadRenderer::usagePayloadRenderer);
        TEMPLATE_GROUP.registerRenderer(SizePayload.class, PayloadRenderer::sizePayloadRenderer);
        TEMPLATE_GROUP.registerRenderer(RegExpPayload.class, PayloadRenderer::regExpPayloadRenderer);
        TEMPLATE_GROUP.registerRenderer(AssertionPayload.class, PayloadRenderer::assertionPayloadRenderer);
        TEMPLATE_GROUP.registerRenderer(SizeRangePayload.class, PayloadRenderer::sizeRangePayloadRenderer);
        TEMPLATE_GROUP.registerRenderer(LengthPayload.class, PayloadRenderer::lengthPayloadRenderer);
        TEMPLATE_GROUP.registerRenderer(VisibilityPayload.class, PayloadRenderer::visibilityPayloadRenderer);
        TEMPLATE_GROUP.registerRenderer(AccessibilityPayload.class, PayloadRenderer::accessibilityPayloadRenderer);
        TEMPLATE_GROUP.registerRenderer(Cardinality.class, ContextDefinitionRenderer::cardinalityRenderer);
        TEMPLATE_GROUP.registerRenderer(String.class, new CustomStringRenderer());
        TEMPLATE_GROUP.registerModelAdaptor(ContextNavigation.class, new ContextDefinitionRenderer.ContextNavigationAdapter());
        TEMPLATE_GROUP.registerModelAdaptor(ContextField.class, new ContextDefinitionRenderer.ContextFieldAdapter());

        ERROR_LISTENER = new DefaultErrorListener();
        TEMPLATE_GROUP.setListener(ERROR_LISTENER);
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
        ST st = TEMPLATE_GROUP.getInstanceOf("krakenResource");
        st.add("krakenResource", krakenResource);
        String dsl = st.render();
        if(!ERROR_LISTENER.getErrors().isEmpty()) {
            String msg = "Error while generating DSL: " + String.join(";", ERROR_LISTENER.getErrors());
            throw new IllegalStateException(msg);
        }
        return dsl;
    }

}
