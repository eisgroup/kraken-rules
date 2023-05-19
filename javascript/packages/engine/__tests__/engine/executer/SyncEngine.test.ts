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

import { SyncEngine } from '../../../src/engine/executer/SyncEngine'
import { mock } from '../../mock'
import { registry } from '../../../src/engine/runtime/expressions/ExpressionEvaluator'
import { PayloadBuilder, RulesBuilder } from 'kraken-model-builder'
import { AssertionPayloadResult, RuleEvaluationResults } from 'kraken-engine-api'
import { Payloads } from 'kraken-model'
import { EvaluationMode } from '../../../src/engine/runtime/EvaluationMode'
import { DimensionSetBundleCache } from '../../../src/bundle-cache/dimension-set-cache/DimensionSetBundleCache'
import { EntryPointBundle, ExpressionContextManagerImpl } from '../../../src'
import UsageType = Payloads.Validation.UsageType
import PayloadType = Payloads.PayloadType

let engine: SyncEngine

const customFunctionName = 'Log'
const customFunction = jest.fn()
const Policy = mock.modelTreeJson.contexts.Policy
const CreditCardInfo = mock.modelTreeJson.contexts.CreditCardInfo

beforeEach(() => {
    const cache = new DimensionSetBundleCache({ logWarning: () => void 0 }, new ExpressionContextManagerImpl())
    cache.setExpressionContext({})

    function addBundleForDefaultDimension(bundleEvaluation: EntryPointBundle.EntryPointEvaluation) {
        cache.add(
            bundleEvaluation.entryPointName,
            {},
            { evaluation: bundleEvaluation, engineVersion: '1', expressionContext: {} },
        )
    }
    addBundleForDefaultDimension({
        delta: false,
        rules: [],
        entryPointName: 'CreditCardInfo',
        fieldOrder: [],
    })
    addBundleForDefaultDimension({
        delta: false,
        fieldOrder: [],
        rules: [
            new RulesBuilder()
                .setContext(CreditCardInfo.name)
                .setName('r01')
                .setTargetPath(CreditCardInfo.fields.cardNumber.name)
                .setPayload(PayloadBuilder.accessibility().notAccessible())
                .build(),
            new RulesBuilder()
                .setContext(CreditCardInfo.name)
                .setName('r02')
                .setTargetPath(CreditCardInfo.fields.cardNumber.name)
                .setPayload(PayloadBuilder.visibility().notVisible())
                .build(),
        ],
        entryPointName: 'CreditCardInfo',
    })
    addBundleForDefaultDimension({
        delta: false,
        fieldOrder: [`${Policy.name}.${Policy.fields.policyNumber.name}`],
        rules: [
            new RulesBuilder()
                .setContext(Policy.name)
                .setName('Eval_Accessibility')
                .setTargetPath(Policy.fields.policyNumber.name)
                .setPayload(PayloadBuilder.accessibility().notAccessible())
                .build(),
            new RulesBuilder()
                .setContext(Policy.name)
                .setName('Eval_Visibility')
                .setTargetPath(Policy.fields.policyNumber.name)
                .setPayload(PayloadBuilder.visibility().notVisible())
                .build(),
            new RulesBuilder()
                .setContext(Policy.name)
                .setName('Eval_Usage')
                .setTargetPath(Policy.fields.policyNumber.name)
                .setPayload(PayloadBuilder.usage().is(UsageType.mandatory))
                .build(),
            new RulesBuilder()
                .setContext(Policy.name)
                .setName('Eval_Default')
                .setTargetPath(Policy.fields.policyNumber.name)
                .setPayload(PayloadBuilder.default().to('"P0001"'))
                .build(),
            new RulesBuilder()
                .setContext(Policy.name)
                .setName('Eval_Assert')
                .setTargetPath(Policy.fields.policyNumber.name)
                .setPayload(PayloadBuilder.asserts().that('__dataObject__.policyNumber == "P0001"'))
                .build(),
            new RulesBuilder()
                .setContext(Policy.name)
                .setName('Eval_RegExp')
                .setTargetPath(Policy.fields.policyNumber.name)
                .setPayload(PayloadBuilder.regExp().match('[A-Z]+'))
                .build(),
            new RulesBuilder()
                .setContext(Policy.name)
                .setName('Eval_Length')
                .setTargetPath(Policy.fields.policyNumber.name)
                .setPayload(PayloadBuilder.lengthLimit().limit(5))
                .build(),
            new RulesBuilder()
                .setContext(Policy.name)
                .setName('Eval_Size')
                .setTargetPath(Policy.fields.riskItems.name)
                .setPayload(PayloadBuilder.size().min(0))
                .build(),
            new RulesBuilder()
                .setContext(Policy.name)
                .setName('Eval_SizeRange')
                .setTargetPath(Policy.fields.riskItems.name)
                .setPayload(PayloadBuilder.size().range(0, 1))
                .build(),
            new RulesBuilder()
                .setContext(Policy.name)
                .setName('Eval_NumberSet')
                .setTargetPath(Policy.fields.termNo.name)
                .setPayload(PayloadBuilder.numberSet().greaterThanOrEqualTo(1, 1))
                .build(),
            new RulesBuilder()
                .setContext(Policy.name)
                .setName('Eval_ValueList')
                .setTargetPath(Policy.fields.contractTermTypeCd.name)
                .setPayload(
                    PayloadBuilder.valueList().valueList({
                        values: ['MONTHLY', 'ANNUAL'],
                        valueType: 'STRING',
                    }),
                )
                .build(),
        ],
        entryPointName: 'EvalOptionsTest',
    })
    addBundleForDefaultDimension({
        delta: false,
        fieldOrder: [],
        entryPointName: 'CustomFunction',
        rules: [
            new RulesBuilder()
                .setContext(Policy.name)
                .setName('customFunction')
                .setTargetPath(Policy.fields.state.name)
                .setPayload(PayloadBuilder.asserts().that('this.' + customFunctionName + '()'))
                .build(),
        ],
    })
    const instanceExpression =
        "this._t(__dataObject__, 'Policy') " +
        "&& this._i(__dataObject__, 'Policy') " +
        "&& this.GetType(__dataObject__) == 'Policy'"
    addBundleForDefaultDimension({
        delta: false,
        entryPointName: 'InstanceFunctions',
        rules: [
            new RulesBuilder()
                .setContext(Policy.name)
                .setName('R-InstanceFunctions')
                .setTargetPath(Policy.fields.state.name)
                .setPayload(PayloadBuilder.asserts().that(instanceExpression))
                .build(),
        ],
        fieldOrder: [],
    })
    addBundleForDefaultDimension({
        delta: false,
        fieldOrder: [],
        entryPointName: 'ExternalContext',
        rules: [
            new RulesBuilder()
                .setContext(Policy.name)
                .setName('ExternalContext')
                .setTargetPath(Policy.fields.state.name)
                .setPayload(PayloadBuilder.asserts().that("context.external.key == 'value'"))
                .build(),
        ],
    })
    addBundleForDefaultDimension({
        delta: false,
        fieldOrder: [],
        entryPointName: 'function rebuilding',
        rules: [
            new RulesBuilder()
                .setContext(Policy.name)
                .setName('function rebuilding')
                .setTargetPath(Policy.fields.state.name)
                .setPayload(PayloadBuilder.asserts().that('this.custom()'))
                .build(),
        ],
    })
    addBundleForDefaultDimension({
        delta: false,
        fieldOrder: [],
        entryPointName: 'Empty',
        rules: [],
    })
    engine = new SyncEngine({
        cache: cache,
        dataInfoResolver: mock.spi.dataResolver,
        contextInstanceInfoResolver: mock.spi.instance,
        modelTree: mock.modelTree,
        functions: mock.policyFunctions,
    })
})

describe('SyncEngine', () => {
    it('should evaluate rule with external data', () => {
        const results = engine.evaluate(mock.data.empty(), 'ExternalContext', {
            currencyCd: 'USD',
            context: {
                externalData: {
                    key: 'value',
                },
            },
        })
        const payloadResult = (results.getAllRuleResults()[0] as RuleEvaluationResults.ValidationRuleEvaluationResult)
            .payloadResult as AssertionPayloadResult
        expect(payloadResult.error).toBeUndefined()
        expect(payloadResult.success).toBeTruthy()
    })
    it('should evaluate instance functions', () => {
        const results = engine.evaluate(mock.data.empty(), 'InstanceFunctions', mock.evaluationConfig)
        expect(results).k_toHaveExpressionsFailures(0)
    })
    it('should add custom function to registry and execute it', () => {
        registry.add({
            name: customFunctionName,
            function: customFunction,
        })
        engine.evaluate(mock.data.empty(), 'CustomFunction', mock.evaluationConfig)
        expect(customFunction).toHaveBeenCalledTimes(1)
    })
    it('should throw an error when no currency code is provided', () => {
        expect(() => engine.evaluate({}, '', { currencyCd: '', context: {} })).toThrow()
    })
    it('should when restriction is invalid Context instance', () => {
        expect(() => engine.evaluateSubTree({}, { cd: '', noid: 0 }, '', mock.evaluationConfig)).toThrow(
            'Restriction node is invalid Context instance',
        )
    })
    it('should evaluate with empty entryPoint bundle', () => {
        const result = engine.evaluate(mock.data.empty(), 'Empty', mock.evaluationConfig)
        expect(result.getAllRuleResults().length).toBe(0)
        expect(Object.keys(result.getFieldResults()).length).toBe(0)
    })
    it('should evaluate 2 rules on same context', () => {
        const data = mock.data.dataContextCustom({
            billingInfo: {
                creditCardInfo: {
                    cd: mock.modelTreeJson.contexts.CreditCardInfo.name,
                    id: 'cci1',
                },
            },
        })
        const results = engine.evaluate(data.dataObject, 'CreditCardInfo', mock.evaluationConfig)
        expect(Object.keys(results.getFieldResults())).toHaveLength(1)
        const id = `${CreditCardInfo.name}:cci1:${CreditCardInfo.fields.cardNumber.name}`
        expect(results.getFieldResults()[id]).toBeDefined()
        expect(results.getFieldResults()[id].ruleResults).toHaveLength(2)
    })
    // run this test when console.setup.js configuration is disabled
    // this feature can be tested only with logs ;\
    it('evaluation id usage', () => {
        const data = mock.data.dataContextCustom({
            billingInfo: {
                creditCardInfo: {
                    cd: mock.modelTreeJson.contexts.CreditCardInfo.name,
                    id: 'cci1',
                },
            },
        })
        engine.evaluate(data.dataObject, 'CreditCardInfo', { ...mock.evaluationConfig, evaluationId: '11' })
        engine.evaluate(data.dataObject, 'CreditCardInfo', { ...mock.evaluationConfig, evaluationId: '11' })
        engine.evaluate(data.dataObject, 'CreditCardInfo', { ...mock.evaluationConfig, evaluationId: '12' })
        engine.evaluate(data.dataObject, 'CreditCardInfo', { ...mock.evaluationConfig, evaluationId: '12' })
        engine.evaluate(data.dataObject, 'CreditCardInfo', { ...mock.evaluationConfig })
        engine.evaluate(data.dataObject, 'CreditCardInfo', { ...mock.evaluationConfig, evaluationId: '11' })
        engine.evaluate(data.dataObject, 'CreditCardInfo', { ...mock.evaluationConfig, evaluationId: '11' })
    })

    it('Should only evaluate rules applicable for inquiry evaluation mode', () => {
        const data = mock.data.dataContextCustom({
            riskItems: [
                {
                    model: 'WV',
                },
            ],
        })

        const epResult = engine.evaluate(data.dataObject, 'EvalOptionsTest', {
            ...mock.evaluationConfig,
            evaluationMode: EvaluationMode.INQUIRY,
        })

        const resultTypes = epResult.getApplicableResults().map(ruleResult => ruleResult.ruleInfo.payloadtype)

        expect(resultTypes).toHaveLength(2)
        expect(resultTypes).toEqual(expect.arrayContaining([PayloadType.ACCESSIBILITY, PayloadType.VISIBILITY]))
    })

    it('Should only evaluate rules applicable for presentational evaluation mode', () => {
        const data = mock.data.dataContextCustom({
            riskItems: [
                {
                    model: 'WV',
                },
            ],
        })

        const epResult = engine.evaluate(data.dataObject, 'EvalOptionsTest', {
            ...mock.evaluationConfig,
            evaluationMode: EvaluationMode.PRESENTATIONAL,
        })

        const resultTypes = epResult.getApplicableResults().map(ruleResult => ruleResult.ruleInfo.payloadtype)

        expect(resultTypes).toHaveLength(6)
        expect(resultTypes).toEqual(
            expect.arrayContaining([
                PayloadType.ACCESSIBILITY,
                PayloadType.VISIBILITY,
                PayloadType.DEFAULT,
                PayloadType.USAGE,
                PayloadType.SIZE,
                PayloadType.SIZE_RANGE,
            ]),
        )
    })

    it('Should evaluate all rules by default', () => {
        const data = mock.data.dataContextCustom({
            riskItems: [
                {
                    model: 'WV',
                },
            ],
        })

        const epResult = engine.evaluate(data.dataObject, 'EvalOptionsTest', {
            ...mock.evaluationConfig,
        })

        const resultTypes = epResult.getApplicableResults().map(ruleResult => ruleResult.ruleInfo.payloadtype)

        expect(resultTypes).toHaveLength(Object.keys(PayloadType).length)
        expect(resultTypes).toEqual(expect.arrayContaining(Object.keys(PayloadType)))
    })

    it('should evaluate custom function registered after engine creation', () => {
        const data = mock.data.empty()

        registry.add({
            name: 'custom',
            function(): boolean {
                return false
            },
        })

        const result = engine.evaluate(data, 'function rebuilding', { currencyCd: 'eur', context: {} })

        expect(result).k_toHaveExpressionsFailures(0)
    })
})
