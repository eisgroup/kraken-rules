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

import { ContextInstanceInfoResolver } from '../info/ContextInstanceInfoResolver'
import { requireDefinedValue } from '../../../utils/Utils'
import { DataContextBuilderError } from '../Errors'
import { ContextModelTree } from '../../../models/ContextModelTree'
import { DataContext } from './DataContext'
import { ErrorCode, KrakenRuntimeError } from '../../../error/KrakenRuntimeError'
import { ContextInstanceInfo } from 'kraken-engine-api'

export class DataContextBuilder {
    constructor(
        private readonly modelTree: ContextModelTree.ContextModelTree,
        private readonly resolver: ContextInstanceInfoResolver<unknown>,
    ) {}

    /**
     * Produce {@link DataContext} instance from passed context object instance
     */
    buildFromRoot(rootContextObject: object): DataContext {
        const { requireValid, build, resolver } = this
        return build(requireValid(rootContextObject), resolver.resolveRootInfo(rootContextObject))
    }

    /**
     * Produce {@link DataContext} instance from extracted object
     */
    buildFromExtractedObject(
        contextDataObject: object,
        childContextName: string,
        parentContext: DataContext,
        index?: number,
    ): DataContext {
        const { modelTree, requireValid, build, resolver } = this
        const info = resolver.resolveExtractedInfo(
            requireValid(contextDataObject),
            modelTree.contexts[childContextName],
            modelTree.contexts[parentContext.contextName],
            parentContext.info,
            index,
        )
        return build(contextDataObject, info, parentContext)
    }

    private build = (data: object, info: ContextInstanceInfo, parent?: DataContext): DataContext => {
        requireDefinedValue(info, 'Context instance info is null')
        const contextDefinitionName = requireDefinedValue(info.getContextName(), 'Context definition name is null')
        const contextDefinition = this.modelTree.contexts[contextDefinitionName]

        if (!contextDefinition) {
            throw new KrakenRuntimeError(
                ErrorCode.UNKNOWN_CONTEXT_DEFINITION,
                `Context Definition with name ${contextDefinitionName} does not exist in the Kraken model tree`,
            )
        }

        return new DataContext(
            requireDefinedValue(info.getContextInstanceId(), 'Context instance id is null'),
            contextDefinitionName,
            data as Record<string, unknown>,
            info,
            contextDefinition,
            parent,
        )
    }

    private requireValid: (data: object) => object = (data: object) => {
        if (data == undefined) {
            throw new DataContextBuilderError('Context data object is null.')
        }
        const errors = this.resolver.validateContextDataObject(data)
        if (errors.length) {
            throw new DataContextBuilderError(
                'Context data is not valid:\n\t' + errors.map(error => error.message).join('\n\t'),
            )
        }
        return data
    }
}
