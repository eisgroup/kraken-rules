package kraken.engine.sanity.check;

import kraken.runtime.engine.EntryPointResult;
import kraken.testproduct.domain.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;

import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static kraken.testing.matchers.KrakenMatchers.*;
import static kraken.test.KrakenItestMatchers.matchesSnapshot;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EngineSanityRuleOrderComplexTypeFunctionsCheck extends SanityEngineBaseTest {

    @BeforeClass
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }

    @Test
    public void shouldValidateEvaluateRulesInOrder() {
        final Policy policy = new Policy();
        CarCoverage carCoverage = new CarCoverage();
        policy.setCoverage(carCoverage);

        TransactionDetails transactionDetails = new TransactionDetails();
        policy.setTransactionDetails(transactionDetails);

        final EntryPointResult result = engine.evaluate(policy, "FunctionCheck-RulesUsingFunctionOrderCheck");

        assertThat(carCoverage.getLimitAmount(), is(new BigDecimal("1000")));
        assertThat(transactionDetails.getTotalLimit(), is(new BigDecimal("1000")));

        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasApplicableResults(2));
        assertThat(result, hasNoValidationFailures());
        assertThat(result, matchesSnapshot());
    }

}
