/*
 *  Copyright 2021 EIS Ltd and/or one of its affiliates.
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

/**
 * Error codes to use in {@link RuntimeError}
 */
export enum ErrorCode {
    NO_BUNDLE_BY_DIMENSIONS = "K001",
    NO_BUNDLE_BY_ENTRYPOINT = "K002",
    NO_RULES_ORDER_ENTRY = "K003",
    UNKNOWN_EXPRESSION_TYPE = "K004",
    UNKNOWN_NAVIGATION_TYPE = "K005",
    ILLEGAL_EXPRESSION_TYPE = "K006",
    EXTRACT_EXPRESSION_FAILED = "K007",
    INCORRECT_MODEL_TREE = "K008",
    INCORRECT_CONTEXT_INSTANCE = "K009",
    MULTIPLE_DEFAULT = "K010",
    UNKNOWN_CONTEXT_DEFINITION = "K011"
}

export class KrakenRuntimeError extends Error {
    constructor(errorCode: ErrorCode, message: string) {
        super(
            `${errorCode} ${message} `);
        Object.setPrototypeOf(this, new.target.prototype);
    }
}
