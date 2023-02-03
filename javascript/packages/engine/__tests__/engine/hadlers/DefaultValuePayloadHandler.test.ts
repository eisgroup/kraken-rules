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

import { Contexts, Payloads, Rule } from 'kraken-model'
import { DefaultValuePayloadResult } from 'kraken-engine-api'
import { RulesBuilder, PayloadBuilder } from 'kraken-model-builder'
import { mock } from '../../mock'
import { DefaultValuePayloadHandler } from '../../../src/engine/handlers/DefaultValuePayloadHandler'
import DefaultingType = Payloads.Derive.DefaultingType
import DefaultValuePayload = Payloads.Derive.DefaultValuePayload
import { ValueChangedEvent } from '../../../src/engine/results/ValueChangedEvent'
import { payloadResultTypeChecker } from '../../../src/engine/results/PayloadResultTypeChecker'
import { DataContext } from '../../../src/engine/contexts/data/DataContext'

const handler = new DefaultValuePayloadHandler(mock.evaluator)
const { session } = mock
let dataContext: DataContext

type Data = {
    cat: {
        name: string
        lastName: string
    }
}

beforeEach(() => {
    const data = { cat: { name: 'Tom' } }
    dataContext = new DataContext('1', 'PersonContext', data, mock.contextInstanceInfo, {}, undefined)
})

describe('defaultValuePayloadHandler', () => {
    describe('Default type', () => {
        it('should get payload type', () => {
            expect(handler.handlesPayloadType()).toBe(Payloads.PayloadType.DEFAULT)
        })
        it('should return payload payloadResult with type DefaultValuePayloadResult', () => {
            const rule = RulesBuilder.create()
                .setContext('R001')
                .setTargetPath('cat.name')
                .setPayload(PayloadBuilder.default().to("'Murzik'"))
                .setName('mock')
                .build()
            const result = handler.executePayload(rule.payload as DefaultValuePayload, rule, dataContext, mock.session)
            expect(payloadResultTypeChecker.isDefault(result)).toBeTruthy()
        })
        it('should not change value', () => {
            const rule = RulesBuilder.create()
                .setContext('R001')
                .setTargetPath('cat.name')
                .setPayload(PayloadBuilder.default().to("'Murzik'"))
                .setName('mock')
                .build()
            const result = handler.executePayload(
                rule.payload as DefaultValuePayload,
                rule,
                dataContext,
                mock.session,
            ) as DefaultValuePayloadResult
            expect(result.events).toHaveLength(0)
        })
        it('should create not existing key and set value', () => {
            const rule = RulesBuilder.create()
                .setContext('R001')
                .setTargetPath('cat.lastName')
                .setPayload(PayloadBuilder.default().to("'Thomas'"))
                .setName('mock')
                .build()
            const result = handler.executePayload(
                rule.payload as DefaultValuePayload,
                rule,
                dataContext,
                session,
            ) as DefaultValuePayloadResult
            expect(result.events).toHaveLength(1)
            expect((result.events?.[0] as ValueChangedEvent).newValue).toBe(
                (dataContext.dataObject as Data).cat.lastName,
            )
        })
    })
    describe('Reset type', () => {
        it('should reset value', () => {
            const rule = RulesBuilder.create()
                .setContext('R001')
                .setTargetPath('cat.name')
                .setPayload(PayloadBuilder.default().apply(DefaultingType.resetValue).to("'Jerry'"))
                .setName('mock')
                .build()
            const result = handler.executePayload(
                rule.payload as DefaultValuePayload,
                rule,
                dataContext,
                session,
            ) as DefaultValuePayloadResult
            expect(result.events).toHaveLength(1)
            expect((result.events?.[0] as ValueChangedEvent).newValue).toBe('Jerry')
            expect((result.events?.[0] as ValueChangedEvent).newValue).toBe(
                (dataContext.dataObject as Data)['cat']['name'],
            )
            expect((result.events?.[0] as ValueChangedEvent).previousValue).toBe('Tom')
        })
        it('should not change value', () => {
            const rule = RulesBuilder.create()
                .setContext('R001')
                .setTargetPath('cat.name')
                .setPayload(PayloadBuilder.default().apply(DefaultingType.resetValue).to("'Tom'"))
                .setName('mock')
                .build()
            const result = handler.executePayload(
                rule.payload as DefaultValuePayload,
                rule,
                dataContext,
                session,
            ) as DefaultValuePayloadResult
            expect(result.events).toHaveLength(0)
        })
        it('should change value from undefined to Thomas', () => {
            const rule = RulesBuilder.create()
                .setContext('R001')
                .setTargetPath('cat.lastName')
                .setPayload(PayloadBuilder.default().apply(DefaultingType.resetValue).to("'Thomas'"))
                .setName('mock')
                .build()
            const result = handler.executePayload(
                rule.payload as DefaultValuePayload,
                rule,
                dataContext,
                session,
            ) as DefaultValuePayloadResult
            expect(result.events).toHaveLength(1)
            expect((result.events?.[0] as ValueChangedEvent).newValue).toBe('Thomas')
            expect((result.events?.[0] as ValueChangedEvent).newValue).toBe(
                (dataContext.dataObject as Data)['cat']['lastName'],
            )
            expect((result.events?.[0] as ValueChangedEvent).previousValue).not.toBeDefined()
        })
    })

    describe('Value coercion', () => {
        type Coverage = {
            moneyLimit: Contexts.MoneyType | undefined
            decimalLimit: number | undefined
            code: string | undefined
            labels: string[]
            address: object
        }

        let instance: Coverage
        let context: DataContext

        beforeEach(() => {
            const fields: Record<string, Contexts.ContextField> = {
                moneyLimit: {
                    name: 'moneyLimit',
                    fieldType: 'MONEY',
                    cardinality: 'SINGLE',
                    fieldPath: 'moneyLimit',
                },
                decimalLimit: {
                    name: 'decimalLimit',
                    fieldType: 'DECIMAL',
                    cardinality: 'SINGLE',
                    fieldPath: 'decimalLimit',
                },
                code: {
                    name: 'code',
                    fieldType: 'STRING',
                    cardinality: 'SINGLE',
                    fieldPath: 'code',
                },
                labels: {
                    name: 'labels',
                    fieldType: 'STRING',
                    cardinality: 'MULTIPLE',
                    fieldPath: 'labels',
                },
                address: {
                    name: 'address',
                    fieldType: 'Address',
                    cardinality: 'SINGLE',
                    fieldPath: 'address',
                },
            }
            instance = {
                moneyLimit: {
                    amount: 11,
                    currency: 'USD',
                },
                decimalLimit: 22,
                code: 'cd',
                labels: [],
                address: {},
            }
            context = new DataContext('1', 'Coverage', instance, mock.contextInstanceInfo, fields, undefined)
        })

        function createResetRule(field: string, defaultExpression: string): Rule {
            const payload = PayloadBuilder.default().apply(DefaultingType.resetValue).to(defaultExpression)
            return RulesBuilder.create()
                .setName('RL01')
                .setContext('Coverage')
                .setTargetPath(field)
                .setPayload(payload)
                .build()
        }

        it('should coerce number to Money', () => {
            const rule = createResetRule('moneyLimit', '10')
            handler.executePayload(rule.payload as DefaultValuePayload, rule, context, session)
            expect(instance.moneyLimit).toStrictEqual({ amount: 10, currency: 'USD' })
        })
        it('should coerce Money to number', () => {
            const rule = createResetRule('decimalLimit', '__dataObject__.moneyLimit')
            handler.executePayload(rule.payload as DefaultValuePayload, rule, context, session)
            expect(instance.decimalLimit).toStrictEqual(11)
        })
        it('should return error if incompatible type', () => {
            const rule = createResetRule('code', '10')
            const result = handler.executePayload(rule.payload as DefaultValuePayload, rule, context, session)
            expect(result.error).toBeDefined()
            expect(result.error?.error.message).toStrictEqual(
                `Cannot apply value '10 (typeof number)' on 'Coverage.code' because value type is not assignable to field type 'STRING'. Rule will be silently ignored.`,
            )
        })
        it('should reset to null', () => {
            const rule = createResetRule('moneyLimit', 'undefined')
            handler.executePayload(rule.payload as DefaultValuePayload, rule, context, session)
            expect(instance.moneyLimit).toBeUndefined()
        })
        it('should throw if defaulting array of primitives', () => {
            const rule = createResetRule('labels', 'undefined')
            expect(() =>
                handler.executePayload(rule.payload as DefaultValuePayload, rule, context, session),
            ).toThrowError()
        })
        it('should throw if defaulting context', () => {
            const rule = createResetRule('address', 'undefined')
            expect(() =>
                handler.executePayload(rule.payload as DefaultValuePayload, rule, context, session),
            ).toThrowError()
        })
    })
})
