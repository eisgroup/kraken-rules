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

import { Moneys } from '../../../../../src/engine/runtime/expressions/math/Moneys'

describe('Moneys', () => {
    describe('isMoney', () => {
        it('should return true for money object', () => {
            expect(Moneys.isMoney({ amount: 10, currency: 'USD' })).toBeTruthy()
        })
        it('should return false for incomplete object', () => {
            expect(Moneys.isMoney({ amount: 10 })).toBeFalsy()
            expect(Moneys.isMoney({ currency: 'USD' })).toBeFalsy()
        })
        it('should return false for empty object', () => {
            expect(Moneys.isMoney({})).toBeFalsy()
        })
        it('should return false for null', () => {
            expect(Moneys.isMoney(null)).toBeFalsy()
            expect(Moneys.isMoney(undefined)).toBeFalsy()
        })
        it('should return false for other object', () => {
            expect(Moneys.isMoney({ amount: 10, currency: 'USD', smth: 'smth' })).toBeFalsy()
            expect(Moneys.isMoney({ smth: 'smth' })).toBeFalsy()
        })
    })
})
