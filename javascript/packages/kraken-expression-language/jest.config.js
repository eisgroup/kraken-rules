module.exports = {
    moduleFileExtensions: ["ts", "tsx", "js"],
    transform: {
        "^.+\\.ts$": "ts-jest"
    },
    testMatch: ["**/?(*.)test.ts"],
    verbose: true,
    cacheDirectory: "./target/tmp/",
    coverageDirectory: "./target/coverage",
    coverageThreshold: {
        global: {
            branches: 85,
            functions: 95,
            lines: 95,
            statements: 95
        }
    },
    collectCoverageFrom: [
        "**/*[^d\\.].ts",
        "!**/node_modules/**",
        "!**/target/**",
        "!**/test/**",
        "!**/index.ts"
    ],
    testEnvironment: "node"
}