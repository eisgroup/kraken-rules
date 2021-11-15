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

public class EngineSanitySizePayloadTest extends SanityEngineBaseTest {

    @BeforeClass
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }

    @Test
    public void shouldSucceedWithSize2() {
        final Policy policy = new Policy();
        policy.setPolicies(List.of("1", "2"));
        final EntryPointResult result = engine.evaluate(policy, "SizePayload");

        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasApplicableResults(3));
        assertThat(result, hasNoValidationFailures());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldFailWithSize3() {
        final Policy policy = new Policy();
        policy.setPolicies(List.of("1", "2", "3"));
        final EntryPointResult result = engine.evaluate(policy, "SizePayload");

        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasApplicableResults(3));
        assertThat(result, hasValidationFailures(2));
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldHandleNullCollection() {
        final Policy policy = new Policy();
        policy.setPolicies(null);
        final EntryPointResult result = engine.evaluate(policy, "SizePayload");

        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasApplicableResults(3));
        assertThat(result, hasValidationFailures(2));
        assertThat(result, matchesSnapshot());
    }
}
