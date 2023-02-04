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
package kraken.runtime.engine.result.reducers.validation;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import kraken.annotations.API;
import kraken.annotations.SPI;
import kraken.runtime.engine.result.AssertionPayloadResult;
import kraken.runtime.engine.result.LengthPayloadResult;
import kraken.runtime.engine.result.NumberSetPayloadResult;
import kraken.runtime.engine.result.RegExpPayloadResult;
import kraken.runtime.engine.result.SizePayloadResult;
import kraken.runtime.engine.result.SizeRangePayloadResult;
import kraken.runtime.engine.result.UsagePayloadResult;
import kraken.runtime.engine.result.ValueListPayloadResult;

/**
 * Provides default messages for validation payload results in case validation rules do not have code
 * and/or message specified
 *
 * @author mulevicius
 * @since 1.24.0
 */
@SPI
public interface ValidationMessageProvider {

    /**
     * @param payloadResult Result of usage payload, can be used to parametrize validation message.
     * @return Default error message to set for usage validation result.
     * Can be further customized based on {@link UsagePayloadResult#getUsageType()}.
     * This is invoked only when validation severity is ERROR.
     */
    @Nonnull ValidationMessage usageErrorMessage(@Nonnull UsagePayloadResult payloadResult);

    /**
     * @param payloadResult Result of regular expression payload, can be used to parametrize validation message.
     * @return Default error message to set for regular expression validation result.
     * This is invoked only when validation severity is ERROR.
     */
    @Nonnull ValidationMessage regExpErrorMessage(@Nonnull RegExpPayloadResult payloadResult);

    /**
     * @param payloadResult Result of collection size payload, can be used to parametrize validation message.
     * @return Default error message to set for collection size validation result.
     * This is invoked only when validation severity is ERROR.
     */
    @Nonnull ValidationMessage sizeErrorMessage(@Nonnull SizePayloadResult payloadResult);

    /**
     * @param payloadResult Result of assertion payload, can be used to parametrize validation message.
     * @return Default error message to set for assertion validation result.
     * This is invoked only when validation severity is ERROR.
     */
    @Nonnull ValidationMessage assertionErrorMessage(@Nonnull AssertionPayloadResult payloadResult);

    /**
     * @param payloadResult Result of string length payload, can be used to parametrize validation message.
     * @return Default error message to set for string length validation result.
     * This is invoked only when validation severity is ERROR.
     */
    @Nonnull ValidationMessage lengthErrorMessage(@Nonnull LengthPayloadResult payloadResult);

    /**
     * @param payloadResult Result of collection size range payload, can be used to parametrize validation message.
     * @return Default error message to set for collection size range validation result.
     * This is invoked only when validation severity is ERROR.
     */
    @Nonnull ValidationMessage sizeRangeErrorMessage(@Nonnull SizeRangePayloadResult payloadResult);

    /**
     * @param payloadResult Result of number set payload, can be used to parametrize validation message.
     * @return Default error message to set for number set validation result.
     * This is invoked only when validation severity is ERROR.
     */
    @Nonnull ValidationMessage numberSetErrorMessage(@Nonnull NumberSetPayloadResult payloadResult);

    /**
     * @param payloadResult Result of value size payload, can be used to parametrize validation massage.
     * @return Default error message to set for value list validation result. This method is only invoked
     *     when validation severity is ERROR.
     */
    @Nonnull ValidationMessage valueListErrorMessage(@Nonnull ValueListPayloadResult payloadResult);

    /**
     * Represents default message that will be set in rule validation result when rule itself do not have specific
     * message specified.
     *
     * @author mulevicius
     * @since 1.24.0
     */
    @API
    class ValidationMessage {
        private final String code;
        private final String message;
        private final List<Object> parameters;

        /**
         * Creates static validation message with code and message.
         *
         * @param code Code uniquely identifies this message.
         *             Localized message is loaded from localized message bundles by this code.
         * @param message Message text.
         */
        public ValidationMessage(@Nonnull String code, @Nonnull String message) {
            this(code, message, List.of());
        }

        /**
         * Creates validation message template with code, message template and template parameters.
         *
         * @param code Code uniquely identifies this message.
         *             Localized message is loaded from localized message bundles by this code.
         * @param messageTemplate Message template text.
         *                        Must be compatible with {@link java.text.MessageFormat} syntax.
         *                        A final message is obtained by formatting message template by using parameters.
         * @param parameters A list of parameters to use when formatting template.
         *                   Parameters are also be used when formatting message resolved from localized message bundles.
         */
        public ValidationMessage(@Nonnull String code, @Nonnull String messageTemplate, @Nonnull List<Object> parameters) {
            this.code = Objects.requireNonNull(code);
            this.message = Objects.requireNonNull(messageTemplate);
            this.parameters = Objects.requireNonNull(parameters);
        }

        @Nonnull
        public String getCode() {
            return code;
        }

        @Nonnull
        public String getMessage() {
            return message;
        }

        @Nonnull
        public List<Object> getParameters() {
            return parameters;
        }
    }

}
