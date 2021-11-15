package kraken.engine.sanity.check;

import kraken.runtime.engine.EntryPointResult;
import kraken.testproduct.domain.Policy;
import kraken.utils.MockAutoPolicyBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static kraken.testing.matchers.KrakenMatchers.hasNoIgnoredRules;
import static kraken.testing.matchers.KrakenMatchers.hasNoValidationFailures;
import static kraken.testing.matchers.KrakenMatchers.hasRuleResults;
import static kraken.test.KrakenItestMatchers.matchesSnapshot;
import static org.hamcrest.MatcherAssert.assertThat;

public class EngineSanityPolicyCombineTest extends SanityEngineBaseTest {

    @BeforeClass
    public static void beforeAll() {
        start();
    }

    @Test
    public void shouldEvaluatePolicyCombinedEntryPoint() {
        Policy policy = new MockAutoPolicyBuilder().addValidAutoPolicy().build();
        EntryPointResult result = engine.evaluate(policy, "PolicyCombined");

        assertThat(result, hasRuleResults(32));
        assertThat(result, hasNoValidationFailures());
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }
}
