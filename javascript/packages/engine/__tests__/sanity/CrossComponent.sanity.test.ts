/* eslint-disable @typescript-eslint/no-non-null-assertion */
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

import { sanityMocks } from './_AutoPolicyObject.mocks'
import { RuleEvaluationResults, ValidationPayloadResult } from 'kraken-engine-api'
import { sanityEngine, sanityEngineExtendedPolicy } from './_SanityEngine'
import { EntryPointName } from './_SanityEntryPointNames'
import { conditionEvaluationTypeChecker } from '../../src/dto/DefaultConditionEvaluationResult'
import { MatchableResults } from 'kraken-jest-matchers'
import { FieldMetadataReducer } from '../../src'

describe('Cross context references sanity tests', () => {
    const { valid, empty, model, emptyExtended } = sanityMocks
    const expectEvaluationToBe = ({
        model: policy,
        entryPoint,
        payloadSuccess,
        restriction,
    }: {
        model: object
        entryPoint: EntryPointName
        payloadSuccess: boolean
        restriction?: object
    }) => {
        const results = restriction
            ? sanityEngine.evaluateSubTree(policy, restriction, entryPoint)
            : sanityEngine.evaluate(policy, entryPoint)
        expect(results).not.k_toHaveExpressionsFailures()
        expect(results.getAllRuleResults()).toHaveLength(1)
        expect(
            (
                (results.getAllRuleResults()[0] as RuleEvaluationResults.ApplicableRuleEvaluationResult)
                    .payloadResult as ValidationPayloadResult
            ).success,
        ).toBe(payloadSuccess)
    }
    it('should find reference from 2nd level to root (condition false)', () => {
        const data = valid()
        data['policyCurrency'] = 'EUR'
        const results = sanityEngine.evaluate(data, 'Cross-1')

        expect(results.getAllRuleResults()).toHaveLength(1)
        const conditionEvaluationResult = results.getAllRuleResults()[0].conditionEvaluationResult
        const isRuleApplicable = conditionEvaluationTypeChecker.isApplicable(conditionEvaluationResult)
        expect(isRuleApplicable).toBeFalsy()
        expect(results).not.k_toHaveExpressionsFailures()
    })
    it('should find reference from 2nd level to root (condition true)', () => {
        const data = valid()
        data.policyCurrency = 'USD'
        const results = sanityEngine.evaluate(data, 'Cross-1')

        expect(results.getAllRuleResults()).toHaveLength(1)
        const conditionEvaluationResult = results.getAllRuleResults()[0].conditionEvaluationResult
        const isRuleApplicable = conditionEvaluationTypeChecker.isApplicable(conditionEvaluationResult)
        expect(isRuleApplicable).toBeTruthy()
        expect(results).not.k_toHaveExpressionsFailures()
    })
    it('should find reference from 2nd level to root (condition true) - ExtendedModel', () => {
        const data = empty()
        data['policyCurrency'] = 'USD'
        const results = sanityEngine.evaluate(data, 'Cross-1')

        expect(results).not.k_toHaveExpressionsFailures()
        expect(results.getAllRuleResults()).toHaveLength(1)
        const conditionEvaluationResult = results.getAllRuleResults()[0].conditionEvaluationResult
        const isRuleApplicable = conditionEvaluationTypeChecker.isApplicable(conditionEvaluationResult)
        expect(isRuleApplicable).toBeTruthy()
    })
    it('should set default value from Policy to CreditCard', () => {
        const data = empty()
        data['policyCurrency'] = '1'
        const results = sanityEngine.evaluate(data, 'R-CCR-default-CreditCardInfo-fromAutoPolicy')

        expect(results).not.k_toHaveExpressionsFailures()
        expect(results.getAllRuleResults()).toHaveLength(1)
        expect(data.billingInfo!.creditCardInfo!.cardNumber).toBe('1')
    })
    it('should reference from 3rd level to root', () => {
        expectEvaluationToBe({
            model: model({ policyCurrency: 'a', bilAdr_countryCd: 'a' }),
            entryPoint: 'R-CCR-assert-BillingAddress',
            payloadSuccess: true,
        })
        expectEvaluationToBe({
            model: model({ policyCurrency: 'a', bilAdr_countryCd: 'b' }),
            entryPoint: 'R-CCR-assert-BillingAddress',
            payloadSuccess: false,
        })
    })
    it('should reference from 3rd level to 2nd level', () => {
        expectEvaluationToBe({
            model: model({ creditCardInfo_cardType: 'a', bilAdr_countryCd: 'a' }),
            entryPoint: 'R-CCR-assert-BillingAddress-toCreditCard',
            payloadSuccess: true,
        })
        expectEvaluationToBe({
            model: model({ creditCardInfo_cardType: 'a', bilAdr_countryCd: 'b' }),
            entryPoint: 'R-CCR-assert-BillingAddress-toCreditCard',
            payloadSuccess: false,
        })
    })
    it('should reference from 2nd level collection to root', () => {
        expectEvaluationToBe({
            model: model({ policyCurrency: 'a', vehModel: 'a' }),
            entryPoint: 'R-CCR-assert-Vehicle-toAutoPolicy',
            payloadSuccess: true,
        })
        expectEvaluationToBe({
            model: model({ creditCardInfo_cardType: 'a', policyCurrency: 'b' }),
            entryPoint: 'R-CCR-assert-Vehicle-toAutoPolicy',
            payloadSuccess: false,
        })
    })
    it('should reference from root to 2nd level', () => {
        expectEvaluationToBe({
            model: model({ creditCardInfo_cardType: 'a', policyCurrency: 'a' }),
            entryPoint: 'R-CCR-assert-AutoPolicy-toCreditCard',
            payloadSuccess: true,
        })
        expectEvaluationToBe({
            model: model({ creditCardInfo_cardType: 'a', policyCurrency: 'b' }),
            entryPoint: 'R-CCR-assert-AutoPolicy-toCreditCard',
            payloadSuccess: false,
        })
    })
    it('should reference from 3rd to 3rd level same branch', () => {
        expectEvaluationToBe({
            model: model({ personInfo_name: 'a', partyDriverType: 'a' }),
            entryPoint: 'R-CCR-assert-DriverInfo-PersonInfo',
            payloadSuccess: true,
        })
        expectEvaluationToBe({
            model: model({ personInfo_name: 'a', partyDriverType: 'b' }),
            entryPoint: 'R-CCR-assert-DriverInfo-PersonInfo',
            payloadSuccess: false,
        })
    })
    it('should reference from 3rd to 2nd level different branch', () => {
        expectEvaluationToBe({
            model: model({ creditCardInfo_cardType: 'a', partyDriverType: 'a' }),
            entryPoint: 'R-CCR-assert-DriverInfo-CreditCardInfo',
            payloadSuccess: true,
        })
        expectEvaluationToBe({
            model: model({ creditCardInfo_cardType: 'b', partyDriverType: 'a' }),
            entryPoint: 'R-CCR-assert-DriverInfo-CreditCardInfo',
            payloadSuccess: false,
        })
    })
    it('should reference from 3rd to 2nd level different branch with a restriction - ExtendedModel', () => {
        expectEvaluationToBe({
            model: model({ partyDriverType: 'a', creditCardInfo_cardType: 'a' }),
            entryPoint: 'R-CCR-assert-DriverInfo-CreditCardInfo',
            payloadSuccess: true,
        })
        expectEvaluationToBe({
            model: model({ partyDriverType: 'a', creditCardInfo_cardType: 'b' }),
            entryPoint: 'R-CCR-assert-DriverInfo-CreditCardInfo',
            payloadSuccess: false,
        })
    })
    it('should reference from 3rd to 2nd level different branch with restriction', () => {
        const data = model({ partyDriverType: 'a', creditCardInfo_cardType: 'a' })
        expectEvaluationToBe({
            model: data,
            entryPoint: 'R-CCR-assert-DriverInfo-CreditCardInfo',
            payloadSuccess: true,
            restriction: data.parties![0],
        })
        const data2 = model({ partyDriverType: 'a', creditCardInfo_cardType: 'b' })
        expectEvaluationToBe({
            model: data2,
            entryPoint: 'R-CCR-assert-DriverInfo-CreditCardInfo',
            payloadSuccess: false,
            restriction: data2.parties![0],
        })
    })
    it('should access reference with multiple cardinality', () => {
        const policy = empty()
        policy.parties = [
            {
                cd: 'Party',
                id: 'p01',
                roles: [
                    { cd: 'PartyRole', id: 'c01', role: 'a' },
                    { cd: 'PartyRole', id: 'c02', role: 'b' },
                ],
            },
            {
                cd: 'Party',
                id: 'p02',
                roles: [
                    { cd: 'PartyRole', id: 'c03', role: 'c' },
                    { cd: 'PartyRole', id: 'c04', role: 'd' },
                ],
            },
        ]
        const results = sanityEngine.evaluate(policy, 'R-CCR-Policy-PartyRole')
        const fieldResults = new FieldMetadataReducer().reduce(results)
        const matchableResults = {
            entryPointResults: results,
            fieldResults,
        } as MatchableResults

        expect(results).k_toMatchResultsStats({ total: 1, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
        expect(matchableResults).k_toMatchResultsSnapshots()
    })
    it('should check is self reference with inherited context will be skipped', () => {
        const policy = empty()
        policy.policyNumber = 'P01'
        const results = sanityEngine.evaluate(policy, 'R-CCR-Policy-ExPolicy')
        const fieldResults = new FieldMetadataReducer().reduce(results)
        const matchableResults = {
            entryPointResults: results,
            fieldResults,
        } as MatchableResults

        expect(results).k_toMatchResultsStats({ total: 1, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
        expect(matchableResults).k_toMatchResultsSnapshots()
    })
    it('should not fail on executing rules on same instance with inheritance rules', () => {
        const policy = emptyExtended()
        policy.policyCurrency = 'USD'
        policy.billingInfo!.creditCardInfo!.cardType = 'USD'
        const results = sanityEngineExtendedPolicy.evaluate(policy, 'Inheritance-CCR')
        const fieldResults = new FieldMetadataReducer().reduce(results)
        const matchableResults = {
            entryPointResults: results,
            fieldResults,
        } as MatchableResults

        expect(results).k_toMatchResultsStats({ total: 2, critical: 0 })
        expect(results).k_toHaveExpressionsFailures(0)
        expect(matchableResults).k_toMatchResultsSnapshots()
    })
})
