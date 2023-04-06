/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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

import { requireDefinedValue } from '../../../../utils/Utils'
import { ContextDataExtractor } from './ContextDataExtractor.types'
import { DataContext } from '../DataContext'
import { Rule } from 'kraken-model'

/**
 * Resolves context data instance for rule in given root context
 */
export interface ContextDataProvider {
    /**
     * Extracts contexts for a rule
     *
     * @param rule a rule to extract contexts for
     * @returns context data that were extracted
     */
    resolveContextData: (rule: Rule) => ContextData
}

export class ContextDataProviderImpl implements ContextDataProvider {
    constructor(
        private readonly root: DataContext,
        private readonly dataExtractor: ContextDataExtractor,
        private readonly restriction?: DataContext,
    ) {
        requireDefinedValue(root, 'Root dataContext must be defined')
        requireDefinedValue(dataExtractor, 'ContextDataExtractor must be defined')
    }

    /**
     * @override
     */
    public resolveContextData(rule: Rule): ContextData {
        const contexts = this.dataExtractor.extractByName(rule.context, this.root, this.restriction)
        return {
            contexts,
            allowedContexts: contexts.filter(d => !this.isForbidden(d, rule.targetPath)),
            forbiddenContexts: contexts.filter(d => this.isForbidden(d, rule.targetPath)),
        }
    }

    private isForbidden(context: DataContext, fieldName: string): boolean {
        return context.contextDefinition.fields?.[fieldName]?.forbidTarget ?? false
    }
}

export type ContextData = {
    contexts: DataContext[]
    allowedContexts: DataContext[]
    forbiddenContexts: DataContext[]
}
