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

import { CommonPathResolver } from '../../../../src/engine/contexts/ccr/CommonPathResolver'

describe('CommonPathResolver', () => {
    let resolver: CommonPathResolver
    beforeEach(() => {
        resolver = new CommonPathResolver()
    })
    it('should find common when equals', () => {
        const common = resolver.resolveCommon(['Policy', 'Vehicle', 'Driver'], ['Policy', 'Vehicle', 'Driver'])
        expect(common).toMatchObject(['Policy', 'Vehicle', 'Driver'])
    })
    it('should find common when reference children', () => {
        const common = resolver.resolveCommon(
            ['Policy', 'Vehicle', 'Driver'],
            ['Policy', 'Vehicle', 'Driver', 'DriverInfo'],
        )
        expect(common).toMatchObject(['Policy', 'Vehicle', 'Driver'])
    })
    it('should find common when reference parent', () => {
        const common = resolver.resolveCommon(['Policy', 'Vehicle', 'Driver'], ['Policy', 'Vehicle'])
        expect(common).toMatchObject(['Policy', 'Vehicle'])
    })
    it('should find common when reference other branch', () => {
        const common = resolver.resolveCommon(['Policy', 'Vehicle', 'Driver'], ['Policy', 'Party', 'Role'])
        expect(common).toMatchObject(['Policy'])
    })
})
