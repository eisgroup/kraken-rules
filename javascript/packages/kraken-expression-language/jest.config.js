const baseConfig = require('../../jest.config.base')

module.exports = {
    ...baseConfig,
    coverageThreshold: {
        global: {
            branches: 85,
            functions: 90,
            lines: 90,
            statements: 90,
        },
    },
}
