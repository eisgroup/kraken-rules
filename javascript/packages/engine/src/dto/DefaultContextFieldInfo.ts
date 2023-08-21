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
import { DataContext } from '../engine/contexts/data/DataContext'
import { CONTEXT_MODEL_TREE_MISSING_FIELD, KrakenRuntimeError, SystemMessageBuilder } from '../error/KrakenRuntimeError'
import { ContextFieldInfo } from 'kraken-engine-api'

export class DefaultContextFieldInfo implements ContextFieldInfo {
    readonly contextId: string
    readonly contextName: string
    readonly fieldName: string
    readonly fieldPath: string
    readonly fieldType: string

    constructor(dataContext: DataContext, fieldName: string) {
        let contextField: Contexts.ContextField
        if (dataContext.contextDefinition.fields?.[fieldName]) {
            contextField = dataContext.contextDefinition.fields[fieldName]
        } else {
            const m = new SystemMessageBuilder(CONTEXT_MODEL_TREE_MISSING_FIELD).parameters(fieldName).build()
            throw new KrakenRuntimeError(m)
        }
        this.contextId = dataContext.contextId
        this.contextName = dataContext.contextName
        this.fieldName = fieldName
        this.fieldPath = contextField.fieldPath
        this.fieldType = contextField.fieldType
    }
}
