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

import { Condition, Payloads, Dependency, Rule, Expressions } from "kraken-model";

export class RulesBuilder {
    private name: string;
    private context: string;
    private targetPath: string;
    private condition: Condition;
    private payload: Payloads.Payload;
    private dependencies: Dependency[];
    private isDimensional = false;

    static create(): RulesBuilder {
        return new RulesBuilder();
    }

    setName(name: string): RulesBuilder {
        this.name = name;
        return this;
    }

    setTargetPath(path: string): RulesBuilder {
        this.targetPath = path;
        return this;
    }

    setContext(contextName: string): RulesBuilder {
        this.context = contextName;
        return this;
    }

    setCondition(conditionString: string): RulesBuilder {
        this.condition = {
            expression: {
                expressionType: "COMPLEX",
                expressionString: conditionString
            } as Expressions.ComplexExpression
        };
        return this;
    }

    addDependency(dependency: Dependency): RulesBuilder {
        if (!this.dependencies) {
            this.dependencies = [];
        }
        this.dependencies.push(dependency);
        return this;
    }
    setPayload(payload: Payloads.Payload): RulesBuilder {
        this.payload = payload;
        return this;
    }

    setDimensional(): RulesBuilder {
        this.isDimensional = true;
        return this;
    }

    build(): Rule {
        const rule: Rule = {
            name: requireValue(this.name, "Name must be defined"),
            targetPath: requireValue(this.targetPath, "Target path must be defined"),
            condition: this.condition,
            payload: requireValue(this.payload, "Payload must be defined"),
            context: requireValue(this.context, "Context name must be defined"),
            dimensional: this.isDimensional
        };
        if (this.dependencies) {
            rule.dependencies = this.dependencies;
        }
        return rule;
    }
}

const requireValue = <T>(value: T, err: string) => {
    if (!value) {
        throw new Error(err);
    }
    return value;
};
