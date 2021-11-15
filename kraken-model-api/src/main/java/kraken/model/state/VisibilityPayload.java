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
 * Visibility payload defines field's visibility state in UI.
 *
 * @author avasiliauskas
 */
@API
public interface VisibilityPayload extends Payload {

    void setVisible(Boolean visible);

    /**
     * If not null, defines if field should be visible (true), or hidden (false)
     */
    Boolean isVisible();

    @Override
    default PayloadType getPayloadType() {
        return PayloadType.VISIBILITY;
    }
}
