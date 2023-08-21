/* eslint-disable @typescript-eslint/no-explicit-any */

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

import { Moneys } from '../engine/runtime/expressions/math/Moneys'

export type Message = {
    code: string
    messageTemplate: string
}

export const NO_BUNDLE_CACHE_BY_ENTRYPOINT_AND_DIMENSIONS = {
    code: 'kus001',
    messageTemplate: `Cannot find bundle for entrypoint '{0}' by dimensions {1} in cache.`,
}
export const NO_BUNDLE_CACHE_BY_ENTRYPOINT = {
    code: 'kus002',
    messageTemplate: `Cannot find bundle for entrypoint '{0}' in cache.`,
}
export const NO_BUNDLE_CACHE_BY_DIMENSIONS = {
    code: 'kus003',
    messageTemplate: `Cannot find bundle by dimensions {0} in cache.`,
}
export const CONTEXT_MODEL_TREE_MISSING_EXTRACTION_ROOT = {
    code: 'kus004',
    messageTemplate: `Failed to find extraction root from {0} in path {1}.`,
}
export const CONTEXT_MODEL_TREE_MISSING_EXTRACTION_PATH = {
    code: 'kus005',
    messageTemplate: `Could not find any extraction paths from {0} to {1}.`,
}
export const CONTEXT_MODEL_TREE_UNDEFINED_CHILD = {
    code: 'kus006',
    messageTemplate: `Argument 'childContextName' must be defined.`,
}
export const CONTEXT_MODEL_TREE_UNDEFINED_TARGET = {
    code: 'kus007',
    messageTemplate: `Reference target paths cannot be empty.`,
}
export const CONTEXT_MODEL_TREE_MULTIPLE_TARGET = {
    code: 'kus008',
    messageTemplate: `Cannot determine path to reference, resolved {0}.`,
}
export const CONTEXT_MODEL_TREE_MISSING_FIELD = {
    code: 'kus009',
    messageTemplate: `Field projection for a data context instance is missing field: '{0}'`,
}
export const RESTRICTION_NODE_NOT_VALID_CONTEXT = {
    code: 'kus010',
    messageTemplate: `Restriction node is not a valid context instance. Errors: {0}`,
}
export const DEFAULT_RULE_MULTIPLE_ON_SAME_FIELD = {
    code: 'kus011',
    messageTemplate:
        `Field '{0}' has more than one applicable default rule: {1}. ` +
        `Only one default rule can be applied on the same field.`,
}
export const UNKNOWN_CONTEXT_DEFINITION = {
    code: 'kus012',
    messageTemplate: `Context definition with name {0} does not exist in the kraken model tree.`,
}
export const NO_EXPRESSION_CONTEXT = {
    code: 'kus013',
    messageTemplate: `Expression context is missing.`,
}
export const CONTEXT_BUILD_INVALID_DATA = {
    code: 'kus014',
    messageTemplate: `Failed to build data context from object of type: {0}. Reason: {1}.`,
}
export const CONTEXT_BUILD_UNDEFINED_DATA = {
    code: 'kus015',
    messageTemplate: `Context data object is undefined.`,
}
export const DEFAULT_VALUE_PAYLOAD_INCOMPATIBLE_VALUE = {
    code: 'kus016',
    messageTemplate:
        "Cannot apply value '{0} (typeof {1})' on '{2}.{3}' " +
        "because value type is not assignable to field type '{4}'. " +
        'Rule will be silently ignored.',
}
export const VALUE_LIST_PAYLOAD_CANNOT_CONVERT_TO_STRING = {
    code: 'kus017',
    messageTemplate: 'Unable to convert value list item value {0} to a string.',
}
export const VALUE_LIST_PAYLOAD_CANNOT_CONVERT_TO_NUMBER = {
    code: 'kus018',
    messageTemplate: 'Unable to convert value list item value {0} to a number.',
}
export const UNKNOWN_EXPRESSION_TYPE = {
    code: 'kus019',
    messageTemplate: 'Unknown expression type encountered: {0}.',
}

export type SystemMessage = {
    code: string
    message: string
}

export class SystemMessageBuilder {
    private message: Message

    private params: any[] | undefined

    constructor(message: Message) {
        this.message = message
    }

    public parameters(...params: any[]): this {
        this.params = params
        return this
    }

    public build(): SystemMessage {
        return {
            code: this.message.code,
            message: this.formatMessage(),
        }
    }

    private formatMessage(): string {
        return this.message.messageTemplate.replace(/{(\d+)}/g, (_, index) => this.formatValue(this.params?.[index]))
    }

    private formatValue(value: any): string {
        if (value == undefined) {
            return 'undefined'
        }
        if (value instanceof Date) {
            return value.toISOString()
        }
        if (Moneys.isMoney(value)) {
            return `${value.currency} ${value.amount}`
        }
        if (Array.isArray(value)) {
            return '[' + value.map(this.formatValue).join(', ') + ']'
        }
        if (typeof value === 'object') {
            return JSON.stringify(value)
        }
        return String(value)
    }
}

export function formatCodeWithMessage(m: SystemMessage): string {
    return `[${m.code}] ${m.message}`
}

export class KrakenRuntimeError extends Error {
    constructor(systemMessage: SystemMessage) {
        super(formatCodeWithMessage(systemMessage))
        Object.setPrototypeOf(this, new.target.prototype)
    }
}
