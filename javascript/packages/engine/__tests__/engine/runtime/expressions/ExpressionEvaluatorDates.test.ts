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

import { mock } from '../../../mock'
import { TestProduct } from 'kraken-test-product'
import { ExpressionEvaluationResult } from 'kraken-engine-api'
import { Expressions } from 'kraken-model'

const { evaluator } = mock

export const q = (value: string) => {
    let modValue = value[0] !== "'" ? `'${value}` : value
    modValue = modValue[value.length] !== "'" ? `${modValue}'` : modValue
    return modValue
}

function complex(expressionString: string): Expressions.Expression {
    return {
        expressionType: 'COMPLEX',
        expressionString,
    }
}

function unwrapAsDate(expressionResult: ExpressionEvaluationResult.Result): Date {
    if (ExpressionEvaluationResult.isError(expressionResult)) {
        console.error({ expressionResult })
        throw new Error('Expression failed')
    }
    return expressionResult.success as Date
}

function unwrapAsBoolean(expressionResult: ExpressionEvaluationResult.Result): boolean {
    if (ExpressionEvaluationResult.isError(expressionResult)) {
        console.error({ expressionResult })
        throw new Error('Expression failed')
    }
    return expressionResult.success as boolean
}

describe('Date functions in expression evaluator', () => {
    const dataContext = mock.data.dataContextEmpty()
    it('should create current date', () => {
        const result = evaluator.evaluate(complex('this.Today()'), dataContext)
        expect(unwrapAsDate(result)).k_toBeTodayDate()
    })
    it('should create date passed in params', () => {
        const result = evaluator.evaluate(complex("this.Date('2011-11-11')"), dataContext)
        expect(unwrapAsDate(result)).k_toBeDateEqualTo(new Date('2011-11-11'))
    })
    it('should add to 1 year to current date', () => {
        const result = evaluator.evaluate(complex("this.PlusYears(this.Date('2011-11-11'), 1)"), dataContext)
        expect(unwrapAsDate(result)).k_toBeDateEqualTo(new Date('2012-11-11'))
    })
    it('should add to 1 month to current date', () => {
        const result = evaluator.evaluate(complex("this.PlusMonths(this.Date('2011-11-11'), 3)"), dataContext)
        expect(unwrapAsDate(result)).k_toBeDateEqualTo(new Date('2012-02-11'))
    })
    it('should add to 1 day to current date', () => {
        const result = evaluator.evaluate(complex("this.PlusDays(this.Date('2011-11-11'), 3)"), dataContext)
        expect(unwrapAsDate(result)).k_toBeDateEqualTo(new Date('2011-11-14'))
    })
    it('should compare two dates from date field kraken format', () => {
        const dc = mock.data.dataContextEmpty()
        const dcObject = dc.dataObject as unknown as TestProduct.kraken.testproduct.domain.Policy
        dcObject.accessTrackInfo!.createdOn = new Date(2000, 1, 1)
        const result = evaluator.evaluate(complex(`this.Today() > __dataObject__.accessTrackInfo.createdOn`), dc)
        expect(unwrapAsBoolean(result)).toBeTruthy()
    })
    it('should test NumberOfDaysBetween to true', () => {
        const result = evaluator.evaluate(
            complex("this.NumberOfDaysBetween(this.Date('2011-11-01'), this.Date('2011-11-10')) == 9"),
            dataContext,
        )
        expect(unwrapAsBoolean(result)).toBeTruthy()
    })
    it('should test NumberOfDaysBetween to false', () => {
        const result = evaluator.evaluate(
            complex("this.NumberOfDaysBetween(this.Date('2011-11-01'), this.Date('2011-11-11')) == 9"),
            dataContext,
        )
        expect(unwrapAsBoolean(result)).toBeFalsy()
    })
    it('should test NumberOfDaysBetween to true, with negative', () => {
        const result = evaluator.evaluate(
            complex("this.NumberOfDaysBetween(this.Date('2011-11-10'), this.Date('2011-11-01')) == 9"),
            dataContext,
        )
        expect(unwrapAsBoolean(result)).toBeTruthy()
    })
    it('should test IsDateBetween to true', () => {
        const result = evaluator.evaluate(
            complex("this.IsDateBetween(this.Date('2011-11-05'), this.Date('2011-11-01'), this.Date('2011-11-10'))"),
            dataContext,
        )
        expect(unwrapAsBoolean(result)).toBeTruthy()
    })
    it('should test IsDateBetween to false', () => {
        const result = evaluator.evaluate(
            complex("this.IsDateBetween(this.Date('2011-11-11'), this.Date('2011-11-01'), this.Date('2011-11-10'))"),
            dataContext,
        )
        expect(unwrapAsBoolean(result)).toBeFalsy()
    })
    it('should test IsDateBetween inclusive to false', () => {
        const result = evaluator.evaluate(
            complex("this.IsDateBetween(this.Date('2011-11-10'), this.Date('2011-11-01'), this.Date('2011-11-10'))"),
            dataContext,
        )
        expect(unwrapAsBoolean(result)).toBeTruthy()
    })
})
