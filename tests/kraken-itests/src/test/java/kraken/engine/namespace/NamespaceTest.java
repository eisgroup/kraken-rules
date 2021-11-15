package kraken.engine.namespace;

import kraken.runtime.engine.EntryPointResult;
import kraken.testing.matchers.KrakenMatchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static kraken.testing.matchers.KrakenMatchers.hasNoIgnoredRules;
import static org.junit.Assert.assertThat;

/**
 * @author psurinin
 */
public class NamespaceTest extends NamespaceBaseTest {

    @BeforeClass
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }

    @Test
    public void shouldEvaluateInSeveralNamespaces() {
        final EntryPointResult resultBar =
                engine.evaluate(getDataObject(), "constraints");
        assertThat(resultBar, KrakenMatchers.hasValidationFailures(1));
        assertThat(resultBar, hasNoIgnoredRules());
    }
}
