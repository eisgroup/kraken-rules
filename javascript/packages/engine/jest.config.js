const baseConfig = require('../../jest.config.base.js')

module.exports = {
    ...baseConfig,
    coverageThreshold: {
        global: {
            branches: 90,
            functions: 96,
            lines: 96,
            statements: 96,
        },
    },
    collectCoverageFrom: [
        ...baseConfig.collectCoverageFrom,
        '!**/**Error**.ts',
        '!**/Reducer.ts', //interface only
        '!**/RulePayloadHandler.ts', //interface only
        '!**/RepositoryClient.ts', //deprecated
        '!**/info/iterators/**', //deprecated
        '!**/config.ts',
        '!**/DevelopmentLogger.ts',
    ],
    setupFilesAfterEnv: ['./__tests__/console.setup.js', './__tests__/matchers.setup.ts'],
}
