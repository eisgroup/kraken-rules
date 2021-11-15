package kraken.engine.sanity.check;

import kraken.runtime.engine.EntryPointResult;
import kraken.testproduct.domain.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static kraken.testing.matchers.KrakenMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
        assertThat(result, hasApplicableResults(15));
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


}
