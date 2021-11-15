package kraken.engine.sanity.check;

import kraken.runtime.engine.EntryPointResult;
import kraken.testproduct.domain.Policy;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static kraken.testing.matchers.KrakenMatchers.*;
import static org.junit.Assert.assertThat;
import static kraken.test.KrakenItestMatchers.matchesSnapshot;

public class EngineSanitySizeRangePayloadTest extends SanityEngineBaseTest {

    @BeforeClass
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }


    @Test
    public void shouldFailOnLess() {
        final Policy policy = new Policy();
        policy.setPolicies(List.of("1"));
        final EntryPointResult result = engine.evaluate(policy, "SizeRangePayload");

        assertThat(result, hasRuleResults(1));
        assertThat(result, hasValidationFailures(1));
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldFailOnMore() {
        final Policy policy = new Policy();
        policy.setPolicies(List.of("1", "2", "3", "4"));
        final EntryPointResult result = engine.evaluate(policy, "SizeRangePayload");

        assertThat(result, hasRuleResults(1));
        assertThat(result, hasValidationFailures(1));
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldSuccessOnInRange() {
        final Policy policy = new Policy();
        policy.setPolicies(List.of("1", "2", "3"));
        final EntryPointResult result = engine.evaluate(policy, "SizeRangePayload");

        assertThat(result, hasRuleResults(1));
        assertThat(result, hasNoValidationFailures());
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }
}
