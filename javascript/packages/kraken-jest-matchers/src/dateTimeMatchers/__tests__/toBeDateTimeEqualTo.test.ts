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

import { k_toBeDateTimeEqualTo } from '../toBeDateTimeEqualTo'

expect.extend({ k_toBeDateTimeEqualTo })

describe('.k_toBeDateTimeEqualTo', () => {
    it('Should pass when dates are equal', () => {
        const firstDate = new Date()

        expect(firstDate).k_toBeDateTimeEqualTo(firstDate)
    })

    it('Should pass with negate when dates are different', () => {
        const firstDate = new Date('2022-01-01T00:00:00Z')
        const secondDate = new Date('2022-01-01T00:01:00Z')

        expect(firstDate).not.k_toBeDateTimeEqualTo(secondDate)
    })

    it('Should fail when dates are different', () => {
        const firstDate = new Date('2022-01-01T00:00:00Z')
        const secondDate = new Date('2022-01-01T00:00:15Z')

        expect(() => expect(firstDate).k_toBeDateTimeEqualTo(secondDate)).toThrowError()
    })

    it('Should fail with negate when dates are equal', () => {
        const firstDate = new Date('2022-01-01T00:00:00Z')

        expect(() => expect(firstDate).not.k_toBeDateTimeEqualTo(firstDate)).toThrowError()
    })
})
