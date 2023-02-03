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

import { k_toBeTodayDate } from '../toBeTodayDate'

expect.extend({ k_toBeTodayDate })

describe('.k_toBeTodayDate', () => {
    it('Should pass when date is today', () => {
        const expectedDate = new Date()

        expect(expectedDate).k_toBeTodayDate()
    })

    it('Should pass with negate when date is not today', () => {
        const expectedDate = new Date('1970-01-01')

        expect(expectedDate).not.k_toBeTodayDate()
    })

    it('Should fail when date is not today', () => {
        const expectedDate = new Date('1970-01-01')

        expect(() => expect(expectedDate).k_toBeTodayDate()).toThrowError()
    })

    it('Should fail with negate when date is today', () => {
        const expectedDate = new Date()

        expect(() => expect(expectedDate).not.k_toBeTodayDate()).toThrowError()
    })
})
