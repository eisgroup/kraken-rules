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

import { Payloads } from 'kraken-model'
import PayloadType = Payloads.PayloadType

/**
 * Enumerates all possible evaluation modes supported by Kraken engine.
 */
export enum EvaluationMode {
    /**
     * Indicates that all rule types are supported.
     */
    ALL = 'ALL',
    /**
     * Indicates that only rules having {@link PayloadType.VISIBILITY}
     * and {@link PayloadType.ACCESSIBILITY} are supported.
     */
    INQUIRY = 'INQUIRY',
    /**
     * Indicates that only rules having {@link PayloadType.VISIBILITY},
     * {@link PayloadType.ACCESSIBILITY}, {@link PayloadType.DEFAULT}
     * and {@link PayloadType.USAGE} are supported.
     */
    PRESENTATIONAL = 'PRESENTATIONAL',
}

export function isSupportedPayloadType(evalMode: EvaluationMode, payloadType: PayloadType): boolean {
    return Boolean(modeMasks[evalMode] & payloadTypes[payloadType])
}

const payloadTypes: { [key in PayloadType]: number } = {
    [PayloadType.ACCESSIBILITY]: 1 << 0,
    [PayloadType.ASSERTION]: 1 << 1,
    [PayloadType.DEFAULT]: 1 << 2,
    [PayloadType.LENGTH]: 1 << 3,
    [PayloadType.REGEX]: 1 << 4,
    [PayloadType.SIZE]: 1 << 5,
    [PayloadType.SIZE_RANGE]: 1 << 6,
    [PayloadType.USAGE]: 1 << 7,
    [PayloadType.VISIBILITY]: 1 << 8,
    [PayloadType.NUMBER_SET]: 1 << 9,
    [PayloadType.VALUE_LIST]: 1 << 10,
}

const modeMasks: { [key in EvaluationMode]: number } = {
    [EvaluationMode.ALL]: Object.values(payloadTypes).reduce((first, second) => first | second),
    [EvaluationMode.INQUIRY]: payloadTypes[PayloadType.VISIBILITY] | payloadTypes[PayloadType.ACCESSIBILITY],
    [EvaluationMode.PRESENTATIONAL]:
        payloadTypes[PayloadType.VISIBILITY] |
        payloadTypes[PayloadType.ACCESSIBILITY] |
        payloadTypes[PayloadType.DEFAULT] |
        payloadTypes[PayloadType.USAGE],
}
