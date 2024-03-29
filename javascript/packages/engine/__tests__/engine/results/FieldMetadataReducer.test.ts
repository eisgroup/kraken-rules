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
import { Reducer } from 'declarative-js'
import {
    AccessibilityPayloadResult,
    ConditionEvaluation,
    DefaultValuePayloadResult,
    EntryPointResult,
    FieldMetadata,
    PayloadResultType,
    RuleEvaluationResults,
    RuleOverride,
    VisibilityPayloadResult,
} from 'kraken-engine-api'
import { Payloads } from 'kraken-model'
import { PayloadBuilder, RulesBuilder } from 'kraken-model-builder'

import { DefaultConditionEvaluationResult } from '../../../src/dto/DefaultConditionEvaluationResult'
import { DefaultContextFieldInfo } from '../../../src/dto/DefaultContextFieldInfo'
import { DefaultEntryPointResult } from '../../../src/dto/DefaultEntryPointResult'
import { FieldEvaluationResultImpl } from '../../../src/dto/DefaultFieldEvaluationResult'
import { DefaultRuleInfo } from '../../../src/engine/results/DefaultRuleInfo'
import { FieldMetadataReducer } from '../../../src/engine/results/field_metadata_reducer/FieldMetadataReducer'
import { Localization } from '../../../src/engine/results/Localization'
import { payloadResultCreator } from '../../../src/engine/results/PayloadResultCreator'
import { payloadResultTypeChecker } from '../../../src/engine/results/PayloadResultTypeChecker'
import { ValueChangedEvent } from '../../../src/engine/results/ValueChangedEvent'
import { mock } from '../../mock'
import { FieldMetadataMocks } from './mocks/FieldMetadataReducer.mocks'

const { Policy } = mock.modelTreeJson.contexts
let reducedResults: Record<string, FieldMetadata>
let entryPointResults: EntryPointResult
const rule = (targetPath: string) =>
    RulesBuilder.create()
        .setContext('mock')
        .setTargetPath(targetPath)
        .setPayload(PayloadBuilder.lengthLimit().overridableLimit(0))
        .setName('mock')
        .build()
beforeEach(() => {
    const results = {} as Record<string, FieldEvaluationResultImpl>
    results['present1'] = new FieldEvaluationResultImpl(
        new DefaultContextFieldInfo(mock.data.dataContextCustom({ state: 'AZ' }), Policy.fields.state.name),
        [
            {
                kind: RuleEvaluationResults.Kind.REGULAR,
                conditionEvaluationResult: DefaultConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
                payloadResult: {
                    type: PayloadResultType.ACCESSIBILITY,
                    accessible: false,
                } as AccessibilityPayloadResult,
                ruleInfo: new DefaultRuleInfo(rule(Policy.fields.state.name)),
            },
        ],
    )
    results['present2'] = new FieldEvaluationResultImpl(
        new DefaultContextFieldInfo(
            mock.data.dataContextCustom({ termDetails: { termCd: 'hu' } }),
            Policy.fields.termCd.name,
        ),
        [
            {
                kind: RuleEvaluationResults.Kind.REGULAR,
                conditionEvaluationResult: DefaultConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
                payloadResult: {
                    type: PayloadResultType.VISIBILITY,
                    visible: false,
                } as VisibilityPayloadResult,
                ruleInfo: new DefaultRuleInfo(rule(Policy.fields.termCd.name)),
            },
        ],
    )
    const valueChangedEvent = new ValueChangedEvent('a.b', 'name2', '1', 'new', 'old')
    results['2'] = new FieldEvaluationResultImpl(
        new DefaultContextFieldInfo(
            mock.data.dataContextCustom({ transactionDetails: { txType: 'j' } }),
            Policy.fields.txType.name,
        ),
        [
            {
                kind: RuleEvaluationResults.Kind.REGULAR,
                conditionEvaluationResult: DefaultConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
                payloadResult: {
                    type: PayloadResultType.DEFAULT,
                    events: [valueChangedEvent],
                } as DefaultValuePayloadResult,
                ruleInfo: new DefaultRuleInfo(rule(Policy.fields.txType.name)),
            },
        ],
    )
    results['4'] = new FieldEvaluationResultImpl(
        new DefaultContextFieldInfo(
            mock.data.dataContextCustom({ termDetails: { termNo: 6 } }),
            Policy.fields.termNo.name,
        ),
        [
            {
                kind: RuleEvaluationResults.Kind.REGULAR,
                conditionEvaluationResult: DefaultConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
                payloadResult: {
                    type: PayloadResultType.DEFAULT,
                    events: [valueChangedEvent],
                } as DefaultValuePayloadResult,
                ruleInfo: new DefaultRuleInfo(rule(Policy.fields.termNo.name)),
            },
        ],
    )
    results['3'] = new FieldEvaluationResultImpl(
        new DefaultContextFieldInfo(
            mock.data.dataContextCustom({ accessTrackInfo: { updatedBy: 'override' } }),
            Policy.fields.updatedBy.name,
        ),
        [
            {
                kind: RuleEvaluationResults.Kind.VALIDATION,
                conditionEvaluationResult: DefaultConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
                payloadResult: payloadResultCreator.assertion(PayloadBuilder.asserts().that(''), false, []),
                ruleInfo: new DefaultRuleInfo(rule(Policy.fields.termNo.name)),
                overrideInfo: {
                    overridable: true,
                    overrideApplicable: true,
                    overrideContext: {
                        contextAttributeValue: 'override',
                        contextId: '',
                        contextName: '',
                        overrideDependencies: {},
                        rootContextId: '',
                        rule: new DefaultRuleInfo(rule(Policy.fields.updatedBy.name)),
                        ruleEvaluationTimeStamp: new Date(),
                    },
                },
            },
        ],
    )
    entryPointResults = new DefaultEntryPointResult(results)
    reducedResults = new FieldMetadataReducer().reduce(entryPointResults)
})
describe('FieldMetadataReducer', () => {
    it('should contain 2 field metadatas', () => {
        expect(Object.values(reducedResults)).toHaveLength(5)
        expect(reducedResults).toMatchSnapshot()
    })
    it('should count hidden fields', () => {
        expect(Object.values(reducedResults).filter(field => field.isHidden)).toHaveLength(1)
    })
    it('should count disabled fields', () => {
        expect(Object.values(reducedResults).filter(field => field.isDisabled)).toHaveLength(1)
    })
    it('should find overridden rule', () => {
        function isOverridden(c: RuleOverride.OverridableRuleContextInfo): boolean {
            return c.contextAttributeValue === 'override'
        }
        const reducer = new FieldMetadataReducer(isOverridden, undefined, Localization.defaultValidationMessages)
        const res = reducer.reduce(entryPointResults)
        expect(res['3'].ruleResults[0].isOverridden).toBeTruthy()
    })
    it('should return results for one field with overridden values', () => {
        const reducer = new FieldMetadataReducer(p => p.rule.ruleName === 'length-ov-critical')
        const fm = reducer.reduce(new DefaultEntryPointResult(FieldMetadataMocks.results))
        expect(Object.values(fm)).toHaveLength(1)
        const fmResults = Object.values(fm)[0].ruleResults
        expect(fmResults).toHaveLength(4)
        const fmResultsByRuleName = fmResults.reduce(
            Reducer.toMap(x => x.ruleName),
            Reducer.Map(),
        )
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        const overriddenFailed = fmResultsByRuleName.get('length-ov-critical')!
        expect(overriddenFailed.isFailed).toBeTruthy()
        expect(overriddenFailed.isOverridable).toBeTruthy()
        expect(overriddenFailed.isOverridden).toBeTruthy()
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        const overriddenAssertFailed = fmResultsByRuleName.get('assert-ov-critical')!
        expect(overriddenAssertFailed.isFailed).toBeTruthy()
        expect(overriddenAssertFailed.isOverridable).toBeTruthy()
        expect(overriddenAssertFailed.isOverridden).toBeFalsy()
        expect(fm).toMatchSnapshot()
    })
    it('should reduce mandatory results', () => {
        const reduced = new FieldMetadataReducer().reduce(
            new DefaultEntryPointResult(FieldMetadataMocks.mandatoryResults),
        )
        expect(Object.values(reduced)).toHaveLength(1)
        const fmResults = Object.values(reduced)[0].ruleResults
        expect(fmResults).toHaveLength(2)
        expect(fmResults).toMatchSnapshot()
    })
    it('should set undefined to isFailed field if validation was ignored', () => {
        const reducer = new FieldMetadataReducer()
        const reduced = reducer.reduce(
            new DefaultEntryPointResult({
                'Policy:1:state': {
                    contextFieldInfo: {
                        contextId: '1',
                        contextName: 'Policy',
                        fieldName: 'state',
                        fieldPath: 'state',
                        fieldType: 'STRING',
                    },
                    ruleResults: [
                        {
                            kind: RuleEvaluationResults.Kind.VALIDATION,
                            conditionEvaluationResult: DefaultConditionEvaluationResult.of(
                                ConditionEvaluation.APPLICABLE,
                            ),
                            overrideInfo: {
                                overridable: false,
                                overrideApplicable: false,
                            },
                            payloadResult: payloadResultCreator.assertionFail({
                                kind: 2,
                                error: {
                                    message: 'test',
                                    severity: 'info',
                                },
                            }),
                            ruleInfo: {
                                context: 'Policy',
                                payloadtype: Payloads.PayloadType.ASSERTION,
                                ruleName: 'test',
                                targetPath: 'state',
                            },
                        },
                    ],
                },
            }),
        )
        expect(reduced['Policy:1:state'].ruleResults[0].isFailed).toBeUndefined()
    })
    it('should set set mustBeEmpty failure as a rule result', () => {
        const reducer = new FieldMetadataReducer()
        const reduced = reducer.reduce(
            new DefaultEntryPointResult({
                'Policy:1:state': {
                    contextFieldInfo: {
                        contextId: '1',
                        contextName: 'Policy',
                        fieldName: 'state',
                        fieldPath: 'state',
                        fieldType: 'STRING',
                    },
                    ruleResults: [
                        {
                            kind: RuleEvaluationResults.Kind.VALIDATION,
                            conditionEvaluationResult: DefaultConditionEvaluationResult.of(
                                ConditionEvaluation.APPLICABLE,
                            ),
                            overrideInfo: {
                                overridable: false,
                                overrideApplicable: false,
                            },
                            payloadResult: payloadResultCreator.usage(
                                PayloadBuilder.usage().is(Payloads.Validation.UsageType.mustBeEmpty),
                                false,
                                [],
                            ),
                            ruleInfo: {
                                context: 'Policy',
                                payloadtype: Payloads.PayloadType.USAGE,
                                ruleName: 'test',
                                targetPath: 'state',
                            },
                        },
                    ],
                },
            }),
        )
        const ruleResult = reduced['Policy:1:state'].ruleResults[0]
        expect(ruleResult.ruleName).toBe('test')
        expect(ruleResult.payloadResult.type).toBe(PayloadResultType.USAGE)
        expect(ruleResult.isFailed).toBe(true)
        expect(payloadResultTypeChecker.isEmpty(ruleResult.payloadResult)).toBe(true)
    })
})
