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

import javax.annotation.Nonnull;

import kraken.annotations.API;
import kraken.model.validation.UsageType;
import kraken.runtime.engine.result.AssertionPayloadResult;
import kraken.runtime.engine.result.LengthPayloadResult;
import kraken.runtime.engine.result.RegExpPayloadResult;
import kraken.runtime.engine.result.SizePayloadResult;
import kraken.runtime.engine.result.SizeRangePayloadResult;
import kraken.runtime.engine.result.UsagePayloadResult;

/**
 * Provides default messages.
 *
 * @author mulevicius
 * @since 1.24.0
 */
@API
public class DefaultValidationMessageProvider implements ValidationMessageProvider {

    /**
     * All default messages provided
     */
    public enum MESSAGE {
        ASSERTION("rule-assertion-error", "Assertion failed"),
        USAGE_MANDATORY("rule-mandatory-error", "Field is mandatory"),
        USAGE_EMPTY("rule-mandatory-empty-error", "Field must be empty"),
        REGEXP("rule-regexp-error", "Field must match regular expression pattern: {0}"),
        LENGTH("rule-length-error", "Text must not be longer than {0}"),
        SIZE("rule-size-error", "Invalid collection size"),
        SIZE_RANGE("rule-size-range-error", "Invalid collection size");

        private final String code;
        private final String message;

        MESSAGE(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    @Override
    @Nonnull
    public ValidationMessage assertionErrorMessage(@Nonnull AssertionPayloadResult payloadResult) {
        return new ValidationMessage(MESSAGE.ASSERTION.code, MESSAGE.ASSERTION.message);
    }

    @Override
    @Nonnull
    public ValidationMessage usageErrorMessage(@Nonnull UsagePayloadResult payloadResult) {
        if (payloadResult.getUsageType() == UsageType.mandatory) {
            return new ValidationMessage(MESSAGE.USAGE_MANDATORY.code, MESSAGE.USAGE_MANDATORY.message);
        }
        if (payloadResult.getUsageType() == UsageType.mustBeEmpty) {
            return new ValidationMessage(MESSAGE.USAGE_EMPTY.code, MESSAGE.USAGE_EMPTY.message);
        }
        throw new IllegalStateException("Unknown usage type encountered: " + payloadResult.getUsageType());
    }

    @Override
    @Nonnull
    public ValidationMessage regExpErrorMessage(@Nonnull RegExpPayloadResult payloadResult) {
        return new ValidationMessage(MESSAGE.REGEXP.code, MESSAGE.REGEXP.message, List.of(payloadResult.getRegExp()));
    }

    @Override
    @Nonnull
    public ValidationMessage lengthErrorMessage(@Nonnull LengthPayloadResult payloadResult) {
        return new ValidationMessage(MESSAGE.LENGTH.code, MESSAGE.LENGTH.message, List.of(payloadResult.getLength()));
    }

    @Override
    @Nonnull
    public ValidationMessage sizeErrorMessage(@Nonnull SizePayloadResult payloadResult) {
        return new ValidationMessage(MESSAGE.SIZE.code, MESSAGE.SIZE.message);
    }

    @Override
    @Nonnull
    public ValidationMessage sizeRangeErrorMessage(@Nonnull SizeRangePayloadResult payloadResult) {
        return new ValidationMessage(MESSAGE.SIZE_RANGE.code, MESSAGE.SIZE_RANGE.message);
    }

}
