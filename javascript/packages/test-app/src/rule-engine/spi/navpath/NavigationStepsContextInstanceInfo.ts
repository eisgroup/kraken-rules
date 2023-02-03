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

import { NavigationStep, NavigationStepType } from './NavigationStep'
import { ContextInstanceInfo } from 'kraken-typescript-engine'

/**
 * Implementation of {{@link ContextInstanceInfo}} interface for navigation path
 * based SPI implementation. Used in {@link NavigationStepsContextInstanceInfoResolver}
 *
 * Described data context instance, by providing name and id fields, as well as full
 * extraction path from the root.
 */
export class NavigationStepsContextInstanceInfo implements ContextInstanceInfo {
    readonly contextName: string
    readonly navigationSteps: NavigationStep[]
    readonly contextInstanceId: string

    constructor(name: string, id: string, steps?: NavigationStep[]) {
        Object.defineProperty(this, 'contextName', {
            enumerable: false,
            value: name,
        })
        Object.defineProperty(this, 'contextInstanceId', {
            enumerable: true,
            value: id,
        })
        Object.defineProperty(this, 'navigationSteps', {
            enumerable: false,
            value: steps || [],
        })
    }

    addExtractionStep(name: string, id: string, expression: string): NavigationStepsContextInstanceInfo {
        const step = new NavigationStep(
            NavigationStepType.EXTRACTION,
            this.contextName,
            this.contextInstanceId,
            expression,
        )
        return this.appendStep(name, id, step)
    }

    addListExtractionStep(index: number): NavigationStepsContextInstanceInfo {
        const step = new NavigationStep(NavigationStepType.LIST_INDEX, this.contextName, this.contextInstanceId, index)
        return this.appendStep(this.contextName, this.contextInstanceId, step)
    }

    addInheritanceStep(parentName: string): NavigationStepsContextInstanceInfo {
        const step = new NavigationStep(NavigationStepType.PARENT_ACCESS, this.contextName, this.contextInstanceId)
        return this.appendStep(parentName, this.contextInstanceId, step)
    }

    /**
     * @override
     */
    getContextInstanceId(): string {
        return this.contextInstanceId
    }
    /**
     * @override
     */
    getContextName(): string {
        return this.contextName
    }

    private appendStep(name: string, id: string, step: NavigationStep): NavigationStepsContextInstanceInfo {
        return new NavigationStepsContextInstanceInfo(name, id, [...this.navigationSteps, step])
    }
}
