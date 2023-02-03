package kraken.engine.sanity.check;

import kraken.runtime.engine.EntryPointResult;
import kraken.testproduct.domain.AnubisCoverage;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.TransactionDetails;
import kraken.testproduct.domain.Vehicle;
import kraken.utils.MockAutoPolicyBuilder;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static kraken.testing.matchers.KrakenMatchers.hasNoIgnoredRules;
import static kraken.testing.matchers.KrakenMatchers.hasRuleResults;
import static kraken.testing.matchers.KrakenMatchers.hasValidationFailures;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author mulevicius
 */
public class EngineSanityForIteratorsTest extends SanityEngineBaseTest {

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void shouldEvaluateRulesWithForEachAnubisCoverage() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setAnubisCoverages(List.of(
                new AnubisCoverage("code1", new BigDecimal(10)),
                new AnubisCoverage("code2", new BigDecimal(10))
        ));

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setAnubisCoverages(List.of(
                new AnubisCoverage("code3", new BigDecimal(10))
        ));

        Policy policy = new MockAutoPolicyBuilder()
                .addRiskItems(List.of(vehicle1, vehicle2))
                .addTxDetails(new TransactionDetails())
                .build();

        final EntryPointResult result = engine.evaluate(policy, "ForEach_EntryPoint");

        assertThat(result, hasRuleResults(3));
        assertThat(policy.getTransactionDetails().getChangePremium(), equalTo(new BigDecimal("300")));
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasValidationFailures(0));
    }

    @Test
    public void shouldEvaluateRulesWithForEachAnubisCoverageAndFail() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setAnubisCoverages(List.of(
                new AnubisCoverage("code1", new BigDecimal(10)),
                new AnubisCoverage("code2", new BigDecimal(20))
        ));

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setAnubisCoverages(List.of(
                new AnubisCoverage("code3", new BigDecimal(10))
        ));

        Policy policy = new MockAutoPolicyBuilder()
                .addRiskItems(List.of(vehicle1, vehicle2))
                .addTxDetails(new TransactionDetails())
                .build();

        final EntryPointResult result = engine.evaluate(policy, "ForEach_EntryPoint");

        assertThat(result, hasRuleResults(3));
        assertThat(policy.getTransactionDetails().getChangePremium(), equalTo(new BigDecimal("400")));
        assertThat(result, hasValidationFailures(2));
    }

    @Test
    public void shouldEvaluateRulesWithForSomeAndForEveryExpressions() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setAnubisCoverages(List.of(
                new AnubisCoverage("code1", new BigDecimal(10), "CULT"),
                new AnubisCoverage("code2", new BigDecimal(20), "CULT")
        ));

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setAnubisCoverages(List.of(
                new AnubisCoverage("code3", new BigDecimal(30), "CULT")
        ));

        Policy policy = new MockAutoPolicyBuilder()
                .addRiskItems(List.of(vehicle1, vehicle2))
                .build();

        final EntryPointResult result = engine.evaluate(policy, "ForSome_ForEvery_EntryPoint");

        assertThat(result, hasRuleResults(1));
        assertThat(result, hasValidationFailures(0));
        assertThat(result, hasNoIgnoredRules());
    }

    @Test
    public void shouldEvaluateRulesWithForSomeAndForEveryExpressionsAndFail() {
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setAnubisCoverages(List.of(
                new AnubisCoverage("code1", new BigDecimal(20), "CULT"),
                new AnubisCoverage("code2", new BigDecimal(20), "CULT")
        ));

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setAnubisCoverages(List.of(
                new AnubisCoverage("code3", new BigDecimal(20), "CULT")
        ));

        Policy policy = new MockAutoPolicyBuilder()
                .addRiskItems(List.of(vehicle1, vehicle2))
                .build();

        final EntryPointResult result = engine.evaluate(policy, "ForSome_ForEvery_EntryPoint");

        assertThat(result, hasRuleResults(1));
        assertThat(result, hasValidationFailures(1));
    }
}
