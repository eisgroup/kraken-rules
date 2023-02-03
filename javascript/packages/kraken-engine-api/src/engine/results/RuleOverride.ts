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

/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */

import { Payloads, Contexts, Rule } from 'kraken-model'
import { RuleInfo } from './RuleInfo'

/**
 * RuleOverride is a Kraken feature, that lets override validation failures.
 * Validation failures can occur only on validation rules.
 * Validation can be invalidated or ignored or be overriden.
 * To know was rule overriden or not, from Kraken engine rule evaluation result
 * comes as it is (failed or succeed).
 * Raw results reduced with {@link FieldMetadataReducer} can be interpreted with rule override SPI.
 * ({@link IsRuleOverridden}). This SPI provides all required information {@link OverridableRuleContextInfo}
 * for engine invoker to determine was rule overriden or not.
 * If this SPI returns true, that means - rule was overriden and validation failure can be ignored.
 * Please see the diagram below, that explains this process. (it is be rendered on a static html)
 *
 * @see {@link OverridableRuleContextInfo}
 * @see {@link OverrideDependency}
 * @see {@link IsRuleOverridden}
 * @see {@link Rule}
 *
 * @mermaid Rule Override Sequence diagram
 * sequenceDiagram
 *     participant RR as Rule Repository
 *     participant RE as Rule Engine
 *     participant KR as Kraken Reducer
 *     participant OV as SPI
 *     participant C as Consumer
 *     RR ->> RE: Rules
 *     RE ->> KR: raw results
 *     KR ->> OV: results
 *     OV ->> KR: is overriden
 *     KR ->> C: reduced results
 */
export namespace RuleOverride {
    /**
     * Data that is passed to SPI for {@Link RuleOverride}.IsRuleOverridden
     */
    export interface OverridableRuleContextInfo {
        /**
         * id of context definition instance
         * @type {string}
         * @memberof OverridableRuleContextInfo
         */
        contextId: string

        /**
         * Name Context definition instance, rule was evaluated
         * @type {string}
         * @memberof OverridableRuleContextInfo
         */
        contextName: string
        /**
         * id of the root context definition instance
         * @type {string}
         * @memberof OverridableRuleContextInfo
         */
        rootContextId: string
        /**
         * the value of  a field at the moment of {@link Rule} evaluation.
         * @type {*} value can be any type, that can be present in model
         * @memberof OverridableRuleContextInfo
         */
        contextAttributeValue: unknown
        /**
         * Timestamp of the start of rules evaluation.
         * @type {Date}
         * @memberof OverridableRuleContextInfo
         */
        ruleEvaluationTimeStamp: Date
        /**
         * Validation {@link Rule}, which evaluation have validation failure
         * @type {Rule}
         * @memberof OverridableRuleContextInfo
         */
        rule: RuleInfo
        /**
         * An object with dependencies for override context.
         * @type {Record<string, OverrideDependency>}   key as a string is the same name
         *                                              as in the {@link OverrideDependency#name}
         * @memberof OverridableRuleContextInfo
         */
        overrideDependencies: Record<string, OverrideDependency>
    }

    /**
     * Represents a value that the rule override depends on.
     * Is used in {@link OverridableRuleContextInfo}
     */
    export interface OverrideDependency {
        /**
         * Name of the override dependency os ContextDefinition name "." ContextField name
         * @example "Policy.code"
         * @type {string}
         * @memberof OverrideDependency
         */
        name: string
        /**
         * value of the dependency can be only primitive type
         * @type {Contexts.PrimitiveDataType}
         * @memberof OverrideDependency
         */
        value?: Contexts.KrakenPrimitive
        /**
         * Type of the dependency value
         * @type {Contexts.PrimitiveDataType}
         * @memberof OverrideDependency
         */
        type: Contexts.PrimitiveDataType
    }

    export interface OverrideDependencyInfo {
        contextName: string
        /**
         * field is undefined in case a dependency is only on ContextDefinition with no field reference.
         */
        contextFieldName?: string
    }

    /**
     * Function to decide was rule overridden or not, to ignore errors in case rule is overridden
     */
    export type IsRuleOverridden = (info: OverridableRuleContextInfo) => boolean

    /**
     * Determines is {@link Rule#payload} overridable.
     * @export
     * @param {Rule} rule
     * @returns {boolean}
     */
    export function isOverridable(rule: Rule): boolean {
        return !!(rule.payload as Payloads.Validation.ValidationPayload).isOverridable
    }

    /**
     * Resolves {@link OverrideDependency#name} from rules dependency
     * @export
     * @param {string} contextDefinitionName
     * @param {string} fieldName
     * @returns {string}
     */
    export function resolveOverrideDependencyName(contextDefinitionName: string, fieldName: string): string {
        return contextDefinitionName + '.' + fieldName
    }
}
