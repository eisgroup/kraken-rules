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
package kraken.model.validation;

import kraken.annotations.API;
import kraken.model.payload.PayloadType;

/**
 * Validates max {@link String} length. If length value is 10,
 * then {@link String} to be valid must have 10 or less symbols
 *
 * @author psurinin
 * @since 1.0
 */
@API
public interface LengthPayload extends ValidationPayload {

    int getLength();

    void setLength(int length);

    @Override
    default PayloadType getPayloadType() {
        return PayloadType.LENGTH;
    }
}
