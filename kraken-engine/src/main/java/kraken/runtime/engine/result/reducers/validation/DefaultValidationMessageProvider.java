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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import kraken.annotations.API;
import kraken.model.validation.UsageType;
import kraken.runtime.engine.result.AssertionPayloadResult;
import kraken.runtime.engine.result.LengthPayloadResult;
import kraken.runtime.engine.result.NumberSetPayloadResult;
import kraken.runtime.engine.result.RegExpPayloadResult;
import kraken.runtime.engine.result.SizePayloadResult;
import kraken.runtime.engine.result.SizeRangePayloadResult;
import kraken.runtime.engine.result.UsagePayloadResult;
import kraken.runtime.engine.result.ValueListPayloadResult;

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
        NUMBER_SET_MIN("number-set-min-error", "Value must be {0} or larger"),
        NUMBER_SET_MAX("number-set-max-error", "Value must be {0} or smaller"),
        NUMBER_SET_MIN_MAX("number-set-min-max-error", "Value must be in interval between {0} and {1} inclusively"),
        NUMBER_SET_MIN_STEP("number-set-min-step-error", "Value must be {0} or larger with increment {1}"),
        NUMBER_SET_MAX_STEP("number-set-max-step-error", "Value must be {0} or smaller with decrement {1}"),
        NUMBER_SET_MIN_MAX_STEP("number-set-min-max-step-error", "Value must be in interval between {0} and {1} inclusively with increment {2}"),
        SIZE("rule-size-error", "Invalid collection size"),
        SIZE_RANGE("rule-size-range-error", "Invalid collection size"),
        VALUE_LIST("value-list-error", "Value must be one of: {0}");

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

    @Nonnull
    @Override
    public ValidationMessage numberSetErrorMessage(@Nonnull NumberSetPayloadResult payloadResult) {
        var min = payloadResult.getMin();
        var max = payloadResult.getMax();
        var step = payloadResult.getStep();

        if(min != null && max != null && step != null) {
            return new ValidationMessage(MESSAGE.NUMBER_SET_MIN_MAX_STEP.code, MESSAGE.NUMBER_SET_MIN_MAX_STEP.message);
        }
        if(min != null && max == null && step != null) {
            return new ValidationMessage(MESSAGE.NUMBER_SET_MIN_STEP.code, MESSAGE.NUMBER_SET_MIN_STEP.message);
        }
        if(min == null && max != null && step != null) {
            return new ValidationMessage(MESSAGE.NUMBER_SET_MAX_STEP.code, MESSAGE.NUMBER_SET_MAX_STEP.message);
        }
        if(min != null && max != null && step == null) {
            return new ValidationMessage(MESSAGE.NUMBER_SET_MIN_MAX.code, MESSAGE.NUMBER_SET_MIN_MAX.message);
        }
        if(min != null && max == null && step == null) {
            return new ValidationMessage(MESSAGE.NUMBER_SET_MIN.code, MESSAGE.NUMBER_SET_MIN.message);
        }
        if(min == null && max != null && step == null) {
            return new ValidationMessage(MESSAGE.NUMBER_SET_MAX.code, MESSAGE.NUMBER_SET_MAX.message);
        }
        throw new IllegalArgumentException("Cannot resolve error message for number set payload");
    }

    @Nonnull
    @Override
    public ValidationMessage valueListErrorMessage(@Nonnull ValueListPayloadResult payloadResult) {
        return new ValidationMessage(
            MESSAGE.VALUE_LIST.code,
            MESSAGE.VALUE_LIST.message,
            new ArrayList<>(payloadResult.getValueList().getValues())
        );
    }

}
