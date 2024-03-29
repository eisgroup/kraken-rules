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
import { ConditionEvaluation, RuleEvaluationResults } from 'kraken-engine-api'
import { Payloads } from 'kraken-model'
import { PayloadBuilder, RulesBuilder } from 'kraken-model-builder'

import { DefaultConditionEvaluationResult } from '../../../../src/dto/DefaultConditionEvaluationResult'
import { DefaultContextFieldInfo } from '../../../../src/dto/DefaultContextFieldInfo'
import { FieldEvaluationResultImpl } from '../../../../src/dto/DefaultFieldEvaluationResult'
import { DefaultRuleInfo } from '../../../../src/engine/results/DefaultRuleInfo'
import { payloadResultCreator } from '../../../../src/engine/results/PayloadResultCreator'
import { mock } from '../../../mock'

import ValidationRuleEvaluationResult = RuleEvaluationResults.ValidationRuleEvaluationResult
export namespace FieldMetadataMocks {
    export const results = {} as Record<string, FieldEvaluationResultImpl>
    results['policy:0:state'] = {
        contextFieldInfo: new DefaultContextFieldInfo(mock.data.dataContextCustom({ state: 'AZ' }), 'state'),
        ruleResults: [
            (() => {
                const lengthOvCritical = PayloadBuilder.lengthLimit().overridableLimit(5)
                const rule = new RulesBuilder()
                    .setName('length-ov-critical')
                    .setPayload(lengthOvCritical)
                    .setTargetPath('state')
                    .setContext('Policy')
                    .build()
                return {
                    kind: RuleEvaluationResults.Kind.VALIDATION,
                    conditionEvaluationResult: DefaultConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
                    ruleInfo: new DefaultRuleInfo(rule),
                    payloadResult: payloadResultCreator.length(lengthOvCritical, false, []),
                    overrideInfo: {
                        overridable: true,
                        overrideApplicable: true,
                        overrideContext: {
                            contextAttributeValue: 'value',
                            contextId: 'id',
                            contextName: 'name',
                            overrideDependencies: {},
                            rootContextId: 'id',
                            rule: new DefaultRuleInfo(rule),
                            ruleEvaluationTimeStamp: new Date(),
                        },
                    },
                } as ValidationRuleEvaluationResult
            })(),
            (() => {
                const assertOvCritical = PayloadBuilder.asserts().overridableThat('false')
                const rule = new RulesBuilder()
                    .setName('assert-ov-critical')
                    .setPayload(assertOvCritical)
                    .setTargetPath('state')
                    .setContext('Policy')
                    .build()
                return {
                    kind: RuleEvaluationResults.Kind.VALIDATION,
                    ruleInfo: new DefaultRuleInfo(rule),
                    conditionEvaluationResult: DefaultConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
                    payloadResult: payloadResultCreator.assertion(assertOvCritical, false, []),
                    overrideInfo: {
                        overridable: true,
                        overrideApplicable: false,
                    },
                } as ValidationRuleEvaluationResult
            })(),
            (() => {
                const assertCritical = PayloadBuilder.asserts().that('false')
                const ruleInfo = new DefaultRuleInfo(
                    new RulesBuilder()
                        .setName('assert-critical')
                        .setPayload(assertCritical)
                        .setTargetPath('state')
                        .setContext('Policy')
                        .build(),
                )
                return {
                    kind: RuleEvaluationResults.Kind.VALIDATION,
                    ruleInfo,
                    payloadResult: payloadResultCreator.assertion(assertCritical, false, []),
                    overrideInfo: {
                        overridable: false,
                        overrideApplicable: false,
                    },
                } as ValidationRuleEvaluationResult
            })(),
            (() => {
                const lengthOvWarning = PayloadBuilder.lengthLimit().overridableLimit(5)
                lengthOvWarning.severity = Payloads.Validation.ValidationSeverity.warning
                return {
                    kind: RuleEvaluationResults.Kind.VALIDATION,
                    conditionEvaluationResult: DefaultConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
                    ruleInfo: new DefaultRuleInfo(
                        new RulesBuilder()
                            .setName('length-ov-warning')
                            .setPayload(lengthOvWarning)
                            .setTargetPath('state')
                            .setContext('Policy')
                            .build(),
                    ),
                    payloadResult: payloadResultCreator.length(lengthOvWarning, false, []),
                    overrideInfo: {
                        overridable: true,
                        overrideApplicable: false,
                    },
                } as ValidationRuleEvaluationResult
            })(),
        ],
    }
    export const mandatoryResults = {} as Record<string, FieldEvaluationResultImpl>
    mandatoryResults['policy:0:state'] = {
        contextFieldInfo: new DefaultContextFieldInfo(mock.data.dataContextCustom({ state: 'AZ' }), 'state'),
        ruleResults: [
            (() => {
                const mandatoryCritical = PayloadBuilder.usage().is(Payloads.Validation.UsageType.mandatory)
                return {
                    kind: RuleEvaluationResults.Kind.VALIDATION,
                    conditionEvaluationResult: DefaultConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
                    ruleInfo: new DefaultRuleInfo(
                        new RulesBuilder()
                            .setName('mandatory-critical-true')
                            .setPayload(mandatoryCritical)
                            .setTargetPath('state')
                            .setContext('Policy')
                            .build(),
                    ),
                    payloadResult: payloadResultCreator.usage(mandatoryCritical, true, []),
                    overrideInfo: {
                        overridable: false,
                        overrideApplicable: false,
                    },
                } as ValidationRuleEvaluationResult
            })(),
            (() => {
                const mandatoryCritical = PayloadBuilder.usage().is(Payloads.Validation.UsageType.mandatory)
                return {
                    kind: RuleEvaluationResults.Kind.VALIDATION,
                    conditionEvaluationResult: DefaultConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
                    ruleInfo: new DefaultRuleInfo(
                        new RulesBuilder()
                            .setName('mandatory-critical-false')
                            .setPayload(mandatoryCritical)
                            .setTargetPath('state')
                            .setContext('Policy')
                            .build(),
                    ),
                    payloadResult: payloadResultCreator.usage(mandatoryCritical, false, []),
                    overrideInfo: {
                        overridable: false,
                        overrideApplicable: false,
                    },
                } as ValidationRuleEvaluationResult
            })(),
        ],
    }
}
