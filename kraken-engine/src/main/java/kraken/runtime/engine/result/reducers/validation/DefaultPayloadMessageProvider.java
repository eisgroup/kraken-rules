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
package kraken.runtime.engine.result.reducers.validation;

import java.text.MessageFormat;

import kraken.annotations.API;
import kraken.model.validation.UsageType;
import kraken.runtime.engine.result.AssertionPayloadResult;
import kraken.runtime.engine.result.LengthPayloadResult;
import kraken.runtime.engine.result.NumberSetPayloadResult;
import kraken.runtime.engine.result.RegExpPayloadResult;
import kraken.runtime.engine.result.SizePayloadResult;
import kraken.runtime.engine.result.SizeRangePayloadResult;
import kraken.runtime.engine.result.UsagePayloadResult;
import kraken.runtime.engine.result.ValidationPayloadResult;
import kraken.runtime.engine.result.ValueListPayloadResult;
import kraken.runtime.engine.result.reducers.validation.DefaultValidationMessageProvider.MESSAGE;
import kraken.runtime.engine.result.reducers.validation.ValidationMessageProvider.ValidationMessage;

/**
 * Provides default messages. Can be extended to override only specific payload messages.
 *
 * @author mulevicius
 * @deprecated this implementation does not support localization. Use {@link DefaultValidationMessageProvider} instead.
 */
@API
@Deprecated(since = "1.24.0", forRemoval = true)
public class DefaultPayloadMessageProvider implements PayloadMessageProvider {

    @Override
    public DefaultMessage getRegExpMessage() {
        return new DefaultMessage("rule-regexp-error", "Field must match regular expression pattern: {0}");
    }

    @Override
    public DefaultMessage getUsageMandatoryMessage() {
        return new DefaultMessage("rule-mandatory-error", "Field is mandatory");
    }

    @Override
    public DefaultMessage getUsageEmptyMessage() {
        return new DefaultMessage("rule-mandatory-empty-error", "Field must be empty");
    }

    @Override
    public DefaultMessage getLengthMessage() {
        return new DefaultMessage("rule-length-error", "Text must not be longer than {0}");
    }

    @Override
    public DefaultMessage getAssertionMessage() {
        return new DefaultMessage("rule-assertion-error", "Assertion failed");
    }

    @Override
    public DefaultMessage getCollectionSizeMessage() {
        return new DefaultMessage("rule-size-error", "Invalid collection size");
    }

    @Override
    public DefaultMessage getCollectionSizeRangeMessage() {
        return new DefaultMessage("rule-size-range-error", "Invalid collection size");
    }

    private DefaultMessage getValueListMessage() {
        return new DefaultMessage(MESSAGE.VALUE_LIST.getCode(), MESSAGE.VALUE_LIST.getMessage());
    }

    @Override
    public DefaultMessage resolveByPayloadResult(ValidationPayloadResult validationPayloadResult) {
        if (validationPayloadResult instanceof AssertionPayloadResult) {
            return getAssertionMessage();
        }
        if (validationPayloadResult instanceof SizePayloadResult) {
            return getCollectionSizeMessage();
        }
        if (validationPayloadResult instanceof SizeRangePayloadResult) {
            return getCollectionSizeRangeMessage();
        }
        if (validationPayloadResult instanceof LengthPayloadResult) {
            LengthPayloadResult payloadResult = (LengthPayloadResult) validationPayloadResult;
            DefaultMessage message = getLengthMessage();
            return new DefaultMessage(message.getCode(), MessageFormat.format(message.getMessage(), payloadResult.getLength()));
        }
        if (validationPayloadResult instanceof RegExpPayloadResult) {
            RegExpPayloadResult payloadResult = (RegExpPayloadResult) validationPayloadResult;
            DefaultMessage message = getRegExpMessage();
            return new DefaultMessage(message.getCode(), MessageFormat.format(message.getMessage(), payloadResult.getRegExp()));
        }
        if (validationPayloadResult instanceof UsagePayloadResult) {
            UsagePayloadResult payloadResult = (UsagePayloadResult) validationPayloadResult;
            if (payloadResult.getUsageType() == UsageType.mandatory) {
                return getUsageMandatoryMessage();
            }
            if (payloadResult.getUsageType() == UsageType.mustBeEmpty) {
                return getUsageEmptyMessage();
            }
        }
        if(validationPayloadResult instanceof NumberSetPayloadResult) {
            NumberSetPayloadResult payloadResult = (NumberSetPayloadResult) validationPayloadResult;
            var min = payloadResult.getMin();
            var max = payloadResult.getMax();
            var step = payloadResult.getStep();

            if(min != null && max != null && step != null) {
                return new DefaultMessage(MESSAGE.NUMBER_SET_MIN_MAX_STEP.getCode(),
                    MESSAGE.NUMBER_SET_MIN_MAX_STEP.getMessage());
            }
            if(min != null && max == null && step != null) {
                return new DefaultMessage(MESSAGE.NUMBER_SET_MIN_STEP.getCode(),
                    MESSAGE.NUMBER_SET_MIN_STEP.getMessage());
            }
            if(min == null && max != null && step != null) {
                return new DefaultMessage(MESSAGE.NUMBER_SET_MAX_STEP.getCode(),
                    MESSAGE.NUMBER_SET_MAX_STEP.getMessage());
            }
            if(min != null && max != null && step == null) {
                return new DefaultMessage(MESSAGE.NUMBER_SET_MIN_MAX.getCode(),
                    MESSAGE.NUMBER_SET_MIN_MAX.getMessage());
            }
            if(min != null && max == null && step == null) {
                return new DefaultMessage(MESSAGE.NUMBER_SET_MIN.getCode(), MESSAGE.NUMBER_SET_MIN.getMessage());
            }
            if(min == null && max != null && step == null) {
                return new DefaultMessage(MESSAGE.NUMBER_SET_MAX.getCode(), MESSAGE.NUMBER_SET_MAX.getMessage());
            }
            throw new IllegalArgumentException("Cannot resolve error message for number set payload");
        }
        if (validationPayloadResult instanceof ValueListPayloadResult) {
            return getValueListMessage();
        }
        throw new IllegalStateException("Unknown payload result type encountered: " + validationPayloadResult.getClass());
    }
}
