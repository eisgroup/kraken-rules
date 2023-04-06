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

import { ContextDataProviderFactory } from '../../../src/engine/contexts/data/extraction/ContextDataProviderFactory'
import { mock } from '../../mock'
import { Payloads, Rule } from 'kraken-model'
import PayloadType = Payloads.PayloadType

const { Policy, Party, PartyRole } = mock.modelTree.contexts

describe('ContextDataProviderFactory', () => {
    const contextDataProviderFactory = new ContextDataProviderFactory(mock.contextDataExtractor, mock.contextBuilder)
    const data = Object.freeze(mock.data.empty())
    it('should extract 1 policy', () => {
        const contextDataProvider = contextDataProviderFactory.createContextProvider(data)
        const rule: Rule = {
            name: 'RL01',
            context: Policy.name,
            targetPath: Policy.fields.policyNumber.name,
            payload: {
                type: PayloadType.ACCESSIBILITY,
            },
            dimensionSet: {
                variability: 'UNKNOWN',
            },
        }
        const resolveContextData = contextDataProvider.resolveContextData(rule)
        expect(resolveContextData.contexts).toHaveLength(1)
        expect(resolveContextData.contexts.map(v => v.contextId)).toMatchObject([data.id])
    })
    it('should extract 1 party from array', () => {
        const contextDataProvider = contextDataProviderFactory.createContextProvider(data)
        const rule: Rule = {
            name: 'RL01',
            context: Party.name,
            targetPath: Party.fields.roles.name,
            payload: {
                type: PayloadType.ACCESSIBILITY,
            },
            dimensionSet: {
                variability: 'UNKNOWN',
            },
        }
        const resolveContextData = contextDataProvider.resolveContextData(rule)
        expect(resolveContextData.contexts).toHaveLength(1)
        expect(resolveContextData.contexts.map(v => v.contextId)).toMatchObject([data.parties?.[0].id])
    })
    it('should extract 1 party role from array of arrays', () => {
        const contextDataProvider = contextDataProviderFactory.createContextProvider(data)
        const rule: Rule = {
            name: 'RL01',
            context: PartyRole.name,
            targetPath: PartyRole.fields.role.name,
            payload: {
                type: PayloadType.ACCESSIBILITY,
            },
            dimensionSet: {
                variability: 'UNKNOWN',
            },
        }

        const resolveContextData = contextDataProvider.resolveContextData(rule)
        expect(resolveContextData.contexts).toHaveLength(1)
        expect(resolveContextData.contexts.map(v => v.contextId)).toMatchObject([data.parties?.[0].roles?.[0].id])
    })
})
