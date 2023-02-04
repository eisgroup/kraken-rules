/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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
import java.util.Locale;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import kraken.model.ValueList;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.validation.SizePayload;
import kraken.model.validation.UsagePayload;
import kraken.model.validation.ValidationSeverity;
import kraken.model.validation.ValueListPayload;

/**
 * Used by {@link DSLModelConverter} to render {@link kraken.model.Payload}
 * when converting {@link kraken.model.resource.Resource} to Dsl <code>string</code>
 *
 * @author avasiliauskas
 */
class PayloadRenderer {


    private static final java.net.URL URL = 
        Thread.currentThread().getContextClassLoader().getResource("templates/rule-payload-model.stg");
    private final STGroup template;
    private final DefaultErrorListener errorListener;
        
    public PayloadRenderer() {
        template =
            new STGroupFile(URL, StandardCharsets.UTF_8.name(), '<', '>');
        template.registerRenderer(ValidationSeverity.class, this::validationSeverityRenderer);
        template.registerRenderer(String.class, new CustomStringRenderer());
        template.registerRenderer(Number.class, new CustomNumberRenderer());
        template.registerRenderer(ValueList.class, new ValueListRenderer());
        errorListener = new DefaultErrorListener();
        template.setListener(errorListener);
    }

    public String validationSeverityRenderer(Object object, String s, Locale locale) {
        ValidationSeverity validationSeverity = (ValidationSeverity)object;
        switch (validationSeverity) {
            case info: return "Info";
            case warning: return "Warn";
            case critical: return "Error";
            default:
                throw new IllegalStateException(
                        "ValidationSeverity can not be constructed with severity type: " + validationSeverity
                );
        }
    }

    String usagePayloadRenderer(Object payload, String formatString, Locale locale) {
        UsagePayload usagePayload = (UsagePayload) payload;
        switch (usagePayload.getUsageType()) {
            case mandatory: return renderPayload(payload, "usageMandatoryPayload");
            case mustBeEmpty: return renderPayload(payload, "usageEmptyPayload");
            default:
                throw new IllegalStateException(
                        "UsagePayload can not be constructed with usage type: " + usagePayload.getUsageType()
                );
        }
    }

    String regExpPayloadRenderer(Object payload, String formatString, Locale locale){
        return renderPayload(payload, "regExpPayload");
    }

    String assertionPayloadRenderer(Object payload, String formatString, Locale locale){
        return renderPayload(payload, "assertionPayload");
    }

    String sizeRangePayloadRenderer(Object payload, String s, Locale locale) {
        return renderPayload(payload, "sizeRangePayload");
    }

    String numberSetPayloadRenderer(Object payload, String s, Locale locale) {
        return renderPayload(payload, "numberSetPayload");
    }

    String lengthPayloadRenderer(Object payload, String s, Locale locale) {
        return renderPayload(payload, "lengthPayload");
    }

    String visibilityPayloadRenderer(Object payload, String s, Locale locale) {
        return  render("visibilityPayload");
    }

    String accessibilityPayloadRenderer(Object payload, String s, Locale locale) {
        return render("accessibilityPayload");
    }

    String sizePayloadRenderer(Object payload, String formatString, Locale locale) {
        SizePayload sizePayload = (SizePayload) payload;
        switch (sizePayload.getOrientation()) {
            case MAX: return renderPayload(payload, "sizeMaxPayload");
            case MIN: return renderPayload(payload, "sizeMinPayload");
            case EQUALS: return renderPayload(payload, "sizeEqualsPayload");
            default:
                throw new IllegalStateException(
                        "Size payload can not be created with orientation: " + sizePayload.getOrientation()
                );
        }
    }

    String defaultValuePayloadRenderer(Object payload, String formatString, Locale locale) {
        DefaultValuePayload defaultValuePayload = (DefaultValuePayload) payload;
        switch (defaultValuePayload.getDefaultingType()) {
            case defaultValue: return renderPayload(payload, "defaultValuePayload");
            case resetValue: return renderPayload(payload, "resetValuePayload");

            default:
                throw new IllegalStateException(
                        "DefaultValuePayload can not be constructed with defaulting type: " + defaultValuePayload.getDefaultingType()
                );
        }
    }

    String valueListPayloadRenderer(Object payload, String formatString, Locale locale) {
        return renderPayload(payload,"valueListPayload");
    }

    private String renderPayload(Object payload, String instanceName){
        ST st = template.getInstanceOf(instanceName);
        st.add("payload", payload);
        return renderST(st);
    }

    private String render(String instanceName){
        ST st = template.getInstanceOf(instanceName);
        return renderST(st);
    }

    private String renderST(ST st){
        String dsl = st.render();
        if(!errorListener.getErrors().isEmpty()) {
            String msg = "Error while generating DSL: " + String.join(";", errorListener.getErrors());
            throw new IllegalStateException(msg);
        }
        return dsl;
    }

}
