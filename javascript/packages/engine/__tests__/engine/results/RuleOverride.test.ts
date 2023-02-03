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
import { Contexts } from 'kraken-model'
import { extractOverrideDependencies, extractRootId } from '../../../src/engine/results/RuleOverrideContextExtractor'

const { AddressInfo, Policy, COLLCoverage, CreditCardInfo } = mock.modelTreeJson.contexts
import { RuleOverride } from 'kraken-engine-api'
import OverrideDependencyInfo = RuleOverride.OverrideDependencyInfo
import { DataContext } from '../../../src/engine/contexts/data/DataContext'

describe('RuleOverride', () => {
    it('should find root id', () => {
        const dataContext = (id: string, parent?: DataContext) =>
            new DataContext(id, 'mockName', {}, mock.contextInstanceInfo, undefined, parent)
        const root = dataContext('rootId')
        const child1 = dataContext('child1', root)
        const child2 = dataContext('child1', child1)
        expect(extractRootId(child2)).toBe('rootId')
        expect(extractRootId(child1)).toBe('rootId')
        expect(extractRootId(root)).toBe('rootId')
    })
    it('should extract rule override dependency from single dependencies', () => {
        const dependency = {
            name: AddressInfo.name,
            field: AddressInfo.fields.city.name,
            value: 'Vilnius',
        }
        const address = createDataContext(dependency)
        const policy = createDataContext({
            name: Policy.name,
            field: Policy.fields.policyNumber.name,
            value: 'P01',
        })
        policy.externalReferenceObjects.addSingle('AddressInfo', address)
        const overrideDependencies = extractOverrideDependencies(createOverrideDependencyInfos(dependency), policy)

        expect(Object.keys(overrideDependencies)).toHaveLength(1)
        const overrideDependencyName = dependency.name + '.' + dependency.field
        expect(overrideDependencies[overrideDependencyName]).toBeDefined()
        expect(overrideDependencies[overrideDependencyName].name).toBe(overrideDependencyName)
        expect(overrideDependencies[overrideDependencyName].type).toBe('STRING')
        expect(overrideDependencies[overrideDependencyName].value).toBe(dependency.value)
    })
    it('should not extract rule override dependency with no target path', () => {
        const dependency = {
            name: AddressInfo.name,
            field: AddressInfo.fields.city.name,
            value: 'Vilnius',
        }
        const address = createDataContext(dependency)
        const policy = createDataContext({
            name: Policy.name,
            field: Policy.fields.policyNumber.name,
            value: 'P01',
        })
        policy.externalReferenceObjects.addSingle('AddressInfo', address)
        const overrideDependencies = extractOverrideDependencies(
            createOverrideDependencyInfos({ name: AddressInfo.name }),
            policy,
        )

        expect(Object.keys(overrideDependencies)).toHaveLength(0)
    })
    it('should not extract rule override dependency with complex dependency', () => {
        const dependency = {
            name: AddressInfo.name,
            field: AddressInfo.fields.addressLines.name,
            value: {},
        }
        const address = createDataContext(dependency)
        const policy = createDataContext({
            name: Policy.name,
            field: Policy.fields.policyNumber.name,
            value: 'P01',
        })
        policy.externalReferenceObjects.addSingle('AddressInfo', address)
        const overrideDependencies = extractOverrideDependencies(createOverrideDependencyInfos(dependency), policy)

        expect(Object.keys(overrideDependencies)).toHaveLength(0)
    })
    it('should not extract rule override dependency from multiple dependencies', () => {
        const dependency = {
            name: AddressInfo.name,
            field: AddressInfo.fields.city.name,
            value: 'Vilnius',
        }
        const address = createDataContext(dependency)
        const policy = createDataContext({
            name: Policy.name,
            field: Policy.fields.policyNumber.name,
            value: 'P01',
        })
        policy.externalReferenceObjects.addMultiple('AddressInfo', [address])
        const overrideDependencies = extractOverrideDependencies(createOverrideDependencyInfos(dependency), policy)

        expect(Object.keys(overrideDependencies)).toHaveLength(0)
    })
    it('should extract 2 rule override dependencies', () => {
        const addressInfo = {
            name: AddressInfo.name,
            field: AddressInfo.fields.city.name,
            value: 'Vilnius',
        }
        const cOLLCoverageInfo = {
            name: COLLCoverage.name,
            field: COLLCoverage.fields.expirationDate.name,
            value: new Date('2011-11-11'),
        }
        const creditCardInfo = {
            name: CreditCardInfo.name,
            field: CreditCardInfo.fields.cardCreditLimitAmount.name,
            value: { amount: 1, currency: 'eur' } as Contexts.MoneyType,
        }
        const address = createDataContext(addressInfo)
        const cOLLCoverage = createDataContext(cOLLCoverageInfo)
        const creditCard = createDataContext(creditCardInfo)

        const deps = createOverrideDependencyInfos(addressInfo, cOLLCoverageInfo, creditCardInfo)
        const policy = createDataContext({
            name: Policy.name,
            field: Policy.fields.policyNumber.name,
            value: 'P01',
        })
        policy.externalReferenceObjects.addMultiple('AddressInfo', [address])
        policy.externalReferenceObjects.addSingle('COLLCoverage', cOLLCoverage)
        policy.externalReferenceObjects.addSingle('CreditCardInfo', creditCard)
        const overrideDependencies = extractOverrideDependencies(deps, policy)

        expect(Object.keys(overrideDependencies)).toHaveLength(2)
    })
    it('should ignore duplicated dependencies', () => {
        const addressInfo = {
            name: AddressInfo.name,
            field: AddressInfo.fields.city.name,
            value: 'Vilnius',
        }
        const cOLLCoverageInfo = {
            name: COLLCoverage.name,
            field: COLLCoverage.fields.expirationDate.name,
            value: new Date('2011-11-11'),
        }
        const creditCardInfo = {
            name: CreditCardInfo.name,
            field: CreditCardInfo.fields.cardCreditLimitAmount.name,
            value: { amount: 1, currency: 'eur' } as Contexts.MoneyType,
        }
        let depInfos = createOverrideDependencyInfos(addressInfo, cOLLCoverageInfo, creditCardInfo)
        depInfos = [...depInfos, ...depInfos]

        const address = createDataContext(addressInfo)
        const cOLLCoverage = createDataContext(cOLLCoverageInfo)
        const creditCard = createDataContext(creditCardInfo)

        const policy = createDataContext({
            name: Policy.name,
            field: Policy.fields.policyNumber.name,
            value: 'P01',
        })
        policy.externalReferenceObjects.addMultiple('AddressInfo', [address])
        policy.externalReferenceObjects.addSingle('COLLCoverage', cOLLCoverage)
        policy.externalReferenceObjects.addSingle('CreditCardInfo', creditCard)
        const overrideDependencies = extractOverrideDependencies(depInfos, policy)

        expect(Object.keys(overrideDependencies)).toHaveLength(2)
    })
    it('should resolve override dependency name', () => {
        const name = RuleOverride.resolveOverrideDependencyName(Policy.name, Policy.fields.accountName.name)
        expect(name).toBe(Policy.name + '.' + Policy.fields.accountName.name)
    })
})

function createOverrideDependencyInfos(...dependencies: { name: string; field?: string }[]): OverrideDependencyInfo[] {
    return dependencies.map(d => ({
        contextName: d.name,
        contextFieldName: d.field,
    }))
}

let idCounter = 0
function createDataContext(dc: { name: string; field: string; value: unknown }): DataContext {
    return new DataContext(
        'dc' + idCounter++,
        dc.name,
        { [dc.field]: dc.value },
        mock.contextInstanceInfo,
        mock.modelTree.contexts[dc.name].fields,
    )
}
