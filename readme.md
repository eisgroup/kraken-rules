# 🦑 Kraken 🦑

  - [Writing Sanity test](#writing-sanity-test)
  - [Commit Gating (CG)](#commit-gating-cg)
  - [Test Application](#test-application)
    - [UI](#ui)
    - [Backend](#backend)
  - [Publishing kraken](#publishing-kraken)
    - [Backend](#backend-1)
    - [UI](#ui-1)
  - [Backend maven profiles](#backend-maven-profiles)
 
## Write Sanity Test

### Add Rules

- Add rules to `.\tests\kraken-test-product\src\main\resources\database\`

```
EntryPoint "policyNumber" {
    "assert-rule",
    "visibility-rule"
}

Rule "assert-rule" On AutoPolicySummaryCtx.policyNumber {
    Assert false
    Warning "code" : "Secret code must not be entered at this moment"
}

Rule "visibility-rule" On AutoPolicySummaryCtx.policyNumber {
    Set Hidden
}
```

### Implement Test in Java

In maven project `kraken-itests` in package `kraken.engine.sanity.check` create new class
`EngineSanity<FunctionalityToTest>Test.java`

```java
import static kraken.testing.matchers.KrakenMatchers.*;

// Extend class SanityEngineBaseTest to have prepared to use engine
public final class EngineSanityAAATest extends SanityEngineBaseTest {

    @BeforeClass
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }

    @Test
    public void shouldExecutePolicyNumberEntryPoint() {
        final EntryPointResult result = engine.evaluate(dataObject, "policyNumber");
        // check not to have expression errors
        assertThat(result, hasNoExpressionErrors());
        // engine produced one rule result
        assertThat(result, hasRuleResults(1));
        // one validation rule failed
        // optional if no rules have failed:
        // assertThat(result, hasNoValidationFailures());
        assertThat(result, hasValidationFailures(1));
        // make snapshot of results and compare on each test
        // to update snapshots, delete old one, and new one will generate
        assertThat(result, matchesSnapshot());
    }
}
```

### Generate Test Data

To generate test data for TypeScript tests, add test info to `tests/kraken-itests/sanity.data.json`
Test info looks like: 
```json
  {
      "id": "Dimensional-state-CA",
      "entryPointName": "Dimensional",
      "delta": true,
      "context": {
        "state": "CA"
      }
  }
```
 - `id: string` - unique identifier of the test case. This id is used to generate dimension set for a specific 
    test case. This i must be used as 3rd parameter in TypeScript `sanityEngine` to load specific bundle.
 - `entryPointName: stirng` - entryPoint name to use. It will generated to use in `sanityEngine` as a second parameter.
 - `delta?: boolean` - if true it will be loaded to typescript repository last 
        and bundle will be generated only for dimensional rules.
 - `context?: {object}` - context to generate rules. Dimension filter is implemented 
     in sanity repo, which filters by "state" dimension
     in that case rules below will be filtered by dimension state,
 ```
@Dimension("state", "CA")
Rule "Set AddressInfo.postalCode to the state name" On AddressInfo.postalCode {
    Reset To "CA"
}

@Dimension("state", "AZ")
Rule "Set AddressInfo.postalCode to the state name" On AddressInfo.postalCode {
    Reset To "AZ"
}
```
 
Run `mvn clean install` and bundle will be generated

### Implement Test in TypeScript

Create file in `javascript\packages\engine\__tests__\sanity\AAA.sanity.test.ts`

```typescript
import { sanityMocks } from "./_AutoPolicyObject.mocks";
import { sanityEngine } from "./_SanityEngine";

describe("Engine Sanity Visibility Payload Test", () => {
    // has data objects with empty, valid and invalid policy
    const { valid } = sanityMocks;
    it("should execute 'policyNumber' entrypoint", () => {
        // sanityEngine accepts as a first parameter only object type of `AutoPolicySummary`
        // entry point name is also typed, so invalid (not generated) entry point name can not be used
        const results = sanityEngine.evaluate(valid(), "policyNumber");
        // asserts must be the same as in java implementation
        // `k_toMatchResultsStats` will not check for expression failures, 
        // because expression error is not a failed validation
        expect(results).k_toMatchResultsStats({ total: 1, warning: 1, critical: 0 });
        // to check for expression failures:
        expect(results).k_toHaveExpressionsFailures(1);
        // will make snapshots of raw results and results reduced with FieldMetadata Reducer and ValidationStatus Reducer
        expect(results).k_toMatchResultsSnapshots();
    });
});
```

If test case has same entrypioint name with different dimensions, `generated_test_id` from entry in `tests/kraken-itests/sanity.data.json`
must be passed as a 3rd parameter.

`const results = sanityEngine.evaluate(valid(), "policyNumber", "generated_test_id");`

## Test Application

Demo application is used to test/demo Kraken engines.
To use demo application with latest changes first start backend app, then ui app

### UI

To start application run this commands:
```bash
cd javascript
npm install
npx lerna bootstrap
cd packages/test-app
npm run serve
```
Application will start on `localhost:5000`

### Backend

To build run this commands:
```bash
cd kraken-spring-rest
mvn clean install
mvn spring-boot:run
```

Application will start on `localhost:8888`

Available endpoints are documented in [swagger](http://localhost:8888/swagger-ui.html) on 
`localhost:8888/swagger-ui.html`


## Kraken Code Coverage Report

Aggregate code coverage report can be generated for kraken codebase by invoking `mvn verify test -Pcode-coverage` command
from root directory.

Aggregated report will be available in tools/kraken-code-coverage-report/target/site/jacoco-aggregate directory.

### Including additional modules to aggregated report

To include additional maven modules to aggregated code coverage report a dependency to that maven module needs to be 
declared under dependencies section in tools/kraken-code-coverage-report/pom.xml file:

```xml
<dependencies>
    ....
    <dependency>
        <groupId>my.package</groupId>
        <artifactId>artifact-id-to-include-in-report</artifactId>
    </dependency>
</dependencies>
```
