/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other
 * media without EIS Group prior written consent.
 */
module.exports = {
    moduleFileExtensions: ['ts', 'tsx', 'js'],
    transform: {
        '^.+\\.ts$': ['@swc/jest'],
    },
    testMatch: ['**/?(*.)test.ts'],
    verbose: true,
    cacheDirectory: './target/tmp/',
    coverageDirectory: './target/coverage',
    coverageThreshold: {
        global: {
            branches: 60,
            functions: 80,
            lines: 80,
            statements: 80,
        },
    },
    collectCoverageFrom: ['**/*[^d\\.].ts', '!**/node_modules/**', '!**/target/**', '!**/__tests__/**', '!**/index.ts'],
    testEnvironment: 'node',
    setupFilesAfterEnv: [],
}
