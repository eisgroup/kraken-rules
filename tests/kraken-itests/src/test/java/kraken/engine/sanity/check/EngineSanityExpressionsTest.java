package kraken.engine.sanity.check;

import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.result.ValidationPayloadResult;
import kraken.testproduct.domain.AddressInfo;
import kraken.testproduct.domain.AnubisCoverage;
import kraken.testproduct.domain.AnubisSecretCoverage;
import kraken.testproduct.domain.COLLCoverage;
import kraken.testproduct.domain.Cult;
import kraken.testproduct.domain.DriverInfo;
import kraken.testproduct.domain.Insured;
import kraken.testproduct.domain.Party;
import kraken.testproduct.domain.PartyRole;
import kraken.testproduct.domain.PersonInfo;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.RRCoverage;
import kraken.testproduct.domain.TermDetails;
import kraken.testproduct.domain.TransactionDetails;
import kraken.testproduct.domain.Vehicle;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static kraken.testing.matchers.KrakenMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static kraken.test.KrakenItestMatchers.matchesSnapshot;

public class EngineSanityExpressionsTest extends SanityEngineBaseTest {

    @BeforeClass
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }

    @Test
    public void shouldExecuteFilterExpressions() {
        final Policy policy = new Policy();
        policy.setPolicyNumber("P01");
        policy.setPolicies(Arrays.asList("P01", "P02"));
        policy.setInsured(new Insured("P01", null));
        policy.setRiskItems(
                Arrays.asList(
                        new Vehicle("P01"),
                        new Vehicle("P02")
                )
        );
        final EntryPointResult result = engine.evaluate(policy, "Expressions-Filter");

        assertThat(isSuccess(result, "policyNumber"), is(true));
        assertThat(isSuccess(result, "policies"), is(true));
        assertThat(isSuccess(result, "policyCurrency"), is(true));
        assertThat(isSuccess(result, "state"), is(true));
        assertThat(isSuccess(result, "createdFromPolicyRev"), is(true));
        assertThat(isSuccess(result, "updatedBy"), is(true));
        assertThat(isSuccess(result, "txType"), is(true));
        assertThat(isSuccess(result, "txReason"), is(true));
        assertThat(isSuccess(result, "txEffectiveDate"), is(true));
        assertThat(isSuccess(result, "txCreateDate"), is(true));

        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldDefaultDatesWithNestedFunctions(){
        final Policy policy = new Policy();
        policy.setTransactionDetails(new TransactionDetails());
        TermDetails termDetails = new TermDetails();
        termDetails.setTermExpirationDate(LocalDate.parse("2019-06-06"));
        policy.setTermDetails(termDetails);
        final EntryPointResult result = engine.evaluate(policy, "Expressions_Date_Nested_Functions");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(2));
        assertThat(policy.getTransactionDetails().getTxCreateDate(), is(LocalDate.parse("2022-05-05")));
        assertThat(policy.getTermDetails().getTermEffectiveDate(), is(LocalDate.parse("2019-06-06")));
    }

    @Test
    public void shouldEvaluateFlatWithPredicate(){
        final Policy policy = new Policy();
        policy.setRiskItems(
                Arrays.asList(
                        createVehicle("2017-10-10", "2018-10-10"),
                        createVehicle("2015-10-10", "2016-10-10")
                )
        );
        TermDetails termDetails = new TermDetails();
        termDetails.setTermExpirationDate(LocalDate.parse("2019-06-06"));
        policy.setTermDetails(termDetails);
        final EntryPointResult result = engine.evaluate(policy, "Expressions_Flat_with_Predicate");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(1));
        assertThat(result, matchesSnapshot());
        assertThat(policy.getPolicyNumber(), is("1"));
    }

    @Test
    public void shouldDefaultWithValueFromCCRCollectionCount(){
        final Policy policy = new Policy();
        policy.setRiskItems(
                Collections.singletonList(
                        createVehicle(createAnubis("2018-01-05"), createAnubis("2017-01-05"))
                )
        );
        Party party1 = createParty();
        policy.setParties(Collections.singletonList(party1));
        final EntryPointResult result = engine.evaluate(policy, "Expressions_default_with_value_from_CCR_collection_count");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(1));
        assertThat(result, matchesSnapshot());
        assertThat(policy.getParties().get(0).getRelationToPrimaryInsured(), is("2"));
    }

    @Test
    public void shouldFindServiceHistoryAndMatchWithPlusMonthsExpressionOutput(){
        final Policy policy = new Policy();
        policy.setRiskItems(
                Arrays.asList(
                        createVehicle("2018-01-05", "2017-01-05"),
                        createVehicle("2015-01-05", "2014-01-05"),
                        createVehicle("2012-01-05", "2013-01-05")
                )
        );

        Insured insured = new Insured();
        insured.setChildrenAges(List.of(5,9,11)); //25
        policy.setInsured(insured);

        Party party = createParty();
        DriverInfo driverInfo = new DriverInfo();
        driverInfo.setTrainingCompletionDate(LocalDate.parse("2013-05-05")); // + 25 month = 2015-06-05
        party.setDriverInfo(driverInfo);
        policy.setParties(Collections.singletonList(party));

        final EntryPointResult result = engine.evaluate(policy, "Expressions_default_driverType_with_value_from_CCR_count_with_predicate");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(1));
        assertThat(result, matchesSnapshot());
        assertThat(policy.getParties().get(0).getDriverInfo().getDriverType(), is("2"));
    }

    @Test
    public void shouldAssertWithPropositionOperations(){
        final Policy policy = new Policy();

        Party party = createParty();
        DriverInfo driverInfo = new DriverInfo();
        driverInfo.setTrainingCompletionDate(LocalDate.parse("2017-05-05")); //NumberOfDaysBetween(2017-05-05, 2017-10-10) > 365
        driverInfo.setConvicted(true);
        party.setDriverInfo(driverInfo);

        PersonInfo personInfo = new PersonInfo();
        personInfo.setSameHomeAddress(false);
        AddressInfo addressInfo = new AddressInfo();
        addressInfo.setDoNotSolicit(false);
        personInfo.setAddressInfo(addressInfo);
        party.setPersonInfo(personInfo);

        policy.setParties(Collections.singletonList(party));
        final EntryPointResult result = engine.evaluate(policy, "Expressions_assert_with_proposition_operations");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(1));
        assertThat(result, hasValidationFailures(0));
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldDefaultWithFilterCountResult(){
        final Policy policy = new Policy();
        policy.setRiskItems(
                Arrays.asList(
                        createVehicle(createAnubis("Yinepu", "2018-01-05"), createAnubis("Pharaoh", "2017-01-05")),
                        createVehicle(createAnubis("Yinepu", "2014-01-05"), createAnubis("Pharaoh", "2015-01-05")),
                        createVehicle(createAnubis("Yinepu", "2012-01-05"), createAnubis("Pharaoh", "2013-01-05"))
                )
        );
        Party party = createParty();
        party.setDriverInfo(new DriverInfo());
        policy.setParties(Collections.singletonList(party));
        final EntryPointResult result = engine.evaluate(policy, "Expressions_default_with_filter_count_result");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(1));
        assertThat(policy.getPolicyNumber(), is("3"));
    }

    @Test
    public void shouldEvaluateExpressionWithCCRAndPredicate(){
        final Policy policy = new Policy();
        policy.setRiskItems(
                Arrays.asList(
                        createVehicle(createAnubis("2018-01-05"), createAnubis("2017-01-05")),
                        createVehicle(createAnubis("2015-01-05"), createAnubis("2014-01-05")),
                        createVehicle(createAnubis("2012-01-05"), createAnubis("2013-01-05"))
                )
        );
        Party party = createParty();
        DriverInfo driverInfo = new DriverInfo();
        driverInfo.setTrainingCompletionDate(LocalDate.parse("2017-05-05"));
        party.setDriverInfo(driverInfo);
        policy.setParties(Collections.singletonList(party));
        final EntryPointResult result = engine.evaluate(policy, "Expressions_default_to_with_value_from_CCR_with_predicate");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(1));
        assertThat(policy.getParties().get(0).getDriverInfo().getDriverType(), is("4"));
    }

    @Test
    public void shouldDefaultWithFlatAndFilterResultCount(){
        final Policy policy = new Policy();
        policy.setRiskItems(
                Arrays.asList(
                        createVehicle(createAnubis("Yinepu", "2018-01-05"), createAnubis("Pharaoh", "2017-01-05")),
                        createVehicle(createAnubis("Yinepu", "2014-01-05"), createAnubis("Pharaoh", "2015-01-05")),
                        createVehicle(createAnubis("Yinepu", "2012-01-05"), createAnubis("Pharaoh", "2013-01-05"))
                )
        );
        Party party = createParty();
        party.setDriverInfo(new DriverInfo());
        policy.setParties(Collections.singletonList(party));
        final EntryPointResult result = engine.evaluate(policy, "Expressions_default_with_flat_and_filter_result_count");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(1));
        assertThat(policy.getPolicyNumber(), is("3"));
    }

    @Test
    public void shouldCompareSumAndFailIfLower(){
        final Policy policy = new Policy();
        policy.setRiskItems(
                List.of(
                        createVehicle(createAnubis(new BigDecimal("50")), createAnubis(new BigDecimal("10"))),
                        createVehicle(createAnubis(new BigDecimal("50")), createAnubis(new BigDecimal("10")))
                )
        );
        Party party = createParty();
        party.setDriverInfo(new DriverInfo());
        policy.setParties(Collections.singletonList(party));
        final EntryPointResult result = engine.evaluate(policy, "Expressions_compare_sum");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(1));
        assertThat(result, hasValidationFailures(1));
    }

    @Test
    public void shouldCompareSumAndNotFailIfNotLower(){
        final Policy policy = new Policy();
        policy.setRiskItems(
                List.of(
                        createVehicle(createAnubis(new BigDecimal("50")), createAnubis(new BigDecimal("100"))),
                        createVehicle(createAnubis(new BigDecimal("50")), createAnubis(new BigDecimal("100")))
                )
        );
        Party party = createParty();
        party.setDriverInfo(new DriverInfo());
        policy.setParties(Collections.singletonList(party));
        final EntryPointResult result = engine.evaluate(policy, "Expressions_compare_sum");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(1));
        assertThat(result, hasValidationFailures(0));
    }

    @Test
    public void shouldDefaultStateWithIfExpressionResult(){
        final Policy policy = new Policy();
        policy.setPolicies(Arrays.asList("One", "Two", "Three"));
        final EntryPointResult result = engine.evaluate(policy, "Expressions_default_to_policy_state_with_if");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(1));
        assertThat(result, hasValidationFailures(0));
        assertThat(policy.getState(), is("CA"));
    }

    @Test
    public void shouldAddSecretLimitAmountIfCoverageIsInstanceOfAnubisSecretCoverage_AssertPasses(){
        final Policy policy = new Policy();
        policy.setRiskItems(
                List.of(
                        createVehicle(
                                new AnubisCoverage(),
                                new AnubisSecretCoverage(new BigDecimal("30"))
                        ),
                        createVehicle(
                                new AnubisCoverage(),
                                new AnubisSecretCoverage(new BigDecimal("300"))
                        )
                )
        );
        final EntryPointResult result = engine.evaluate(policy, "Expressions_instanceof");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(1));
        assertThat(result, hasValidationFailures(0));
    }

    @Test
    public void shouldAddSecretLimitAmountIfCoverageIsInstanceOfAnubisSecretCoverage_AssertFails(){
        final Policy policy = new Policy();
        policy.setRiskItems(
                List.of(
                        createVehicle(
                                new AnubisCoverage(),
                                new AnubisSecretCoverage(new BigDecimal("3"))
                        ),
                        createVehicle(
                                new AnubisCoverage(),
                                new AnubisSecretCoverage(new BigDecimal("300"))
                        )
                )
        );
        final EntryPointResult result = engine.evaluate(policy, "Expressions_instanceof");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(1));
        assertThat(result, hasValidationFailures(1));
    }

    @Test
    public void shouldEvaluateRuleOnlyOnCorrectTypeWhenCheckedWithTypeOfInCondition(){
        final Policy policy = new Policy();
        Vehicle vehicle = new Vehicle();
        vehicle.setCollCoverages(List.of(new COLLCoverage(new BigDecimal("10"))));
        vehicle.setRentalCoverage(new RRCoverage(new BigDecimal("10")));
        policy.setRiskItems(List.of(vehicle));
        final EntryPointResult result = engine.evaluate(policy, "Expressions_typeof");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(2));
        assertThat(result, hasValidationFailures(1));
    }

    @Test
    public void shouldEvaluateDeeplyNestedForLoopExpression(){
        final Policy policy = new Policy();
        policy.setTransactionDetails(new TransactionDetails());

        final EntryPointResult result = engine.evaluate(policy, "Expressions_nested_for");

        assertThat(result, hasNoIgnoredRules());
        assertThat(policy.getTransactionDetails().getTotalPremium(), equalTo(new BigDecimal("123")));
    }

    @Test
    public void shouldEvaluateDeeplyNestedFilterExpression(){
        final Policy policy = new Policy();
        policy.setTermDetails(new TermDetails());
        Vehicle vehicle = new Vehicle();
        vehicle.setOdometerReading(100000L);
        policy.setRiskItems(List.of(vehicle));

        final EntryPointResult result = engine.evaluate(policy, "Expressions_nested_filter");

        assertThat(result, hasNoIgnoredRules());
        assertThat(policy.getTermDetails().getTermNo(), equalTo(100000));
    }

    @Test
    public void shouldEvaluateDeeplyNestedMixedExpression(){
        final Policy policy = new Policy();
        policy.setTransactionDetails(new TransactionDetails());
        Vehicle vehicle = new Vehicle();
        vehicle.setOdometerReading(100000L);
        policy.setRiskItems(List.of(vehicle));

        final EntryPointResult result = engine.evaluate(policy, "Expressions_nested_mixed");

        assertThat(result, hasNoIgnoredRules());
        assertThat(policy.getTransactionDetails().getChangePremium(), equalTo(new BigDecimal("100000")));
    }

    private Vehicle createVehicle(String... serviceDates){
        Vehicle vehicle = new Vehicle();
        vehicle.setServiceHistory(Stream.of(serviceDates).map(LocalDate::parse).collect(Collectors.toList()));
        return vehicle;
    }

    private Vehicle createVehicle(AnubisCoverage... anubisCoverages){
        Vehicle vehicle = new Vehicle();
        vehicle.setAnubisCoverages(Arrays.asList(anubisCoverages));
        return vehicle;
    }

    private AnubisCoverage createAnubis(String cultDate){
        return createAnubis("cult", cultDate);
    }

    private AnubisCoverage createAnubis(BigDecimal limit){
        AnubisCoverage anubisCoverage = new AnubisCoverage();
        anubisCoverage.setLimitAmount(limit);
        return anubisCoverage;
    }

    private AnubisCoverage createAnubis(String name, String cultDate){
        AnubisCoverage anubisCoverage = new AnubisCoverage();
        Cult cult = new Cult(name, LocalDate.parse(cultDate));
        anubisCoverage.setCult(cult);
        return anubisCoverage;
    }

    private Party createParty(PartyRole... partyRoles){
        Party party = new Party();
        party.setRoles(Arrays.asList(partyRoles));
        return party;
    }

    private Boolean isSuccess(EntryPointResult evaluate, String field) {
        return ((ValidationPayloadResult) evaluate.getFieldResults().values().stream()
                .filter(fer -> fer.getContextFieldInfo().getFieldName().contains(field))
                .findFirst()
                .get()
                .getRuleResults()
                .get(0)
                .getPayloadResult())
                .getSuccess();
    }

}
