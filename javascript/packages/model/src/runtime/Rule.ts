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

import { Dependency } from './Dependency'
import { Metadata } from './Metadata'
import { Payloads } from './Payloads'
import { Condition } from './Condition'

export interface Rule {
    name: string
    context: string
    targetPath: string
    condition?: Condition
    payload: Payloads.Payload
    description?: string
    dependencies?: Dependency[]
    metadata?: Metadata
    dimensionSet: DimensionSet
    priority?: number
}

export type DimensionSet = UnknownDimensionSet | KnownDimensionSet

export interface UnknownDimensionSet {
    variability: 'UNKNOWN'
}

export interface KnownDimensionSet {
    /**
     * A set of a dimension names by which rule is varied.
     * If this property is `undefined`, then rule is varied
     * by all dimensions.
     */
    dimensions: string[]
    variability: 'STATIC' | 'KNOWN'
}

export function isKnownDimensionSet(dimensionSet: DimensionSet): dimensionSet is KnownDimensionSet {
    return dimensionSet.variability !== 'UNKNOWN'
}
