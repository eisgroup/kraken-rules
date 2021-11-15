package kraken.engine.sanity.check;

import kraken.runtime.engine.EntryPointResult;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.SecondaryInsured;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static kraken.testing.matchers.KrakenMatchers.hasNoIgnoredRules;
import static kraken.test.KrakenItestMatchers.matchesSnapshot;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class EngineSanityMultipleChildrenTest extends SanityEngineBaseTest {

    @BeforeClass
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }

    @Test
    public void shouldFlatAllElementsInSubArraysAndApplyDefaults() {
        final Policy policy = new Policy();
        final ArrayList<SecondaryInsured> si = new ArrayList<>();
        si.add(new SecondaryInsured("mock", "1"));
        si.add(new SecondaryInsured("mock", "2"));
        policy.setMultipleInsureds(
                new SecondaryInsured("mock", "3"),
                List.of(new SecondaryInsured("mock", "4"), new SecondaryInsured("mock", "5")),
                List.of(new SecondaryInsured("mock", "6"), new SecondaryInsured("mock", "7")),
                si
        );
        final EntryPointResult result = engine.evaluate(policy, "MultipleInsureds-default");
        assertThat(policy.getOneInsured().getName(), is("new"));
        assertThat(policy.getMultiInsureds1().get(0).getName(), is("new"));
        assertThat(policy.getMultiInsureds1().get(1).getName(), is("new"));
        assertThat(policy.getMultiInsureds2().get(0).getName(), is("new"));
        assertThat(policy.getMultiInsureds2().get(1).getName(), is("new"));
        assertThat(policy.getMultipleInsureds().get(0).getName(), is("new"));
        assertThat(policy.getMultipleInsureds().get(1).getName(), is("new"));
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldFlatAllElementsInSubArraysIgnoringNullsAndNotFail() {
        final Policy policy = new Policy();
        final ArrayList<SecondaryInsured> si = new ArrayList<>();
        si.add(null);
        si.add(new SecondaryInsured("mock", "99"));
        policy.setMultipleInsureds(
                new SecondaryInsured("mock", "6"),
                List.of(new SecondaryInsured("mock", "2"), new SecondaryInsured("mock", "3")),
                List.of(new SecondaryInsured("mock", "4"), new SecondaryInsured("mock", "5")),
                si
        );
        engine.evaluate(policy, "MultipleInsureds-default");

        policy.setMultipleInsureds(
                new SecondaryInsured("mock", "1"),
                List.of(new SecondaryInsured("mock", "2"), new SecondaryInsured("mock", "3")),
                si,
                List.of(new SecondaryInsured("mock", "4"), new SecondaryInsured("mock", "5"))
        );
        engine.evaluate(policy, "MultipleInsureds-default");

        policy.setMultipleInsureds(
                new SecondaryInsured("mock", "1"),
                si,
                List.of(new SecondaryInsured("mock", "2"), new SecondaryInsured("mock", "3")),
                List.of(new SecondaryInsured("mock", "4"), new SecondaryInsured("mock", "5"))
        );
        engine.evaluate(policy, "MultipleInsureds-default");

        policy.setMultipleInsureds(
                null,
                si,
                List.of(new SecondaryInsured("mock", "12"), new SecondaryInsured("mock", "13")),
                List.of(new SecondaryInsured("mock", "14"), new SecondaryInsured("mock", "15"))
        );
        engine.evaluate(policy, "MultipleInsureds-default");

        policy.setMultipleInsureds(
                null,
                null,
                null,
                null
        );
        engine.evaluate(policy, "MultipleInsureds-default");

        policy.setMultipleInsureds(
                null,
                si,
                null,
                List.of(new SecondaryInsured("mock", "22"), new SecondaryInsured("mock", "23"))
        );
        engine.evaluate(policy, "MultipleInsureds-default");
    }

}
