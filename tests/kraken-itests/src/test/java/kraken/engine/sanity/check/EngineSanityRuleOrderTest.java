package kraken.engine.sanity.check;

import kraken.runtime.KrakenRuntimeException;
import kraken.runtime.engine.EntryPointResult;
import kraken.testproduct.domain.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static kraken.testing.matchers.KrakenMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static kraken.test.KrakenItestMatchers.matchesSnapshot;

/**
 * @author psurinin@eisgroup.com
 */
public class EngineSanityRuleOrderTest extends SanityEngineBaseTest {

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
        Vehicle vehicle = new Vehicle();
        COLLCoverage coverage = new COLLCoverage();
        vehicle.setCollCoverages(List.of(coverage));
        policy.setRiskItems(List.of(vehicle));
        Insured insured = new Insured();
        insured.setHaveChildren(true);
        policy.setInsured(insured);
        BillingInfo billingInfo = new BillingInfo();
        CreditCardInfo creditCardInfo = new CreditCardInfo();
        creditCardInfo.setBillingAddress(new BillingAddress());
        billingInfo.setCreditCardInfo(creditCardInfo);
        policy.setBillingInfo(billingInfo);
        policy.setParties(List.of(new Party()));
        final EntryPointResult result = engine.evaluate(policy, "RuleOrder");

        assertThat(coverage.getCode(), is("PartyPartyAddress"));

        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasApplicableResults(7));
        assertThat(result, hasNoValidationFailures());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldValidateRulesWithComplexFieldExpressionsInOrder() {
        final Policy policy = new Policy();
        BillingInfo billingInfo = new BillingInfo();
        billingInfo.setCreditCardInfo(new CreditCardInfo());
        policy.setBillingInfo(billingInfo);

        //RRCoverage
        Vehicle vehicle = new Vehicle();
        RRCoverage rrCoverage = new RRCoverage();
        vehicle.setRentalCoverage(rrCoverage);
        policy.setRiskItems(List.of(vehicle));

        //Insured
        Insured insured = new Insured();
        BillingAddress billingAddress = new BillingAddress();
        insured.setAddressInfo(billingAddress);
        policy.setInsured(insured);

        //Party
        Party party = new Party();
        PartyRole partyRole = new PartyRole();
        party.setRoles(List.of(partyRole));
        policy.setParties(List.of(party));

        final EntryPointResult result = engine.evaluate(policy, "RuleOrderWithComplexField");

        assertThat(policy.getPolicyNumber(), is("doNotSolicit is true"));
        assertThat(insured.getAddressInfo().getDoNotSolicit(), is(true));
        assertThat(rrCoverage.getLimitAmount(), is(new BigDecimal(10)));
        assertThat(billingAddress.getCity(), is("San Diego"));
        assertThat(partyRole.getLimit(), is(new BigDecimal(100)));


        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasApplicableResults(6));
        assertThat(result, hasNoValidationFailures());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldValidateEvaluateDefaultRulesInOrder() {
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

    @Test
    public void shouldEvaluateRuleOrderWithNoCycleInFunction() {
        Vehicle vehicle = new Vehicle();
        RRCoverage rrCoverage = new RRCoverage();
        vehicle.setRentalCoverage(rrCoverage);
        COLLCoverage collCoverage1 = new COLLCoverage(new BigDecimal("50"));
        COLLCoverage collCoverage2 = new COLLCoverage(new BigDecimal("75"));
        vehicle.setCollCoverages(List.of(collCoverage1, collCoverage2));
        Policy policy = new Policy();
        policy.setRiskItems(List.of(vehicle));

        EntryPointResult result = engine.evaluate(policy, "RuleOrderWithNoCycleInFunction");

        assertThat(policy.getPolicyNumber(), is("Large"));
        assertThat(rrCoverage.getLimitAmount(), is(new BigDecimal("125")));

        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasApplicableResults(2));
        assertThat(result, hasNoValidationFailures());
    }

}
