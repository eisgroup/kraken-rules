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

import { Reducer, toBe } from 'declarative-js'
import flat = Reducer.flat

import { Contexts } from 'kraken-model'
import ContextDefinition = Contexts.ContextDefinition

import { ExtractedChildDataContextBuilder } from '../ExtractedChildDataContextBuilder'
import { ContextModelTree } from '../../../../models/ContextModelTree'
import { ContextDataExtractor } from './ContextDataExtractor.types'
import { logger } from '../../../../utils/DevelopmentLogger'
import { DataContext } from '../DataContext'
import { ErrorCode, KrakenRuntimeError } from '../../../../error/KrakenRuntimeError'

interface ModelTree {
    context(name: string): ContextDefinition
}

function Tree(tree: ContextModelTree.ContextModelTree): ModelTree {
    return {
        context: function context(name: string): ContextDefinition {
            return tree.contexts[name]
        },
    }
}

/**
 *
 *
 * @export
 * @class ContextDataExtractorImpl
 * @implements {ContextDataExtractor}
 */
export class ContextDataExtractorImpl implements ContextDataExtractor {
    constructor(
        private readonly modelTree: ContextModelTree.ContextModelTree,
        private readonly childBuilder: ExtractedChildDataContextBuilder,
    ) {
        this.extractByPath = this.extractByPath.bind(this)
        this.extractByName = this.extractByName.bind(this)
    }

    extractByPath(root: DataContext, path: string[]): DataContext[] {
        if (path.length === 1 && path[0] === root.contextName) {
            return [root]
        }
        const extractedDataContexts = this.extractContexts(path.map(Tree(this.modelTree).context), root)
        return extractedDataContexts
    }

    extractByName(childContextName: string, root: DataContext, restriction?: DataContext): DataContext[] {
        logger.debug(() => `Extracting context definition '${childContextName}' instance`)
        if (!childContextName) {
            throw new KrakenRuntimeError(ErrorCode.INCORRECT_MODEL_TREE, 'childContextName must be defined')
        }

        if (root == null) {
            throw new Error('root must be defined')
        }
        const paths = this.modelTree.pathsToNodes[childContextName]
        if (!paths || !paths.length) {
            throw new KrakenRuntimeError(
                ErrorCode.INCORRECT_MODEL_TREE,
                `Could not find any extraction paths from ${root.contextName} to ${childContextName}.`,
            )
        }
        function extractFromPath(this: ContextDataExtractorImpl, path: ContextModelTree.ContextPath): DataContext[] {
            return this.extractContexts(path.path.map(Tree(this.modelTree).context), root, restriction)
        }
        const dataContexts = paths
            .filter(assertRestrictionWithinPath(restriction))
            .filter(toBe.notEmpty)
            .map(extractFromPath.bind(this))
            .reduce(flat, [])
        return dataContexts
    }

    private extractContexts(paths: ContextDefinition[], root: DataContext, restriction?: DataContext): DataContext[] {
        logger.debug(() => `Path to extract '${paths.map(p => p.name).join('->')}'`)
        let contexts = [root]
        for (let i = 0; i < paths.length; i++) {
            if (i === paths.length - 1) break
            const from = paths[i]
            const next = paths[i + 1]
            const resolvedCtxChildren = contexts.map(child =>
                this.childBuilder.resolveImmediateChildren({
                    childContextName: next.name,
                    parentContextDefinition: from,
                    parentDataContext: child,
                }),
            )
            contexts = resolvedCtxChildren.reduce(flat, []).filter(isNotRestricted(restriction))
        }
        return contexts
    }
}

function isNotRestricted(restriction?: DataContext): (child: DataContext) => boolean {
    return function _toBeRestrictedBy(child: DataContext): boolean {
        if (!restriction) {
            return true
        }
        if (child.contextName === restriction.contextName) {
            return child.contextId === restriction.contextId
        }
        return true
    }
}

function assertRestrictionWithinPath(target?: DataContext): (path: ContextModelTree.ContextPath) => boolean {
    return function _assertRestrictionWithinPath(path: ContextModelTree.ContextPath): boolean {
        return target ? !!path.path.filter(pathContextName => pathContextName === target.contextName).length : true
    }
}
