/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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

import { Contexts } from 'kraken-model'
import { ContextInstanceInfo } from 'kraken-engine-api'
import Cardinality = Contexts.Cardinality
import ContextDefinition = Contexts.ContextDefinition

/**
 * DTO wrapper for data context object instance
 */
export class DataContext {
    public readonly id: string
    public readonly description: string
    // lazy props
    public path?: string[]
    public parentChain?: DataContext[]

    /**
     * A record of all references to DataContexts by name. It does contain references to self DataContext as well.
     */
    public dataContextReferences: Record<string, DataReference>
    /**
     * A record of all references to data objects by name. It does contain references to self data object as well.
     */
    public objectReferences: Record<string, object | object[] | undefined>

    /**
     * @param contextId     Identifies particular data context instance
     * @param contextName   Context definition name, identifies context type
     * @param contextPath   A path to data context.
     * @param dataObject    Underlying data object for context
     * @param info          Information about data object
     * @param contextDefinition    context definition of this context
     * @param parent                Parent data context form which this data context was extracted
     */
    constructor(
        public readonly contextId: string,
        public readonly contextName: string,
        public readonly contextPath: string | undefined,
        public readonly dataObject: Record<string, unknown>,
        public readonly info: ContextInstanceInfo,
        public readonly contextDefinition: ContextDefinition,
        public readonly parent?: DataContext | undefined,
    ) {
        this.dataContextReferences = {}
        this.objectReferences = {}

        const selfReference: DataReference = {
            name: contextName,
            cardinality: 'SINGLE',
            dataContexts: [this],
        }
        this.objectReferences[contextName] = dataObject
        this.dataContextReferences[contextName] = selfReference
        for (const inheritedContextName of contextDefinition.inheritedContexts) {
            this.objectReferences[inheritedContextName] = dataObject
            this.dataContextReferences[inheritedContextName] = selfReference
        }

        this.id = `${this.contextName}:${this.contextId}`
        this.description =
            this.contextPath == undefined ? this.id : `${this.contextName}:${this.contextPath}:${this.contextId}`
    }

    getId(): string {
        return this.id
    }

    getDescription(): string {
        return this.description
    }

    getPath(): string[] {
        if (!this.path) {
            this.path = getPath(this)
        }
        return this.path
    }

    getParents(): DataContext[] {
        if (!this.parentChain) {
            this.parentChain = getParents(this)
        }
        return this.parentChain
    }

    getContextPath(): string | undefined {
        return this.contextPath
    }

    updateReference(name: string, cardinality: Cardinality, dataContexts: DataContext[]) {
        this.objectReferences[name] =
            cardinality === 'SINGLE' ? dataContexts[0]?.dataObject : dataContexts.map(d => d.dataObject)
        this.dataContextReferences[name] = {
            name,
            cardinality,
            dataContexts,
        }
    }
}

function getPath(dataContext: DataContext): string[] {
    const names = []
    let current: DataContext | undefined = dataContext
    while (current) {
        names.push(current.contextName)
        current = current.parent
    }
    return names.reverse()
}

function getParents(dataContext: DataContext): DataContext[] {
    const contexts = []
    let current: DataContext | undefined = dataContext
    while (current) {
        contexts.push(current)
        current = current.parent
    }
    return contexts.reverse()
}

export type DataReference = {
    name: string
    dataContexts: DataContext[]
    cardinality: Cardinality
}
