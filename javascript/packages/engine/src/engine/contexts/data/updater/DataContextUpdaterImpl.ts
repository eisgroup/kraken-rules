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

import { ReferenceExtractionInfo } from '../extraction/ReferenceExtractionInfo'
import { DataContextUpdater, DataContextDependency } from './DataContextUpdater'
import { ReferencePathResolver } from '../../ccr/ReferencePathResolver'
import { DataContext } from '../DataContext'
import { ErrorCode, KrakenRuntimeError } from '../../../../error/KrakenRuntimeError'

export class DataContextUpdaterImpl implements DataContextUpdater {
    constructor(
        private readonly referencePathResolver: ReferencePathResolver,
        private readonly resolveReferences: (root: DataContext, path: string[]) => DataContext[],
    ) {}
    update(dataContext: DataContext, dependency: DataContextDependency): void {
        const extractionInfo = this.getExtractionInfo(dataContext, dependency.contextName)
        const refs = this.resolveReferences(extractionInfo.extractionRoot, extractionInfo.extractionPath)
        dataContext.updateReference(dependency.contextName, extractionInfo.cardinality, refs)
    }

    private getExtractionInfo(dataContext: DataContext, dependencyName: string): ReferenceExtractionInfo {
        const path = dataContext.getPath()
        const reference = this.referencePathResolver.resolveReferencePath(path, dependencyName)
        return {
            cardinality: reference.cardinality,
            extractionPath: reference.path,
            extractionRoot: this.startContext(dataContext, reference.path),
            dependencyName: dependencyName,
        }
    }
    private startContext(dataContext: DataContext, path: string[]): DataContext {
        const parents = dataContext.getParents()
        const extractionRoot = path[0]
        const parent = parents.find(x => x.contextName === extractionRoot)
        if (!parent) {
            throw new KrakenRuntimeError(
                ErrorCode.INCORRECT_MODEL_TREE,

                `Failed to find extraction root from ${parents.map(x => x.contextName).join(', ')} in path ${path.join(
                    '.',
                )}`,
            )
        }
        return parent
    }
}
