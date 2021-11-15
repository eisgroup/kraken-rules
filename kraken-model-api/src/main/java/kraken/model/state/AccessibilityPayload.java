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
package kraken.model.state;

import kraken.annotations.API;
import kraken.model.Payload;
import kraken.model.payload.PayloadType;

/**
 * Accessibility payload defines field's accessibility state in UI.
 *
 * @author avasiliauskas
 */
@API
public interface AccessibilityPayload extends Payload {

    void setAccessible(Boolean accessible);

    /**
     * If not null, defines if field should be enabled (true), or disabled (false)
     */
    Boolean isAccessible();

    @Override
    default PayloadType getPayloadType() {
        return PayloadType.ACCESSIBILITY;
    }
}
