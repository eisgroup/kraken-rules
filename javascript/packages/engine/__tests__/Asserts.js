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

const { toMatchSnapshot } = require('jest-snapshot');
const { Sort } = require('declarative-js');
const { ValidationStatusReducer } = require('./ValidationStatusReducer')
const { FieldMetadataReducer } = require('../src/engine/results/field_metadata_reducer/FieldMetadataReducer')
const { matcherHint, printReceived, printExpected } = require('jest-matcher-utils');

expect.extend({
    k_toHaveExpressionsFailures,
    k_toBeDate,
    k_toBeDateTime,
    k_toBeTodayDate,
    k_toBeValidRuleResult,
    k_toMatchResultsSnapshots,
    k_toMatchResultsStats
});

function ok() {
    return {
        message: () => `ok`,
        pass: true
    };
}

const fail = (matcher, header, expected, received) => `${matcherHint('.' + matcher)}
${header}:
  Expected: ${printExpected(expected)}
  Received: ${printReceived(received)}
`
const pass = (matcher, header, expected, received) => fail(matcher, 'Received', expected, received)

function k_toMatchResultsStats(received, argument) {
    if (this.isNot) {
        throw new Error("Matcher 'k_toMatchResultsStats' can not be used with '.not'")
    }

    const vr = new ValidationStatusReducer().reduce(received);
    const total = received.getApplicableResults()

    if (argument.total !== 0 && argument.total !== total.length) {
        return {
            message: () => fail('k_toMatchResultsStats', 'Total results count must be', argument.total, total.length),
            pass: false
        };
    }
    if (argument.critical !== undefined && vr.critical.length !== argument.critical) {
        return {
            message: () => fail(
                'k_toMatchResultsStats',
                'Expected reduced ValidationStatusReducer results \'critical\' errors count',
                argument.critical,
                "Actual count is: " + vr.critical.length + ":\n" + vr.critical.map(m => `Rule name: '${m.ruleName}', message: '${m.message}'`).join("\n\t")
            ),
            pass: false
        };
    }
    if (argument.warning !== undefined && vr.warning.length !== argument.warning) {
        return {
            message: () => fail(
                'k_toMatchResultsStats',
                'Expected reduced ValidationStatusReducer results \'warning\' errors count',
                argument.warning,
                vr.warning.length
            ),
            pass: false
        };
    }
    if (argument.info !== undefined && vr.info.length !== argument.info) {
        return {
            message: () => fail(
                'k_toMatchResultsStats',
                'Expected reduced ValidationStatusReducer results \'info\' count',
                argument.info,
                vr.info.length
            ),
            pass: false
        };
    }
    const disabledCount = total
        .filter(result => result.payloadResult.type & 128)
        .filter(result => result.payloadResult.accessible === false)
        .length
    if (argument.disabled !== undefined && disabledCount !== argument.disabled) {
        return {
            message: () => fail(
                'k_toMatchResultsStats',
                'Reduced FieldMetadata results \'disabled\' fields count',
                argument.disabled,
                disabledCount
            ),
            pass: false
        };
    }
    const hiddenCount = total
        .filter(result => result.payloadResult.type & 256)
        .filter(result => result.payloadResult.visible === false)
        .length
    if (argument.hidden !== undefined && hiddenCount !== argument.hidden) {
        return {
            message: () => fail(
                'k_toMatchResultsStats',
                'Reduced FieldMetadata results \'hidden\' fields count',
                argument.hidden,
                hiddenCount
            ),
            pass: false
        };
    }
    return ok();
}

function k_toMatchResultsSnapshots(received, argument) {
    const fm = new FieldMetadataReducer().reduce(received);
    const vr = new ValidationStatusReducer().reduce(received);
    Object.entries(received.results)
        .forEach(e => {
            // remove data context from results
            // data context in snapshots adding only data noise
            delete e[1].dataContext
            e[1].ruleResults.forEach(ruleResult => {
                // sorting dependencies for snapshot.
                // Backend rule impl has set of dependencies
                // generating each time in different order
                ruleResult.ruleInfo.overrideDependencies
                    && ruleResult.ruleInfo.overrideDependencies.sort(Sort.ascendingBy(
                        x => x.contextName,
                        x => x.contextFieldName || ""
                    ))
            })
            return e;
        })
    // received
    const results = {
        'Field Metadata reduced results': fm,
        'Validation results': vr,
        'Raw results': received
    };

    return toMatchSnapshot.call(
        this,
        results,
        {
            'Raw results': {
                evaluationTimestamp: expect.any(Date)
            }
        },
        'Results + reducer results',
    );
}

function k_toBeTodayDate(received, argument) {
    const res = received.getDate() === new Date().getDate();
    if (!res) {
        return {
            message: () => `Date ${received.getDate()} is not equal to ${argument.getDate()}`,
            pass: true
        };
    }
    return ok();
}

function k_toBeDate(received, argument) {
    const res = received.getDate() === argument.getDate();
    if (!res) {
        return {
            message: () => `Date ${received.getDate()} is not equal to ${argument.getDate()}`,
            pass: true
        };
    }
    return ok();
}

function k_toBeDateTime(received, argument) {
    const receivedD = `${received.getDate()} ${received.getHours()} ${received.getMinutes()} ${received.getSeconds()}`;
    const argumentD = `${argument.getDate()} ${argument.getHours()} ${argument.getMinutes()} ${argument.getSeconds()}`;

    const res = receivedD === argumentD;
    if (!res) {
        return {
            message: () => `Date time ${received.getDate()} is not equal to ${argument.getDate()}`,
            pass: false
        };
    }
    return ok();
}

function k_toHaveExpressionsFailures(received, argument) {
    const allRuleResults = received.getAllRuleResults();
    const failedConditions = allRuleResults
        .filter(r => !!r.conditionEvaluationResult.error);
    const failedPayloads = allRuleResults
        .filter(r => !r.conditionEvaluationResult.error)
        .filter(r => r.payloadResult && !!r.payloadResult.error)
    const numberOfFailures = failedConditions.length + failedPayloads.length;
    const condition = argument !== undefined
        ? numberOfFailures === argument
        : numberOfFailures > 0;
    if (!condition) {
        const formatRuleDescription = (errFormat) => `

Rule name: "${errFormat.rule.ruleName}",
Context: "${errFormat.rule.context}",
Field: "${errFormat.rule.field}"
Expression: ${printReceived(errFormat.expression)}`

        const conditionsFailsFormated = failedConditions
            .map(rer => ({
                rule: {
                    ruleName: rer.rule.name,
                    context: rer.rule.context,
                    field: rer.rule.targetPath
                },
                expression: rer.rule.condition.expression.translatedExpressionString
            }))

        const payloadFormated = failedPayloads
            .map(rer => ({
                rule: {
                    ruleName: rer.rule.name,
                    context: rer.rule.context,
                    field: rer.rule.targetPath
                },
                expression: rer.rule.payload.assertionExpression
                    ? rer.rule.payload.assertionExpression.translatedExpressionString
                    : rer.rule.payload.valueExpression.translatedExpressionString
            }))
        return {
            message: () =>
                `Expected expressions to be evaluated with ${printExpected(argument || 0)} failures, but received ${printReceived(numberOfFailures)}: ${failedConditions.length
                    ? "\ncondition expressions failed in: " + conditionsFailsFormated.map(formatRuleDescription)
                    : []
                } ${failedPayloads.length
                    ? "\npayload expressions failed in: " + payloadFormated.map(formatRuleDescription)
                    : []
                }
                `,
            pass: false
        };
    }
    return {
        message: () =>
            `Expected expressions to be evaluated without ${argument || ""} failures, but received ${numberOfFailures}: ${failedConditions.length
                ? "\ncondition expressions failed in: " + JSON.stringify(failedConditions, undefined, 2)
                : []
            } ${failedPayloads.length
                ? "\npayload expressions failed in: " + JSON.stringify(failedPayloads, undefined, 2)
                : []
            }
                `,
        pass: true
    };
}

function k_toBeValidRuleResult(received) {
    const payload = JSON.stringify(received.payloadResult, undefined, 2);
    if (received.payloadResult.error) {
        return {
            message: () => `payload1 ${payload} is evaluated with error ${received.payloadResult.error}`,
            pass: false
        }
    }
    if (received.payloadResult.success === false) {
        return {
            message: () => `payload ${payload} is not evaluated is evaluated to success`,
            pass: false
        }
    }
    return {
        message: () => `payload ${payload} is evaluated is evaluated to success`,
        pass: true
    }
}