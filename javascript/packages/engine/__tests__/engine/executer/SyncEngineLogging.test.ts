/*
 *  Copyright 2023 EIS Ltd and/or one of its affiliates.
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
import { PayloadBuilder, RulesBuilder } from 'kraken-model-builder'
import { Payloads } from 'kraken-model'
import UsageType = Payloads.Validation.UsageType
import { DimensionSetBundleCache } from '../../../src/bundle-cache/dimension-set-cache/DimensionSetBundleCache'
import { ExpressionContextManagerImpl } from '../../../src'
import { KrakenConfig } from '../../../src/config'

type ThisWithKraken = typeof global & { Kraken: KrakenConfig }

let engine: SyncEngine

const Policy = mock.modelTreeJson.contexts.Policy

let previousEnvironment

beforeEach(() => {
    ;(global as ThisWithKraken).Kraken.logger.clear()
    previousEnvironment = process.env.NODE_ENV

    const cache = new DimensionSetBundleCache({ logWarning: () => void 0 }, new ExpressionContextManagerImpl())
    cache.setExpressionContext({})

    cache.add(
        'EngineDebugLoggingTest',
        {},
        {
            evaluation: {
                delta: false,
                fieldOrder: [`${Policy.name}.${Policy.fields.policyNumber.name}`],
                rules: [
                    new RulesBuilder()
                        .setContext(Policy.name)
                        .setName('Accessibility')
                        .setTargetPath(Policy.fields.policyNumber.name)
                        .setPayload(PayloadBuilder.accessibility().notAccessible())
                        .build(),
                    new RulesBuilder()
                        .setContext(Policy.name)
                        .setName('Visibility')
                        .setTargetPath(Policy.fields.policyNumber.name)
                        .setPayload(PayloadBuilder.visibility().notVisible())
                        .build(),
                    new RulesBuilder()
                        .setContext(Policy.name)
                        .setName('Usage')
                        .setTargetPath(Policy.fields.policyNumber.name)
                        .setPayload(PayloadBuilder.usage().is(UsageType.mandatory))
                        .build(),
                    new RulesBuilder()
                        .setContext(Policy.name)
                        .setName('Default')
                        .setTargetPath(Policy.fields.policyNumber.name)
                        .setPayload(PayloadBuilder.default().to('"P0001"'))
                        .build(),
                    new RulesBuilder()
                        .setContext(Policy.name)
                        .setName('Assert')
                        .setTargetPath(Policy.fields.policyNumber.name)
                        .setCondition('!this._eq(__references__.Policy.policyNumber, undefined)', [
                            { name: 'Policy', type: 'CROSS_CONTEXT' },
                        ])
                        .setPayload(PayloadBuilder.asserts().that('__dataObject__.policyNumber == "P0001"'))
                        .addDependency({ contextName: 'Policy', ccrDependency: true, selfDependency: false })
                        .build(),
                    new RulesBuilder()
                        .setContext(Policy.name)
                        .setName('RegExp')
                        .setTargetPath(Policy.fields.policyNumber.name)
                        .setPayload(PayloadBuilder.regExp().match('[A-Z]+'))
                        .build(),
                    new RulesBuilder()
                        .setContext(Policy.name)
                        .setName('Length')
                        .setTargetPath(Policy.fields.policyNumber.name)
                        .setPayload(PayloadBuilder.lengthLimit().limit(5))
                        .build(),
                    new RulesBuilder()
                        .setContext(Policy.name)
                        .setName('MinSize')
                        .setTargetPath(Policy.fields.riskItems.name)
                        .setPayload(PayloadBuilder.size().min(0))
                        .build(),
                    new RulesBuilder()
                        .setContext(Policy.name)
                        .setName('MaxSize')
                        .setTargetPath(Policy.fields.riskItems.name)
                        .setPayload(PayloadBuilder.size().max(5))
                        .build(),
                    new RulesBuilder()
                        .setContext(Policy.name)
                        .setName('SizeRange')
                        .setTargetPath(Policy.fields.riskItems.name)
                        .setPayload(PayloadBuilder.size().range(0, 1))
                        .build(),
                    new RulesBuilder()
                        .setContext(Policy.name)
                        .setName('NumberSet')
                        .setTargetPath(Policy.fields.termNo.name)
                        .setPayload(PayloadBuilder.numberSet().greaterThanOrEqualTo(1, 1))
                        .build(),
                    new RulesBuilder()
                        .setContext(Policy.name)
                        .setName('ValueList')
                        .setTargetPath(Policy.fields.contractTermTypeCd.name)
                        .setPayload(
                            PayloadBuilder.valueList().valueList({
                                values: ['MONTHLY', 'ANNUAL'],
                                valueType: 'STRING',
                            }),
                        )
                        .build(),
                ],
                entryPointName: 'EngineDebugLoggingTest',
            },
            engineVersion: '1',
            expressionContext: {},
        },
    )

    jest.useFakeTimers('modern').setSystemTime(new Date('2022-01-01T10:00:00Z'))

    engine = new SyncEngine({
        cache: cache,
        dataInfoResolver: mock.spi.dataResolver,
        contextInstanceInfoResolver: mock.spi.instance,
        modelTree: mock.modelTree,
        functions: mock.policyFunctions,
        engineCompatibilityVersion: '1.2.3',
    })
})

afterEach(() => {
    ;(global as ThisWithKraken).Kraken.logger.clear()
    process.env.NODE_ENV = previousEnvironment

    jest.useRealTimers()
})

describe('SyncEngine', () => {
    it('Should debug log in development environment', () => {
        ;(global as ThisWithKraken).Kraken.logger.debug = true
        process.env.NODE_ENV = 'development'

        const data = mock.data.dataContextCustom({
            riskItems: [
                {
                    model: 'WV',
                },
            ],
        })

        engine.evaluate(data.dataObject, 'EngineDebugLoggingTest', { ...mock.evaluationConfig })

        const logs = (global as ThisWithKraken).Kraken.logger.logs

        expect(logs.join('\n')).toMatchSnapshot()
    })
})
