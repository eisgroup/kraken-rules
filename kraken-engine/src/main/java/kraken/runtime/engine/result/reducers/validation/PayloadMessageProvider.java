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

import kraken.annotations.SPI;
import kraken.runtime.engine.result.ValidationPayloadResult;

/**
 * Provides default messages to validation payload results in case validation rules do not have code and/or message specified
 */
@SPI
public interface PayloadMessageProvider {

    /**
     * @return default message to set for reg exp validation result
     */
    DefaultMessage getRegExpMessage();

    /**
     * @return default message to set for mandatory validation result
     */
    DefaultMessage getUsageMandatoryMessage();

    /**
     * @return default message to set for empty field validation result
     */
    DefaultMessage getUsageEmptyMessage();

    /**
     * @return default message to set for string length validation result
     */
    DefaultMessage getLengthMessage();

    /**
     * @return default message to set for failing assertion rule
     */
    DefaultMessage getAssertionMessage();

    /**
     * @return default message to set for failing collection size rule
     */
    DefaultMessage getCollectionSizeMessage();

    /**
     * @return default message to set for failing collection size range rule
     */
    DefaultMessage getCollectionSizeRangeMessage();

    /**
     * @return default message by provided payload result
     */
    DefaultMessage resolveByPayloadResult(ValidationPayloadResult payloadResult);
}
