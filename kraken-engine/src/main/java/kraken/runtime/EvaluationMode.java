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
package kraken.runtime;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import kraken.annotations.API;
import kraken.model.payload.PayloadType;

/**
 * Enumerates all possible evaluation modes supported by Kraken engine. Evaluation mode
 * dictates what type of rules will be evaluated during a Kraken Engine call.
 *
 * @author Tomas Dapkunas
 * @since 1.36.0
 */
@API
public enum EvaluationMode {

    ALL(PayloadType.values()),

    INQUIRY(PayloadType.VISIBILITY, PayloadType.ACCESSIBILITY),

    PRESENTATIONAL(PayloadType.VISIBILITY, PayloadType.ACCESSIBILITY, PayloadType.DEFAULT, PayloadType.USAGE);

    private final Set<PayloadType> supportedTypes;

    EvaluationMode(PayloadType... supportedTypes) {
        Set<PayloadType> types = new HashSet<>();
        Collections.addAll(types, supportedTypes);

        this.supportedTypes = types;
    }

    /**
     * Performs a check whether this evaluation mode supports given payload type.
     *
     * @param payloadType    Payload type.
     * @return {@code true} if evaluation model support payload type, {@code false} otherwise.
     */
    public boolean isSupported(PayloadType payloadType) {
        return this.supportedTypes.contains(payloadType);
    }

}
