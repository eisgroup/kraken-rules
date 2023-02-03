# 👣 Kraken Tracer Guides 👣

- [Writing Unit Tests](#writing-unit-tests)
  - [Using provided test utilities](#using-provided-test-utilities)
  - [Configuring Tracer for testing manually](#configuring-tracer-for-testing-manually)

## Writing unit tests

### Using provided test utilities

Kraken Tracer module provides test utility classes and removes the need for manual configuration in tests. 
To use provided tooling - add the following dependency to the maven module where you want to write
tests for Tracer:

```xml
<dependency>
    <groupId>kraken.tracer</groupId>
    <artifactId>kraken-tracer</artifactId>
    <classifier>tests</classifier>
    <type>test-jar</type>
    <scope>test</scope>
</dependency>
```

Then, make your test class extend from `AbstractTracerTest` - it will automatically configure `Tracer`
and inject with testing specific trace observer before test execution. `AbstractTracerTest` also
provides methods to retrieve `TraceResult` and `TracerObserver` invocation count.

```java
import kraken.tracer.TraceResult;

public class MyClassTest extends AbstractTraceTest {

    @Test
    public void anInterestingTest() {
        <...>

        TraceResult result = getTestObserver().getResult();
        
        assertThat(<...>)
    }
}
```

#### Operation Matcher

`AbstractTracerTest` also provides a matcher to match trace operation results, usage example:

```java
assertThat(operation, operation("first name", 2, 
        operation("child operation one", 0),
        operation("child operation two", 1)))
```

### Configuring Tracer for testing manually

If the above solution is not an option, you can configure `Tracer` by following these steps: 

Firstly, implement `TraceObserver`:

```java
import kraken.tracer.TraceResult;
import kraken.tracer.observer.TraceObserver;

public class MyObserver extends TraceObserver {

    @Override
    public void observe(TraceResult result) {
        // do something here
    }
    
}
```

Then, implement `TracerConfigurer`:

```java
import kraken.tracer.TracerConfigurer;
import kraken.tracer.TracerToggle;
import kraken.tracer.observer.TraceObserver;

public class MyTestingConfiguration implements TracerConfigurer {

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public List<TraceObserver> traceObservers() {
      return List.of(<...>);
  }
}
```

Then in test resources create META-INF/services directory, and add file kraken.tracer.TracerConfigurer containing
single line:

```xml
fully.qualified.name.to.MyTestingConfiguration
```