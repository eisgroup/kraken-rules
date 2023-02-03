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
            branches: 85,
            functions: 90,
            lines: 90,
            statements: 90,
        },
    },
    collectCoverageFrom: ['**/*[^d\\.].ts', '!**/node_modules/**', '!**/target/**', '!**/test/**', '!**/index.ts'],
    testEnvironment: 'node',
}
