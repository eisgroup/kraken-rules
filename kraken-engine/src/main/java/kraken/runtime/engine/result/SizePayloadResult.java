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
package kraken.runtime.engine.result;

import java.util.List;

import kraken.annotations.API;
import kraken.model.validation.SizeOrientation;
import kraken.runtime.model.rule.payload.validation.SizePayload;

/**
 * @author psurinin
 */
@API
public class SizePayloadResult extends ValidationPayloadResult {

    private int size;

    private SizeOrientation sizeOrientation;

    /**
     * @deprecated use {@link #SizePayloadResult(Boolean, SizePayload, List)} instead.
     */
    @Deprecated(since = "1.14.0", forRemoval = true)
    public SizePayloadResult(Boolean success, SizePayload payload) {
        this(success, payload, List.of());
    }

    public SizePayloadResult(Boolean success, SizePayload payload, List<String> templateVariables) {
        super(success, payload, templateVariables);

        this.size = payload.getSize();
        this.sizeOrientation = payload.getOrientation();
    }

    public int getSize() {
        return size;
    }

    public SizeOrientation getSizeOrientation() {
        return sizeOrientation;
    }
}
