/* eslint-disable @typescript-eslint/no-non-null-assertion */
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

import { DataContextBuilder } from '../../../src/engine/contexts/data/DataContextBuilder'
import { mock } from '../../mock'
import { DataContext } from '../../../src/engine/contexts/data/DataContext'

describe('DataContextBuilder', () => {
    const builder = new DataContextBuilder(mock.modelTree, mock.spi.instance)
    const dataObject = Object.freeze(mock.data.empty())
    const { Insured, Party, AddressInfo } = mock.modelTreeJson.contexts
    it('should create instance', () => {
        expect(builder).not.toBeNull()
    })
    it('should check created instance', () => {
        const dataContext: DataContext = builder.buildFromRoot(dataObject)
        expect(dataContext.dataObject).toMatchObject(dataObject)
        expect(dataContext.contextName).toBe(dataObject['cd'])
        expect(dataContext.contextId).toBeDefined()
    })
    it('should build from extracted array first element', () => {
        const parent: DataContext = builder.buildFromRoot(dataObject)
        const party = builder.buildFromExtractedObject(dataObject.parties![0], Party.name, parent, 0)
        expect(party).toMatchSnapshot()
    })
    it('should build from extracted object', () => {
        const parent: DataContext = builder.buildFromRoot(dataObject)
        const human0DataContext = builder.buildFromExtractedObject(dataObject.insured!, Insured.name, parent, 0)
        const dataContext = builder.buildFromExtractedObject(
            dataObject.insured!.addressInfo!,
            AddressInfo.name,
            human0DataContext,
        )
        expect(dataContext).toMatchSnapshot()
    })
    it('should throw on undefined data object', () => {
        expect(() => builder.buildFromRoot(undefined!)).toThrowError()
    })
    it('should throw on empty data object', () => {
        expect(() => builder.buildFromRoot({})).toThrowError()
    })
    it('should throw on empty info fields', () => {
        const parent: DataContext = builder.buildFromRoot(dataObject)
        expect(() => builder.buildFromExtractedObject({}, 'Policy', parent, 2)).toThrowError()
    })
})
