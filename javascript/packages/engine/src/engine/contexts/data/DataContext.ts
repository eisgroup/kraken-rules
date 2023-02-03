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

/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */

import { Contexts } from 'kraken-model'
import ContextField = Contexts.ContextField
import { ExternalReferences } from './ExternalReferences'
import { ContextInstanceInfo } from 'kraken-engine-api'

/**
 * DTO wrapper for data context object instance
 */
export class DataContext {
    public externalReferenceObjects: ExternalReferences
    public readonly id: string
    // lazy props
    public path?: string[]
    public parentChain?: DataContext[]

    /**
     * @param contextId     Identifies particular data context instance
     * @param contextName   Context definition name, identifies context type
     * @param dataObject    Underlying data object for context
     * @param info          Information about data object
     * @param definitionProjection    Holds all fields that are of for this context
     * @param parent                Parent data context form which this data context was extracted
     * @param externalReferences    References to other data contexts, that are in object,
     *                              where key is context definition name
     */
    constructor(
        public readonly contextId: string,
        public readonly contextName: string,
        public readonly dataObject: Record<string, unknown>,
        public readonly info: ContextInstanceInfo,
        public readonly definitionProjection: Record<string, ContextField> = {},
        public readonly parent?: DataContext | undefined,
        public readonly inheritedContextNames: string[] = [],
    ) {
        this.externalReferenceObjects = new ExternalReferences()

        this.externalReferenceObjects.addSingle(contextName, this)
        for (const name of inheritedContextNames) {
            this.externalReferenceObjects.addSingle(name, this)
        }
        this.id = `${this.contextName}:${this.contextId}`
    }

    getId(): string {
        return this.id
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
