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

import { Payloads } from "kraken-model";
import PayloadType = Payloads.PayloadType;
import ValidationPayload = Payloads.Validation.ValidationPayload;
import AccessibilityPayload = Payloads.UI.AccessibilityPayload;
import VisibilityPayload = Payloads.UI.VisibilityPayload;
import DefaultValuePayload = Payloads.Derive.DefaultValuePayload;
import DefaultingType = Payloads.Derive.DefaultingType;
import ValidationSeverity = Payloads.Validation.ValidationSeverity;

export const VALIDATION_PAYLOAD: ValidationPayload = {
    type: PayloadType.ASSERTION,
    errorMessage: { errorCode: "mock-code", templateParts: ["Error"], templateExpressions: [] },
    severity: ValidationSeverity.critical
};

export const ACCESSIBILITY_PAYLOAD: AccessibilityPayload = {
    type: PayloadType.ACCESSIBILITY,
    accessible: true
};

export const VISIBILITY_PAYLOAD: VisibilityPayload = {
    type: PayloadType.VISIBILITY,
    visible: true
};

export const DEFAULT_VALUE_PAYLOAD: DefaultValuePayload = {
    type: PayloadType.DEFAULT,
    defaultingType: DefaultingType.defaultValue,
    valueExpression: {
        expressionType: "LITERAL",
        compiledLiteralValue: "value",
        compiledLiteralValueType: "String"
    }
};
