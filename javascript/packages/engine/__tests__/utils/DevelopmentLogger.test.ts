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

import { KrakenConfig, WithKraken } from '../../src/config'
import { DevelopmentLogger } from '../../src/utils/DevelopmentLogger'

const g = globalThis as unknown as WithKraken

type ThisWithKraken = typeof global & { Kraken: KrakenConfig }

beforeEach(() => {
    ;(global as ThisWithKraken).Kraken.logger.clear()
    ;(global as ThisWithKraken).Kraken.logger.debug = true
})

describe('DevelopmentLogger', () => {
    it('should log in development environment', () => {
        process.env.NODE_ENV = 'development'

        const f = jest.fn()
        const mock = {
            groupEnd: jest.fn(),
            group: jest.fn(),
            info: jest.fn(),
            warning: jest.fn(),
            error: jest.fn(),
            debug: jest.fn(),
        }
        const l = new DevelopmentLogger(mock)

        l.debug(() => 'd')
        l.error(() => 'e')
        l.group(
            () => 'g',
            f,
            () => 'g finished',
        )
        l.info(() => 'i')
        l.warning(() => 'w')

        expect(mock.debug).toHaveBeenCalledWith('d')
        expect(mock.error).toHaveBeenCalledWith('e')
        expect(mock.group).toHaveBeenCalledWith('g')
        expect(f).toHaveBeenCalled()
        expect(mock.info).toHaveBeenCalledWith('g finished')
        expect(mock.groupEnd).toHaveBeenCalled()
        expect(mock.info).toHaveBeenCalledWith('i')
        expect(mock.warning).toHaveBeenCalledWith('w')
    })
    it('should not log in production environment', () => {
        g.Kraken.logger.enabled = false

        const f = jest.fn()
        const mock = {
            groupEnd: jest.fn(),
            group: jest.fn(),
            info: jest.fn(),
            warning: jest.fn(),
            error: jest.fn(),
            debug: jest.fn(),
        }
        const l = new DevelopmentLogger(mock)

        l.debug(() => 'd')
        l.error(() => 'e')
        l.group(
            () => 'g',
            f,
            () => 'g finished',
        )
        l.info(() => 'i')
        l.warning(() => 'w')

        expect(mock.debug).not.toHaveBeenCalled()
        expect(mock.error).not.toHaveBeenCalled()
        expect(mock.group).not.toHaveBeenCalled()
        expect(f).toHaveBeenCalled()
        expect(mock.groupEnd).not.toHaveBeenCalled()
        expect(mock.info).not.toHaveBeenCalled()
        expect(mock.warning).not.toHaveBeenCalled()
    })
    it('should catch and log exceptions', () => {
        g.Kraken.logger.enabled = true

        const f = () => {
            throw new Error('custom message')
        }
        const mock = {
            groupEnd: jest.fn(),
            group: jest.fn(),
            info: jest.fn(),
            warning: jest.fn(),
            error: jest.fn(),
            debug: jest.fn(),
        }
        const l = new DevelopmentLogger(mock)

        expect(() =>
            l.group(
                () => 'g',
                f,
                () => 'g finished',
            ),
        ).toThrowError('custom message')
        expect(mock.group).toHaveBeenCalledWith('g')
        expect(mock.error).toHaveBeenCalledTimes(1)
        expect(mock.groupEnd).toHaveBeenCalled()
    })
    it('should log description', () => {
        g.Kraken.logger.enabled = true

        const f = jest.fn()
        const mock = {
            groupEnd: jest.fn(),
            group: jest.fn(),
            info: jest.fn(),
            warning: jest.fn(),
            error: jest.fn(),
            debug: jest.fn(),
        }
        const l = new DevelopmentLogger(mock)

        l.group(
            () => 'g',
            f,
            () => 'description',
        )

        expect(mock.group).toHaveBeenCalledWith('g')
        expect(f).toHaveBeenCalled()
        expect(mock.info).toHaveBeenCalledWith('description')
        expect(mock.groupEnd).toHaveBeenCalled()
    })
    it('should log description and handle nesting', () => {
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

        l.group(
            () => 'level1',
            () => {
                l.debug(() => 'level2DebugBefore')
                l.group(
                    () => 'level2',
                    () => {
                        l.debug(() => 'descriptionLevel2Line1\ndescriptionLevel2Line2')
                    },
                    () => 'level2 finished',
                )
                l.debug(() => 'level2DebugAfter')
            },
            () => 'level1 finished',
        )

        const logs = (global as ThisWithKraken).Kraken.logger.logs
        expect(logs).toStrictEqual([
            '-> level1',
            '   -- level2DebugBefore',
            '   -> level2',
            '      -- descriptionLevel2Line1',
            '         descriptionLevel2Line2',
            '   <- level2 finished',
            '   -- level2DebugAfter',
            '<- level1 finished',
        ])
    })
    it('should log with description and complex object', () => {
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

        const object = {
            name: 'Peter',
            surname: 'John',
            address: {
                city: 'Vilnius',
                addressLine: 'Ulonu 2',
            },
        }

        l.group(
            () => 'level1',
            () => {
                l.debug(() => 'level2DebugBefore')
                l.group(
                    () => 'level2',
                    () => {
                        l.debug(() => ['my object:', object])
                        l.debug(() => 'descriptionLevel2Line1\ndescriptionLevel2Line2')
                        try {
                            throw new Error('this is exception message')
                        } catch (error) {
                            l.debug(() => ['Error occurred.', error])
                        }
                    },
                    () => 'level2 finished',
                )
                l.debug(() => 'level2DebugAfter')
            },
            () => 'level1 finished',
        )

        const logs = (global as ThisWithKraken).Kraken.logger.logs
        expect(logs).toStrictEqual([
            '-> level1',
            '   -- level2DebugBefore',
            '   -> level2',
            '      -- my object:',
            '         {',
            '           "name": "Peter",',
            '           "surname": "John",',
            '           "address": {',
            '             "city": "Vilnius",',
            '             "addressLine": "Ulonu 2"',
            '           }',
            '         }',
            '      -- descriptionLevel2Line1',
            '         descriptionLevel2Line2',
            '      -- Error occurred.',
            '         Error message: this is exception message',
            '   <- level2 finished',
            '   -- level2DebugAfter',
            '<- level1 finished',
        ])
    })
    it('should not nest if debug disabled', () => {
        process.env.NODE_ENV = 'development'
        ;(global as ThisWithKraken).Kraken.logger.debug = false

        const mock = {
            groupEnd: jest.fn(),
            group: jest.fn(),
            info: jest.fn(),
            warning: jest.fn(),
            error: jest.fn(),
            debug: jest.fn(),
        }
        const l = new DevelopmentLogger(mock)

        l.groupDebug(
            () => 'debug start',
            () => l.warning(() => 'warning'),
            () => 'debug end',
        )

        expect(mock.group).not.toHaveBeenCalled()
        expect(mock.warning).toHaveBeenCalledWith('warning')
        expect(mock.debug).not.toHaveBeenCalledWith()
        expect(mock.groupEnd).not.toHaveBeenCalled()

        const logs = (global as ThisWithKraken).Kraken.logger.logs
        expect(logs).toStrictEqual(['-- warning'])
    })
})
