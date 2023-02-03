/*
 *  Copyright 2020 EIS Ltd and/or one of its affiliates.
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

import { KrakenConfig } from '../../src/config'
import { DevelopmentLogger } from '../../src/utils/DevelopmentLogger'

type ThisWithKraken = typeof global & { Kraken: KrakenConfig }

describe('DevelopmentLogger', () => {
    it('should call provided methods', () => {
        ;(global as ThisWithKraken).Kraken.logger.debug = true
        const before = process.env.NODE_ENV
        process.env.NODE_ENV = 'development'
        const mock = {
            groupEnd: jest.fn(),
            group: jest.fn(),
            info: jest.fn(),
            warning: jest.fn(),
            error: jest.fn(),
            debug: jest.fn(),
        }
        const l = new DevelopmentLogger(mock)
        l.debug('d')
        expect(mock.debug).toHaveBeenCalledWith('d')
        l.error('e')
        expect(mock.error).toHaveBeenCalledWith('e')
        l.group('g')
        expect(mock.group).toHaveBeenCalledWith('g')
        l.groupEnd('ge')
        expect(mock.groupEnd).toHaveBeenCalledWith('ge')
        l.info('i')
        expect(mock.info).toHaveBeenCalledWith('i')
        l.warning('w')
        expect(mock.warning).toHaveBeenCalledWith('w')
        process.env.NODE_ENV = before
    })

    it('should not call provided methods', () => {
        const before = process.env.NODE_ENV
        process.env.NODE_ENV = 'production'
        const mock = {
            groupEnd: jest.fn(),
            group: jest.fn(),
            info: jest.fn(),
            warning: jest.fn(),
            error: jest.fn(),
            debug: jest.fn(),
        }
        const l = new DevelopmentLogger(mock)
        l.debug('d')
        l.error('e')
        l.group('g')
        l.groupEnd('ge')
        l.info('i')
        l.warning('w')
        expect(mock.debug).not.toHaveBeenCalled()
        expect(mock.error).not.toHaveBeenCalled()
        expect(mock.group).not.toHaveBeenCalled()
        expect(mock.groupEnd).not.toHaveBeenCalled()
        expect(mock.info).not.toHaveBeenCalled()
        expect(mock.warning).not.toHaveBeenCalled()
        process.env.NODE_ENV = before
    })
})
