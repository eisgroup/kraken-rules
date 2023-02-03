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
            branches: 93,
            functions: 96,
            lines: 96,
            statements: 96,
        },
    },
    collectCoverageFrom: [
        '**/*[^d\\.].ts',
        '!**/node_modules/**',
        '!**/target/**',
        '!**/__tests__/**',
        '!**/index.ts',
        '!**/**Error**.ts',
        '!**/Reducer.ts', //interface only
        '!**/RulePayloadHandler.ts', //interface only
        '!**/RepositoryClient.ts', //deprecated
        '!**/info/iterators/**', //deprecated
        '!**/config.ts',
        '!**/DevelopmentLogger.ts',
    ],
    testEnvironment: 'node',
    setupFilesAfterEnv: ['./__tests__/console.setup.js', './__tests__/matchers.setup.ts'],
}
