module.exports = {
    moduleFileExtensions: ["ts", "tsx", "js"],
    transform: {
        "^.+\\.ts$": "ts-jest"
    },
    testMatch: ["**/?(*.)test.ts"],
    verbose: true,
    testEnvironment: "node"
}