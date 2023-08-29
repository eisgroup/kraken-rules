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

import { ExpressionEvaluationResult } from 'kraken-engine-api'
import { mock } from '../../../mock'
import { DataContext } from '../../../../src/engine/contexts/data/DataContext'
import { Expressions } from 'kraken-model'

const { evaluator, session } = mock

export interface TestDataObject {
    creationTime: string
}

function unwrap(expressionResult: ExpressionEvaluationResult.Result): Date {
    if (ExpressionEvaluationResult.isError(expressionResult)) {
        console.error({ expressionResult })
        throw new Error('Expression failed')
    }
    return new Date(expressionResult.success as string)
}

function complex(expression: string): Expressions.Expression {
    return {
        expressionType: 'COMPLEX',
        expressionString: expression,
        originalExpressionString: expression,
    }
}

let dataContext: DataContext
beforeEach(() => {
    const { Policy } = mock.modelTree.contexts
    const dataObject = { creationTime: new Date() }
    dataContext = new DataContext('1', Policy.name, '', dataObject, mock.contextInstanceInfo, Policy, undefined)
})

describe('DateTime in ExpressionEvaluator', () => {
    it('should create date time passed in params', () => {
        const date = evaluator.evaluate(complex("this.DateTime('2011-11-11T10:10:10')"), dataContext, session)
        evaluator.evaluateSet('creationTime', dataContext.dataObject, unwrap(date))
        const creationTime = (dataContext.dataObject as unknown as TestDataObject).creationTime
        expect(new Date(creationTime)).k_toBeDateEqualTo(unwrap(date))
    })
    it('should compare two dates in kraken format from command expression', () => {
        const is = evaluator.evaluate(
            complex(`this.Today() > this.DateTime('2011-11-11T10:10:10')`),
            dataContext,
            session,
        )
        expect(unwrap(is)).toBeTruthy()
    })
    it('should create current dateTime', () => {
        const beforeNow = new Date()
        const now = unwrap(evaluator.evaluate(complex('this.Now()'), dataContext, session))
        const afterNow = new Date()
        // impossible to accurately test Now(), since immediately create Date will have different timestamp
        expect(now.getTime()).toBeGreaterThanOrEqual(beforeNow.getTime())
        expect(now.getTime()).toBeLessThanOrEqual(afterNow.getTime())
    })
})
