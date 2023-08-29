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

import { mock } from '../../mock'
import { DataContext } from '../../../src/engine/contexts/data/DataContext'

describe('DataContext', () => {
    it('should return construct id from attributes', () => {
        const { Policy } = mock.modelTree.contexts
        const dataCtx: DataContext = new DataContext('1', Policy.name, undefined, {}, mock.contextInstanceInfo, Policy)
        expect(dataCtx.getId()).toBe('Policy:1')
    })
    it('should returns constructed data context description', () => {
        const { Policy } = mock.modelTree.contexts
        const dataCtx: DataContext = new DataContext(
            '1',
            Policy.name,
            'path.to.context',
            {},
            mock.contextInstanceInfo,
            Policy,
        )
        expect(dataCtx.getDescription()).toBe('Policy:path.to.context:1')
    })
})
