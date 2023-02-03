/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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

import { Expressions } from './Expressions'

/**
 * Models data context object on which rules are defined and executed.
 * Each context definition is identified by unique name.
 */

export namespace Contexts {
    export interface ContextDefinition {
        name: string
        children?: Record<string, ContextNavigation>
        fields?: Record<string, ContextField>
        inheritedContexts: string[]
    }

    /**
     * Models a field attribute on {@link ContextDefinition}. Field attributes can be used to
     * define rules on or referenced in rules expressions.
     *
     * Context fields are defined only for strict data context definitions.
     */
    export interface ContextField {
        name: string
        cardinality: Cardinality
        fieldType: string
        fieldPath: string
    }

    export interface ContextNavigation {
        targetName: string
        navigationExpression: Expressions.PathExpression | Expressions.ComplexExpression
        cardinality: Cardinality
    }

    export type Cardinality = 'SINGLE' | 'MULTIPLE'

    export type MoneyType = { amount: number; currency: string }
    export type KrakenPrimitive = number | string | boolean | Date | MoneyType
    export type PrimitiveDataType =
        | 'INTEGER'
        | 'DECIMAL'
        | 'STRING'
        | 'BOOLEAN'
        | 'DATE'
        | 'DATETIME'
        | 'UUID'
        | 'MONEY'

    export type SystemDataType = 'UNKNOWN'

    const systemDataType: SystemDataType = 'UNKNOWN'

    const primitiveDataTypes = new Set<PrimitiveDataType>([
        'BOOLEAN',
        'DATE',
        'DATETIME',
        'DECIMAL',
        'INTEGER',
        'MONEY',
        'STRING',
        'UUID',
    ])

    export const fieldTypeChecker = {
        isPrimitive(type: string): type is PrimitiveDataType {
            return primitiveDataTypes.has(type as PrimitiveDataType)
        },
        isSystem(type: string): type is SystemDataType {
            return systemDataType === type
        },
        isUnknownType(type: string): type is 'UNKNOWN' {
            return type === 'UNKNOWN'
        },
        isMoney(type: string): type is 'MONEY' {
            return type === 'MONEY'
        },
    }
}
