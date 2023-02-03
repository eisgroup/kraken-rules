/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 * or incorporated into any other media without EIS Group prior written consent.
 */

import { k_toBeValidRuleResult } from '../toBeValidRuleResult'
import { ErrorAwarePayloadResult, RuleEvaluationResults } from 'kraken-engine-api'

import ApplicableRuleEvaluationResult = RuleEvaluationResults.ApplicableRuleEvaluationResult

expect.extend({ k_toBeValidRuleResult })

describe('.k_toBeValidRuleResult', () => {
    it('Should fail when error is present', () => {
        const ruleResult = {
            kind: 2,
            payloadResult: {
                error: {
                    error: {
                        severity: 'critical',
                    },
                    kind: 2,
                },
            } as ErrorAwarePayloadResult,
        } as ApplicableRuleEvaluationResult

        expect(ruleResult).not.k_toBeValidRuleResult()
    })

    it('Should fail when evaluating to false', () => {
        const ruleResult = {
            kind: 2,
            payloadResult: {
                success: false,
            },
        } as ApplicableRuleEvaluationResult

        expect(ruleResult).not.k_toBeValidRuleResult()
    })

    it('Should not fail when evaluating to false', () => {
        const ruleResult = {
            kind: 2,
            payloadResult: {
                success: true,
            },
        } as ApplicableRuleEvaluationResult

        expect(ruleResult).k_toBeValidRuleResult()
    })
})
