module.exports = {
    moduleFileExtensions: ['ts', 'tsx', 'js'],
    transform: {
        '^.+\\.ts$': ['@swc/jest'],
    },
    testMatch: ['**/?(*.)test.ts'],
    verbose: true,
    cacheDirectory: './target/tmp/',
    coverageDirectory: './target/coverage',
    collectCoverageFrom: ['**/*[^d\\.].ts', '!**/node_modules/**', '!**/target/**', '!**/test/**', '!**/index.ts'],
    testEnvironment: 'node',
}
