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
import { sanityEngine } from './_SanityEngine'

describe('Engine Sanity DefaultValue Payload Test', () => {
    const { valid, empty } = sanityMocks
    it("should execute 'InitAutoPolicy' entrypoint with empty data", () => {
        const dataObject = empty()
        const results = sanityEngine.evaluate(dataObject, 'InitAutoPolicy')

        expect(results).not.k_toHaveExpressionsFailures()
        expect(dataObject.policyNumber).toBe('Q0001')
        expect(dataObject.state).toBe('Initialized')
        expect(dataObject.createdFromPolicyRev).toBe(1)
        expect(dataObject.transactionDetails!.txType).toBe('NEW BUSINESS')
        expect(dataObject.termDetails!.termNo).toBe(0)
        expect(dataObject.termDetails!.termCd).toBe('ANNUAL')

        expect(dataObject.transactionDetails!.txEffectiveDate).k_toBeDateEqualTo(new Date('2018-04-30T10:04:00Z'))
        expect(dataObject.transactionDetails!.txCreateDate).k_toBeTodayDate()
        expect(dataObject.accessTrackInfo!.createdOn).k_toBeTodayDate()
        expect(dataObject.accessTrackInfo!.updatedOn).k_toBeTodayDate()
        expect(dataObject.accessTrackInfo!.updatedBy).toContain('qa2')
        expect(dataObject.termDetails!.termEffectiveDate).k_toBeTodayDate()
    })
    it("should execute 'InitAutoPolicy' entrypoint with valid data", () => {
        const dataObject = valid()
        const results = sanityEngine.evaluate(dataObject, 'InitAutoPolicy')
        expect(results).not.k_toHaveExpressionsFailures()
        expect(dataObject.policyNumber).toBe('Q0006')
        expect(dataObject.state).toBe('State')
        expect(dataObject.createdFromPolicyRev).toBe(1)
        expect(dataObject.transactionDetails!.txType).toBe('CashBack')
        expect(dataObject.transactionDetails!.txEffectiveDate).k_toBeDateEqualTo(new Date('2018-01-01T00:00:00Z'))
        expect(dataObject.transactionDetails!.txCreateDate).k_toBeDateEqualTo(new Date('2018-01-01'))
        expect(dataObject.accessTrackInfo!.createdOn).k_toBeDateEqualTo(new Date('2000-01-01'))
        expect(dataObject.termDetails!.termEffectiveDate).k_toBeDateEqualTo(new Date('2000-01-01'))
        expect(dataObject.accessTrackInfo!.updatedOn).k_toBeTodayDate()
        expect(dataObject.termDetails!.termNo).toBe(11)
        expect(dataObject.termDetails!.termCd).toBe('TemrCd')
        expect(dataObject.accessTrackInfo!.updatedBy).toBe('qa2')
    })
    it('should throw an error when two rules defined on one field', () => {
        // second default rule is activated when policyNumber is '666'
        const data = empty()
        data.policyNumber = '666'
        expect(() => sanityEngine.evaluate(data, 'InitAutoPolicy')).toThrow(
            "On field 'Policy:0:state' " +
                "applied '2' default rules: 'R0005a, R0005'. Only one default rule can be applied on the same field.",
        )
    })
})
