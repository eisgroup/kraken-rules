module.exports =  {
    moduleFileExtensions: ['ts', 'tsx', 'js'],
    transform: {
        '^.+\\.ts$': ['@swc/jest'],
    },
    verbose: true,
    cacheDirectory: './target/tmp/',
    coverageDirectory: './target/coverage',
    testMatch: ['**/?(*.)test.ts'],
    testEnvironment: 'node',
    collectCoverageFrom: [
        '**/*[^d\\.].ts',
        '!**/node_modules/**',
        '!**/target/**',
        '!**/__tests__/**',
        '!**/index.ts'
    ],
}
