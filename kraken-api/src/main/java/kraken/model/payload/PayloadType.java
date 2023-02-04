/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.model.payload;

import kraken.annotations.API;

/**
 * Enumerates all Kraken Rule Payload types supported by the Kraken Engine.
 *
 * @author mulevicius
 */
@API
public enum PayloadType {

    ASSERTION("AssertionPayload", EvaluationType.VALIDATION),
    DEFAULT("DefaultValuePayload", EvaluationType.DEFAULT),
    USAGE("UsagePayload", EvaluationType.VALIDATION),
    REGEX("RegExpPayload", EvaluationType.VALIDATION),
    ACCESSIBILITY("AccessibilityPayload", EvaluationType.ACCESSIBILITY),
    VISIBILITY("VisibilityPayload", EvaluationType.VISIBILITY),
    LENGTH("LengthPayload", EvaluationType.VALIDATION),
    SIZE("SizePayload", EvaluationType.VALIDATION),
    SIZE_RANGE("SizeRangePayload", EvaluationType.VALIDATION),
    NUMBER_SET("NumberSetPayload", EvaluationType.VALIDATION),
    VALUE_LIST("ValueListPayload", EvaluationType.VALIDATION);

    private String typeName;

    private EvaluationType evaluationType;

    PayloadType(String typeName, EvaluationType evaluationType) {
        this.typeName = typeName;
        this.evaluationType = evaluationType;
    }

    /**
     *
     * @return name of the payload type
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     *
     * @return an evaluation result type that this type of payload will produce
     */
    public EvaluationType getEvaluationType() {
        return evaluationType;
    }
}
