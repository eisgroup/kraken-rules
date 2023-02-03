/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 * or incorporated into any other media without EIS Group prior written consent.
 */

import {
    ConditionEvaluation,
    DefaultValuePayloadResult,
    PayloadResultType,
    RuleEvaluationResults,
} from 'kraken-engine-api'
import { k_toBeApplied, k_toBeIgnored, k_toBeSkipped } from '../toMatchRuleStatus'
import Kind = RuleEvaluationResults.Kind
import RuleEvaluationResult = RuleEvaluationResults.RuleEvaluationResult

expect.extend({ k_toBeSkipped, k_toBeIgnored, k_toBeApplied })

describe('toMatchRuleStatus', () => {
    it('should match when rule is applied', () => {
        const ruleResult = {
            kind: Kind.REGULAR,
            conditionEvaluationResult: {
                conditionEvaluation: ConditionEvaluation.APPLICABLE,
            },
            payloadResult: {
                type: PayloadResultType.DEFAULT,
            } as DefaultValuePayloadResult,
        } as RuleEvaluationResult

        expect(ruleResult).not.k_toBeIgnored()
        expect(ruleResult).not.k_toBeSkipped()
        expect(ruleResult).k_toBeApplied()
    })
    it('should match when rule is skipped', () => {
        const ruleResult = {
            kind: Kind.REGULAR,
            conditionEvaluationResult: {
                conditionEvaluation: ConditionEvaluation.NOT_APPLICABLE,
            },
        } as RuleEvaluationResult

        expect(ruleResult).not.k_toBeIgnored()
        expect(ruleResult).k_toBeSkipped()
        expect(ruleResult).not.k_toBeApplied()
    })
    it('should match when rule is ignored by due to condition error', () => {
        const ruleResult = {
            kind: Kind.REGULAR,
            conditionEvaluationResult: {
                conditionEvaluation: ConditionEvaluation.ERROR,
            },
        } as RuleEvaluationResult

        expect(ruleResult).k_toBeIgnored()
        expect(ruleResult).not.k_toBeSkipped()
        expect(ruleResult).not.k_toBeApplied()
    })
    it('should match when rule is ignored doe to payload error', () => {
        const ruleResult = {
            kind: Kind.REGULAR,
            conditionEvaluationResult: {
                conditionEvaluation: ConditionEvaluation.APPLICABLE,
            },
            payloadResult: {
                type: PayloadResultType.DEFAULT,
                error: {
                    kind: 2,
                    error: {
                        severity: 'critical',
                        message: 'error',
                    },
                },
            } as DefaultValuePayloadResult,
        } as RuleEvaluationResult

        expect(ruleResult).k_toBeIgnored()
        expect(ruleResult).not.k_toBeSkipped()
        expect(ruleResult).not.k_toBeApplied()
    })
})
