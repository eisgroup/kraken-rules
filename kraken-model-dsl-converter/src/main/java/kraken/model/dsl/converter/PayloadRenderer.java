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

import kraken.model.derive.DefaultValuePayload;
import kraken.model.validation.SizePayload;
import kraken.model.validation.UsagePayload;
import kraken.model.validation.ValidationSeverity;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Used by {@link DSLModelConverter} to render {@link kraken.model.Payload}
 * when converting {@link kraken.model.dsl.KrakenDSLModel} to Dsl <code>string</code>
 *
 * @author avasiliauskas
 */
class PayloadRenderer {

    private static final STGroup TEMPLATE_GROUP;
    private static final DefaultErrorListener ERROR_LISTENER;

    static {
        TEMPLATE_GROUP = new STGroupFile(
                Thread.currentThread().getContextClassLoader().getResource("templates/rule-payload-model.stg"),
                StandardCharsets.UTF_8.name(),
                '<', '>'
        );
        TEMPLATE_GROUP.registerRenderer(ValidationSeverity.class, PayloadRenderer::validationSeverityRenderer);
        TEMPLATE_GROUP.registerRenderer(String.class, new CustomStringRenderer());
        ERROR_LISTENER = new DefaultErrorListener();
        TEMPLATE_GROUP.setListener(ERROR_LISTENER);
    }

    static String validationSeverityRenderer(Object object, String s, Locale locale) {
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

    static String usagePayloadRenderer(Object payload, String formatString, Locale locale) {
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

    static String regExpPayloadRenderer(Object payload, String formatString, Locale locale){
        return PayloadRenderer.renderPayload(payload, "regExpPayload");
    }

    static String assertionPayloadRenderer(Object payload, String formatString, Locale locale){
        return PayloadRenderer.renderPayload(payload, "assertionPayload");
    }

    static String sizeRangePayloadRenderer(Object payload, String s, Locale locale) {
        return PayloadRenderer.renderPayload(payload, "sizeRangePayload");
    }

    static String lengthPayloadRenderer(Object payload, String s, Locale locale) {
        return PayloadRenderer.renderPayload(payload, "lengthPayload");
    }

    static String visibilityPayloadRenderer(Object payload, String s, Locale locale) {
        return  PayloadRenderer.render("visibilityPayload");
    }

    static String accessibilityPayloadRenderer(Object payload, String s, Locale locale) {
        return PayloadRenderer.render("accessibilityPayload");
    }

    static String sizePayloadRenderer(Object payload, String formatString, Locale locale) {
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

    static String defaultValuePayloadRenderer(Object payload, String formatString, Locale locale) {
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

    private static String renderPayload(Object payload, String instanceName){
        ST st = TEMPLATE_GROUP.getInstanceOf(instanceName);
        st.add("payload", payload);
        return renderST(st);
    }

    private static String render(String instanceName){
        ST st = TEMPLATE_GROUP.getInstanceOf(instanceName);
        return renderST(st);
    }

    private static String renderST(ST st){
        String dsl = st.render();
        if(!ERROR_LISTENER.getErrors().isEmpty()) {
            String msg = "Error while generating DSL: " + String.join(";", ERROR_LISTENER.getErrors());
            throw new IllegalStateException(msg);
        }
        return dsl;
    }

}
