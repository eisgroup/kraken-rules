module.exports = {
    moduleFileExtensions: ['ts', 'tsx', 'js'],
    transform: {
        '^.+\\.ts$': ['@swc/jest'],
    },
    testMatch: ['**/?(*.)test.ts'],
    verbose: true,
    testEnvironment: 'node',
}
