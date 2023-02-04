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

import { PayloadResultType } from 'kraken-engine-api'
import { Payloads } from 'kraken-model'

/**
 * Contains validation error message from result of validation rule evaluation
 */
export class FieldErrorMessage {
    constructor(
        public readonly ruleName: string,
        public readonly message: string,
        public readonly errorCode: string,
        public readonly payloadResultType: PayloadResultType,
        public readonly severity?: Payloads.Validation.ValidationSeverity,
    ) {}
}
