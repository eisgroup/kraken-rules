# Kraken Typescript Engine

## Tests

To run tests enter command in console:
`npm test`
This command will run all tests without coverage

To run tests with coverage enter command in console:
`npm run test:coverage`

Kraken engine have its own matchers in jest, that can be used `expect(value).<matcher>`

```typescript
expect(results: EntryPointResult).k_toHaveExpressionsFailures(num?: number): void;
expect(result: RuleEvaluationResult).k_toBeValidRuleResult(): void;
expect(date: Date).k_toBeDate(argument: Date): void;
expect(date: Date).k_toBeDateTime(argument: Date): void;
expect(date: Date).k_toBeTodayDate(): void;
expect(results: EntryPointResult).k_toMatchResultsSnapshots();
/**
 * Matches rule results according to provided parameters, 
 * and check to haven no expression failures. For assert it uses
 * '.k_toHaveExpressionsFailures(0)'
 * @param stats     statistics results should match
 */
expect(results: EntryPointResult).k_toMatchResultsStats(stats: {
    total: number,
    critical?: number,
    warning?: number,
    info?: number,
    disabled?: number,
    hidden?: number
});
```

## Lint
To lint project enter command in console:
`npm run lint`

TSLint can fix some errors. To do this enter command in console:
`npm run lint -- --fix`

## Committing
Before commiting run this command:
`npm run precommit` this will fix some linter issues if they are present, run linter, 
tests with coverage and compile typescript to javascript
