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

import { mock } from '../../../mock'
import { TargetPathUtils } from '../../../../src/engine/runtime/expressions/TargetPathUtils'
import { DataContext } from '../../../../src/engine/contexts/data/DataContext'

describe('Expressions', () => {
    it('should change field name to path', () => {
        const { Policy } = mock.modelTree.contexts
        const resolved = TargetPathUtils.resolveTargetPath(
            Policy.fields.createdOn.name,
            new DataContext('1', 'Policy', {}, mock.contextInstanceInfo, Policy, undefined),
        )
        expect(resolved).toBe('accessTrackInfo.createdOn')
    })
})
