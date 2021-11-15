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
import { Reducer } from "declarative-js";
import toObject = Reducer.toObject;
import flat = Reducer.flat;

import { Rule, Expressions, Payloads, Condition } from "kraken-model";

import { EntryPointResult } from "../../dto/EntryPointResult";
import { ContextDataProviderFactory } from "../contexts/data/extraction/ContextDataProviderFactory";
import { DataContext } from "../contexts/data/DataContext";
import { DataContextBuilder } from "../contexts/data/DataContextBuilder";
import { ExecutionSession } from "../ExecutionSession";

import { EntryPointBundle } from "../../models/EntryPointBundle";
import EntryPointEvaluation = EntryPointBundle.EntryPointEvaluation;

import { RuleEvaluationResults } from "../../dto/RuleEvaluationResults";
import RuleEvaluationResult = RuleEvaluationResults.RuleEvaluationResult;

import { RulePayloadProcessor } from "../RulePayloadProcessor";
import { FieldEvaluationResult } from "../../dto/FieldEvaluationResult";
import { ConditionEvaluationResult } from "../../dto/ConditionEvaluationResult";
import { payloadResultTypeChecker } from "../results/PayloadResult";
import { ContextDataExtractor } from "../contexts/data/extraction/ContextDataExtractor.types";
import { ContextFieldInfo } from "../../dto/ContextFieldInfo";
import { logger } from "../../utils/DevelopmentLogger";
import {
    DataContextDependency,
    DataContextUpdater
} from "../contexts/data/updater/DataContextUpdater";
import { RuleConditionProcessor } from "../RuleConditionProcessor";
import { RuleInfo } from "../results/RuleInfo";
import { ErrorCode, KrakenRuntimeError } from "../../error/KrakenRuntimeError";

export interface RuleEvaluation {
    rule: Rule;
    dataContext: DataContext;
}

export interface RuleEvaluationInstance {
    dataContext: DataContext;
    result: RuleEvaluationResult;
}

/**
 * Evaluates rules in order from {@link EntryPointEvaluation#rules}
 * @since 1.0.29
 */
export class OrderedEvaluationLoop {

    static getInstance(options: {
        dataContextUpdater: DataContextUpdater,
        ruleConditionProcessor: RuleConditionProcessor,
        rulePayloadProcessor: RulePayloadProcessor,
        contextDataExtractor: ContextDataExtractor,
        contextBuilder: DataContextBuilder,
        restriction?: {}
    }
    ): OrderedEvaluationLoop {
        return new OrderedEvaluationLoop(
            options.ruleConditionProcessor,
            options.rulePayloadProcessor,
            new ContextDataProviderFactory(
                options.contextDataExtractor,
                options.contextBuilder,
                options.restriction
            ),
            options.dataContextUpdater
        );
    }

    constructor(
        private readonly ruleConditionProcessor: RuleConditionProcessor,
        private readonly rulePayloadProcessor: RulePayloadProcessor,
        private readonly contextDataProviderFactory: ContextDataProviderFactory,
        private readonly dataContextUpdater: DataContextUpdater
    ) {
    }

    evaluate(evaluation: EntryPointEvaluation, data: object, session: ExecutionSession): EntryPointResult {
        const dataProvider = this.contextDataProviderFactory.createContextProvider(data);

        logger.group("Context instance extraction");
        const evalInstances = evaluation.rules
            .map(rule => {
                const groupName = `Rule: ${rule.name}`;
                logger.debug(groupName);
                const rei = dataProvider
                    .resolveContextData(rule.context)
                    .map(dataContext => ({ rule, dataContext }));
                return rei;
            });
        logger.groupEnd("Context instance extraction");

        logger.group("Rules evaluation");
        const results = evalInstances
            .reduce(flat, [])
            .map(rule => {
                // tslint:disable-next-line: max-line-length
                logger.debug(`Processing: rule '${rule.rule.name}', on ${rule.dataContext.contextName}:${rule.dataContext.contextId}`);
                const result = this.evaluateRule(rule, session);
                return {
                    contextFieldInfo: new ContextFieldInfo(rule.dataContext, result.ruleInfo.targetPath),
                    ruleResults: [result]
                };
            })
            .reduce(toObject(resultId, f => f, concatRuleResults), {});
        validate(results);
        logger.groupEnd("Rules evaluation");

        logger.info({ results });
        return new EntryPointResult(results);
    }

    private evaluateRule(ruleEvaluation: RuleEvaluation, session: ExecutionSession): RuleEvaluationResult {
        extractConditionDependencies(ruleEvaluation.rule.condition)
            .forEach(dependency => this.dataContextUpdater.update(
                ruleEvaluation.dataContext,
                dependency
            ));
        const conditionResult = this.ruleConditionProcessor.evaluateCondition(
            ruleEvaluation.dataContext,
            session,
            ruleEvaluation.rule.condition
        );
        const isApplicable = ConditionEvaluationResult.isApplicable(conditionResult);
        logger.debug(`Rule '${ruleEvaluation.rule.name}' is '${isApplicable ? "applicable" : "not applicable"}'`);
        if (isApplicable) {
            extractPayloadDependencies(ruleEvaluation.rule.payload)
                .forEach(dependency => this.dataContextUpdater.update(
                    ruleEvaluation.dataContext,
                    dependency
                ));
            return this.rulePayloadProcessor.processRule(ruleEvaluation, session);
        } else {
            return {
                kind: RuleEvaluationResults.Kind.NOT_APPLICABLE,
                ruleInfo: new RuleInfo(ruleEvaluation.rule),
                conditionEvaluationResult: conditionResult
            };
        }
    }

}

// tslint:disable: triple-equals
function validate(results: Record<string, FieldEvaluationResult>): void {
    Object.keys(results)
        .forEach(key => {
            const rulesOnOneField = results[key].ruleResults
                .filter(rr => ConditionEvaluationResult.isApplicable(rr.conditionEvaluationResult)
                    && !RuleEvaluationResults.isNotApplicable(rr)
                    && payloadResultTypeChecker.isDefault(rr.payloadResult)
                    && rr.payloadResult.error === undefined
                );
            if (rulesOnOneField.length > 1) {
                throw new KrakenRuntimeError(
                    ErrorCode.MULTIPLE_DEFAULT,
                    "On field '"
                    + key
                    + "' evaluated '"
                    + rulesOnOneField.length
                    + "' default rules: '"
                    + rulesOnOneField.map(rr => rr.ruleInfo.ruleName).join(", ")
                    + "'. One default rule per field can be evaluated."
                );
            }
        });
}

function concatRuleResults(fer1: FieldEvaluationResult, fer2: FieldEvaluationResult): FieldEvaluationResult {
    fer1.ruleResults = fer1.ruleResults.concat(fer2.ruleResults);
    return fer1;
}

export function resultId(fieldResult: FieldEvaluationResult): string {
    const info = fieldResult.contextFieldInfo;
    return `${info.contextName}:${info.contextId}:${info.fieldName}`;
}

function extractPayloadDependencies(payload: Payloads.Payload): DataContextDependency[] {
    const dependencies: DataContextDependency[] = [];

    // validation error message template variables extraction
    if (Payloads.isValidationPayload(payload)
        && payload.errorMessage
        && payload.errorMessage.templateExpressions
        && payload.errorMessage.templateExpressions.length
    ) {
        for (const te of payload.errorMessage.templateExpressions) {
            if (te.expressionType === "COMPLEX" && te.expressionVariables) {
                for (const variable of te.expressionVariables) {
                    dependencies.push({ contextName: variable.name });
                }
            }
        }

    }

    if (Payloads.isAssertionPayload(payload)
        && Expressions.isComplex(payload.assertionExpression)
        && payload.assertionExpression.expressionVariables
    ) {
        payload.assertionExpression.expressionVariables
            .forEach(v => dependencies.push({ contextName: v.name }));
    }

    if (Payloads.isDefaultValuePayload(payload)
        && Expressions.isComplex(payload.valueExpression)
        && payload.valueExpression.expressionVariables
    ) {
        payload.valueExpression.expressionVariables
            .forEach(v => dependencies.push({ contextName: v.name }));
    }

    return dependencies;
}

function extractConditionDependencies(condition?: Condition): DataContextDependency[] {
    const dependencies: DataContextDependency[] = [];

    if (condition && Expressions.isComplex(condition.expression) && condition.expression.expressionVariables) {
        condition.expression.expressionVariables
            .forEach(v => dependencies.push({ contextName: v.name }));
    }

    return dependencies;
}
