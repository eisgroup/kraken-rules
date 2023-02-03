/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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
import ContextNavigation = Contexts.ContextNavigation
import ContextField = Contexts.ContextField
import ContextDefinition = Contexts.ContextDefinition

export class ContextDefinitionBuilder {
    private name?: string
    private parentDefinitions: string[] = []
    private children: Record<string, ContextNavigation> = {}
    private contextFields?: Record<string, ContextField>

    static create(): ContextDefinitionBuilder {
        return new ContextDefinitionBuilder()
    }

    setName(name: string): ContextDefinitionBuilder {
        this.name = name
        return this
    }

    setParentDefinitions(value: string | string[]): ContextDefinitionBuilder {
        this.parentDefinitions = typeof value === 'string' ? [value] : value
        return this
    }

    addField(field: ContextField): ContextDefinitionBuilder {
        if (!this.contextFields) {
            this.contextFields = {}
        }

        this.contextFields[field.name] = {
            name: field.name,
            cardinality: field.cardinality,
            fieldType: field.fieldType,
            fieldPath: field.fieldPath,
        }

        return this
    }

    addChild(name: string, nav: string): ContextDefinitionBuilder {
        if (!this.children) {
            this.children = {}
        }
        this.children[name] = {
            navigationExpression: {
                expressionType: 'PATH',
                expressionString: nav,
            },
            cardinality: 'SINGLE',
            targetName: name,
        }
        return this
    }

    public build(): ContextDefinition {
        this.isDefined(this.name, 'Name must be defined')

        const context: ContextDefinition = {
            name: this.name,
            inheritedContexts: [],
        }
        if (this.contextFields) {
            context.fields = this.contextFields
        }
        if (this.children) {
            context.children = this.children
        }
        if (this.parentDefinitions) {
            context.inheritedContexts = this.parentDefinitions
        }
        return context
    }

    private isDefined<T>(value: T | undefined, err: string): asserts value is NonNullable<T> {
        if (value === undefined && value === null) {
            throw new Error(err)
        }

        if (typeof value === 'string' && value === '') {
            throw new Error(err)
        }
    }
}
