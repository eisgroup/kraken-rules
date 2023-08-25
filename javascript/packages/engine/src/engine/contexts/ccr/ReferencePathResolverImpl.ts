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

import { logger } from '../../../utils/DevelopmentLogger'
import { PathToNode } from './PathToNode'
import { PathCardinalityResolver } from './PathCardinalityResolver'
import { CommonPathResolver } from './CommonPathResolver'
import { ContextReference } from './ContextReference'
import { ReferencePathResolver } from './ReferencePathResolver'
import { ContextModelTree } from '../../../models/ContextModelTree'
import {
    CONTEXT_MODEL_TREE_MULTIPLE_TARGET,
    CONTEXT_MODEL_TREE_UNDEFINED_TARGET,
    KrakenRuntimeError,
    SystemMessageBuilder,
} from '../../../error/KrakenRuntimeError'

/**
 * Resolves path to reference from data context to the data context
 *
 * @since 1.1.1
 */
export class ReferencePathResolverImpl implements ReferencePathResolver {
    constructor(
        private readonly pathsToNodes: Record<string, ContextModelTree.ContextPath[]>,
        private readonly cardinalityResolver: PathCardinalityResolver,
        private readonly commonPathResolver: CommonPathResolver,
    ) {}

    /**
     * @override
     */
    resolveReferencePath(origin: PathToNode, targetContextName: string): ContextReference {
        const paths = (this.pathsToNodes[targetContextName] ?? []).map(x => x.path)

        if (!paths.length) {
            const m = new SystemMessageBuilder(CONTEXT_MODEL_TREE_UNDEFINED_TARGET).build()
            throw new KrakenRuntimeError(m)
        }
        if (paths.length === 1) {
            const targetPath = paths[0]
            const common = this.commonPathResolver.resolveCommon(origin, targetPath)
            return this.getReference(targetPath, common)
        }

        const filtered = this.filterPaths(origin, paths)
        if (filtered.length > 1) {
            logger.error(
                () =>
                    `Failed to resolve reference path. Resolved multiple paths, '${filtered
                        .map(p => p.path.join('.'))
                        .join(', ')}' from '${origin.join('.')}'`,
            )
            const m = new SystemMessageBuilder(CONTEXT_MODEL_TREE_MULTIPLE_TARGET).parameters(filtered.length).build()
            throw new KrakenRuntimeError(m)
        }

        const target = filtered[0]
        return this.getReference(target.path, target.common)
    }

    private filterPaths(
        origin: PathToNode,
        paths: PathToNode[],
    ): { path: PathToNode; common: PathToNode; level: number }[] {
        let filtered: { path: PathToNode; common: PathToNode; level: number }[] = []
        let filteredScore = 100
        for (const path of paths) {
            const common = this.commonPathResolver.resolveCommon(origin, path)
            const level = common.length - origin.length
            const childLevel = this.limit(path.length - common.length)
            const score = -3 * level + childLevel
            if (filteredScore === score) {
                filtered.push({ path, common, level })
            }
            if (filteredScore > score) {
                filteredScore = score
                filtered = [{ path, common, level }]
            }
        }
        return filtered
    }

    private limit(level: number): number {
        return level > 2 ? 2 : level
    }

    private getReference(target: PathToNode, common: PathToNode): ContextReference {
        // if reference is ancestor cardinality will be always true
        if (common.length >= target.length) {
            return { path: target.slice(common.length - 1, target.length), cardinality: 'SINGLE' }
        } else {
            // this will resolve only part of the path to from common element.
            // In case of cardinality resolution from root, it can become
            // multiple cardinality if multiple is in path
            const cardinalityPath = target.slice(common.length - 1, target.length)
            return {
                path: cardinalityPath,
                cardinality: this.cardinalityResolver.resolveCardinality({ path: cardinalityPath }),
            }
        }
    }
}
