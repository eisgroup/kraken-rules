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

import { Condition, Payloads, Dependency, Rule, Expressions, DimensionSet } from 'kraken-model'

export class RulesBuilder {
    private name?: string
    private context?: string
    private targetPath?: string
    private condition?: Condition
    private payload?: Payloads.Payload
    private dependencies?: Dependency[]
    private dimensionSet: DimensionSet = {
        variability: 'UNKNOWN',
    }

    static create(): RulesBuilder {
        return new RulesBuilder()
    }

    setName(name: string): RulesBuilder {
        this.name = name
        return this
    }

    setTargetPath(path: string): RulesBuilder {
        this.targetPath = path
        return this
    }

    setContext(contextName: string): RulesBuilder {
        this.context = contextName
        return this
    }

    setCondition(conditionString: string): RulesBuilder {
        this.condition = {
            expression: {
                expressionType: 'COMPLEX',
                expressionString: conditionString,
            } as Expressions.ComplexExpression,
        }
        return this
    }

    addDependency(dependency: Dependency): RulesBuilder {
        if (!this.dependencies) {
            this.dependencies = []
        }
        this.dependencies.push(dependency)
        return this
    }
    setPayload(payload: Payloads.Payload): RulesBuilder {
        this.payload = payload
        return this
    }

    setDimensionSet(dimensionSet: string[]): RulesBuilder {
        this.dimensionSet = {
            dimensions: dimensionSet,
            variability: dimensionSet.length ? 'KNOWN' : 'STATIC',
        }
        return this
    }

    setStatic(): RulesBuilder {
        this.dimensionSet = {
            dimensions: [],
            variability: 'STATIC',
        }
        return this
    }

    build(): Rule {
        this.isDefined(this.name, 'Name must be defined')
        this.isDefined(this.targetPath, 'Target path must be defined')
        this.isDefined(this.payload, 'Payload must be defined')
        this.isDefined(this.context, 'Context name must be defined')

        const rule: Rule = {
            name: this.name,
            targetPath: this.targetPath,
            condition: this.condition,
            payload: this.payload,
            context: this.context,
            dimensionSet: this.dimensionSet,
        }
        if (this.dependencies) {
            rule.dependencies = this.dependencies
        }
        return rule
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
