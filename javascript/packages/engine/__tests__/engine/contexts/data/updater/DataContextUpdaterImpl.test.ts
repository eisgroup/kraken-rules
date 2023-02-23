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

import { DataContextDependency } from '../../../../../src/engine/contexts/data/updater/DataContextUpdater'
import { mock } from '../../../../mock'
import { DataContextUpdaterImpl } from '../../../../../src/engine/contexts/data/updater/DataContextUpdaterImpl'
import { ReferencePathResolverImpl } from '../../../../../src/engine/contexts/ccr/ReferencePathResolverImpl'
import { PathCardinalityResolver } from '../../../../../src/engine/contexts/ccr/PathCardinalityResolver'
import { CommonPathResolver } from '../../../../../src/engine/contexts/ccr/CommonPathResolver'
import { DataContext } from '../../../../../src/engine/contexts/data/DataContext'

const { CreditCardInfo, Vehicle, COLLCoverage, BillingAddress } = mock.modelTreeJson.contexts

describe('DataContextUpdaterImpl', () => {
    function update(dataContext: DataContext, dependency: DataContextDependency): void {
        function resolveReferences(): DataContext[] {
            return [mock.data.dataContextEmpty()]
        }
        const updater = new DataContextUpdaterImpl(
            new ReferencePathResolverImpl(
                mock.modelTree.pathsToNodes,
                new PathCardinalityResolver(mock.modelTree.contexts),
                new CommonPathResolver(),
            ),
            resolveReferences,
        )
        updater.update(dataContext, dependency)
    }
    function createDependency(d: string): DataContextDependency {
        return {
            contextName: d,
        }
    }
    function countReferences(d: DataContext): number {
        return Object.keys(d.objectReferences).length
    }
    it('should update data context', () => {
        const dataContext = mock.data.dataContextEmpty()
        expect(countReferences(dataContext)).toBe(1)
        update(dataContext, createDependency(CreditCardInfo.name))
        expect(countReferences(dataContext)).toBe(2)
    })
    it('should update twice merging dependencies', () => {
        const dataContext = mock.data.dataContextEmpty()
        expect(countReferences(dataContext)).toBe(1)
        update(dataContext, createDependency(CreditCardInfo.name))
        expect(countReferences(dataContext)).toBe(2)
        update(dataContext, createDependency(Vehicle.name))
        expect(countReferences(dataContext)).toBe(3)
    })
    it('should update and not delete dependencies', () => {
        const dataContext = mock.data.dataContextEmpty()
        expect(countReferences(dataContext)).toBe(1)
        update(dataContext, createDependency(CreditCardInfo.name))
        expect(countReferences(dataContext)).toBe(2)
        update(dataContext, createDependency(Vehicle.name))
        expect(countReferences(dataContext)).toBe(3)
    })
    it('should update CreditCardInfo context', () => {
        const dataContext = new DataContext(
            '1',
            CreditCardInfo.name,
            { test: 'data' },
            { getContextInstanceId: () => '1', getContextName: () => CreditCardInfo.name },
            mock.modelTree.contexts[CreditCardInfo.name].fields,
            mock.data.dataContextEmpty(),
        )
        expect(countReferences(dataContext)).toBe(1)
        update(dataContext, createDependency(BillingAddress.name))
        expect(countReferences(dataContext)).toBe(2)
    })
    it('should fail extracting reference with no path available', () => {
        const dataContext = mock.data.dataContextEmpty()
        expect(() => update(dataContext, createDependency(COLLCoverage.name))).toThrow(
            'Cannot determine path to reference, resolved 2',
        )
    })
})
