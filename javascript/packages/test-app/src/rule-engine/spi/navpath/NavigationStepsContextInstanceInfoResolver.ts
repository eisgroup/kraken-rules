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

import { NavigationStepsContextInstanceInfo } from './NavigationStepsContextInstanceInfo'
import { NavigationStep, NavigationStepType } from './NavigationStep'
import { toBe } from 'declarative-js'
import { Contexts } from 'kraken-model'
import { ContextInstanceInfoResolver, DataObjectInfoResolver, ContextInstanceInfo } from 'kraken-typescript-engine'

/**
 * Implementation of {@link ContextInstanceInfoResolver} SPI, using {@link NavigationStepsContextInstanceInfo}
 * to store information about each context instance, as well as all navigation steps executed during context
 * extraction to obtain this data object instance.
 * Navigation steps can be replayed by invoking client application on root object to obtain same instance to
 * apply rule results
 */
export class NavigationStepsContextInstanceInfoResolver implements ContextInstanceInfoResolver<string[]> {
    constructor(private readonly resolver: DataObjectInfoResolver) {}

    /**
     * @override
     */
    resolveRootInfo(dataObject: object): ContextInstanceInfo {
        const { resolver } = this
        return new NavigationStepsContextInstanceInfo(resolver.resolveName(dataObject), resolver.resolveId(dataObject))
    }

    /**
     * @override
     */
    resolveExtractedInfo(
        dataObject: object,
        target: Contexts.ContextDefinition,
        source: Contexts.ContextDefinition,
        parentInfo: ContextInstanceInfo,
        index?: number,
    ): ContextInstanceInfo {
        const { resolveName, resolveId } = this.resolver
        const { navigationExpression } = source.children[target.name]
        const info = (parentInfo as NavigationStepsContextInstanceInfo).addExtractionStep(
            resolveName(dataObject),
            resolveId(dataObject),
            navigationExpression.expressionString,
        )
        if (index !== null && index !== undefined) {
            return info.addListExtractionStep(index)
        }
        return info
    }

    /**
     * @override
     */
    resolveAncestorInfo(
        _dataObject: object,
        ancestor: Contexts.ContextDefinition,
        _child: Contexts.ContextDefinition,
        childInfo: ContextInstanceInfo,
    ): ContextInstanceInfo {
        return (childInfo as NavigationStepsContextInstanceInfo).addInheritanceStep(ancestor.name)
    }

    /**
     * @override
     */
    processContextInstanceInfo(info: ContextInstanceInfo): string[] {
        if (info instanceof NavigationStepsContextInstanceInfo) {
            const instanceInfo = info as NavigationStepsContextInstanceInfo
            const steps = instanceInfo.navigationSteps.map(this.toNavigationString).filter(toBe.present) as string[]
            return ['this', ...steps]
        }
        throw new Error('Unsupported type of ContextInstanceInfo')
    }

    /**
     * @override
     */
    validateContextDataObject = (d: object) => this.resolver.validate(d)

    private toNavigationString(step: NavigationStep): string | undefined {
        const { type } = step
        if (type === NavigationStepType.EXTRACTION) {
            return step.expression as string
        }
        if (type === NavigationStepType.LIST_INDEX) {
            return '[' + (step.expression as number) + ']'
        }
        return undefined
    }
}
