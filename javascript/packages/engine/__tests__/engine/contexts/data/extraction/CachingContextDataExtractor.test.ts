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

import { CachingContextDataExtractor } from '../../../../../src/engine/contexts/data/extraction/CachingContextDataExtractor'
import { ExtractedChildDataContextBuilder } from '../../../../../src/engine/contexts/data/ExtractedChildDataContextBuilder'
import { ContextDataExtractorImpl } from '../../../../../src/engine/contexts/data/extraction/ContextDataExtractorImpl'
import { mock } from '../../../../mock'
import { DataContextBuilder } from '../../../../../src/engine/contexts/data/DataContextBuilder'
import { ExpressionEvaluator } from '../../../../../src/engine/runtime/expressions/ExpressionEvaluator'

describe('CachingContextDataExtractor', () => {
    it('should extract once by name', () => {
        const extractor = new CachingContextDataExtractor(
            new ContextDataExtractorImpl(
                mock.modelTree,
                new ExtractedChildDataContextBuilder(
                    new DataContextBuilder(mock.modelTree, mock.spi.instance),
                    ExpressionEvaluator.DEFAULT,
                ),
            ),
        )
        const dc1 = extractor.extractByName(
            mock.modelTreeJson.contexts.CreditCardInfo.name,
            mock.data.dataContextEmpty(),
        )
        const dc2 = extractor.extractByName(
            mock.modelTreeJson.contexts.CreditCardInfo.name,
            mock.data.dataContextEmpty(),
        )
        expect(dc1).toHaveLength(1)
        expect(dc2).toHaveLength(1)
        expect(dc2).toBe(dc1)
        expect(dc2[0]).toBe(dc1[0])
    })
    it('should cache', () => {
        const extractByName = jest.fn()
        const extractByPath = jest.fn()

        const extractor = new CachingContextDataExtractor({ extractByName, extractByPath })

        extractor.extractByName('A', mock.dataContextEmpty())
        extractor.extractByName('A', mock.dataContextEmpty())
        extractor.extractByName('B', mock.dataContextEmpty())
        expect(extractByName).toHaveBeenCalledTimes(2)

        extractor.extractByPath(mock.dataContextEmpty(), ['A', 'B'])
        extractor.extractByPath(mock.dataContextEmpty(), ['A', 'B'])
        extractor.extractByPath(mock.dataContextEmpty(), ['A', 'B', 'C'])
        expect(extractByName).toHaveBeenCalledTimes(2)
    })
})
