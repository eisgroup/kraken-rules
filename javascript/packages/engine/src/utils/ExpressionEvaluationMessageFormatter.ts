/*
 *  Copyright 2023 EIS Ltd and/or one of its affiliates.
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

import { Expressions } from 'kraken-model'
import { DataContext, DataReference } from '../engine/contexts/data/DataContext'

export function formatExpressionEvaluationMessage(
    type: string,
    expression: Expressions.Expression,
    dataContext: DataContext,
): string {
    let message = `Evaluating ${type} expression: ${expression.originalExpressionString}`
    const ccrDescription = describeCrossContextReferences(expression, dataContext)
    if (ccrDescription) {
        message += `\n${ccrDescription}`
    }
    return message
}

function describeCrossContextReferences(expression: Expressions.Expression, dataContext: DataContext): string {
    if (expression.expressionType === 'COMPLEX') {
        const ccrVariables = expression.expressionVariables?.filter(ref => ref.type === 'CROSS_CONTEXT')
        if (ccrVariables?.length) {
            const ccrString = ccrVariables
                .map(ref => `${ref.name}=${describeReference(dataContext.dataContextReferences[ref.name])}`)
                .join('\n')
            return `Cross context references:\n${ccrString}`
        }
    }
    return ''
}

function describeReference(reference: DataReference | undefined): string {
    if (!reference) {
        return 'null'
    }
    if (reference.cardinality === 'SINGLE') {
        return reference.dataContexts[0]?.id ?? 'null'
    }
    return `[${reference.dataContexts.map(c => c.id).join(',')}]`
}
