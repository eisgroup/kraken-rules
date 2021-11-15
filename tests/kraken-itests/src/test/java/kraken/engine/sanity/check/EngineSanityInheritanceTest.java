package kraken.engine.sanity.check;

import kraken.model.context.ContextDefinition;
import kraken.runtime.engine.EntryPointResult;
import kraken.testproduct.domain.AnubisCoverage;
import kraken.testproduct.domain.COLLCoverage;
import kraken.testproduct.domain.FullCoverage;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.RRCoverage;
import kraken.testproduct.domain.Vehicle;
import kraken.utils.MockAutoPolicyBuilder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static kraken.testing.matchers.KrakenMatchers.hasApplicableResults;
import static kraken.testing.matchers.KrakenMatchers.hasDisabledFields;
import static kraken.testing.matchers.KrakenMatchers.ignoredRuleCountIs;
import static kraken.testing.matchers.KrakenMatchers.hasHiddenFields;
import static kraken.testing.matchers.KrakenMatchers.hasNoIgnoredRules;
import static kraken.testing.matchers.KrakenMatchers.hasValidationFailures;
import static kraken.test.KrakenItestMatchers.matchesSnapshot;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

/**
 * @author psurinin
 */
public class EngineSanityInheritanceTest extends SanityEngineBaseTest {

    @BeforeClass
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }

    private Policy validPolicy;
    private Policy emptyPolicy;

    @Before
    public void setUp() {
        super.setUp();
        validPolicy = new MockAutoPolicyBuilder().addValidRiskItems(1).build();
        Vehicle vehicle = new Vehicle();
        List<Vehicle> vehicles = Collections.singletonList(vehicle);
        vehicle.setRentalCoverage(new RRCoverage());
        vehicle.setCollCoverages(Collections.singletonList(new COLLCoverage()));
        vehicle.setFullCoverages(Collections.singletonList(new FullCoverage()));
        vehicle.setAnubisCoverages(Collections.singletonList(new AnubisCoverage()));
        emptyPolicy = new MockAutoPolicyBuilder().addEmptyAutoPolicy().addRiskItems(vehicles).build();
    }

    @Test
    public void shouldHaveTwoParentDefinitions() {
        final ContextDefinition ctx = getResources().getKrakenProject().getContextDefinitions().get("FullCoverage");

        assertThat(ctx.getParentDefinitions(), hasSize(2));
    }

    @Test
    public void shouldHaveOneParentDefinition(){
        final ContextDefinition ctx = getResources().getKrakenProject().getContextDefinitions().get("PersonInfo");

        assertThat(ctx.getParentDefinitions(), hasSize(1));
    }

    @Test
    public void shouldEvaluateRulesOnLocalAndInheritedFields(){
        final Policy policy = new MockAutoPolicyBuilder().addValidAutoPolicy().build();
        String entryPointName = "AddressInfoLocalAndInheritedFieldRules";
        EntryPointResult result = engine.evaluate(policy, entryPointName);

        assertThat(result, hasApplicableResults(7));
        assertThat(result, hasValidationFailures(0));
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldValidateInheritedMandatoryFields(){
        Vehicle vehicle = new Vehicle();
        vehicle.setFullCoverages(Collections.singletonList(new FullCoverage()));

        final Policy policy = new MockAutoPolicyBuilder().addRiskItems(List.of(vehicle))
                .build();
        final EntryPointResult result = engine.evaluate(policy, "ValidateInheritedMandatoryFields");

        assertThat(result, hasApplicableResults(5));
        assertThat(result, hasValidationFailures(5));
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldSetDefaultValueOnInheritedField(){
        Vehicle vehicle = new Vehicle();
        vehicle.setFullCoverages(Collections.singletonList(new FullCoverage()));

        final Policy policy = new MockAutoPolicyBuilder().addRiskItems(List.of(vehicle))
                .build();
        final EntryPointResult result = engine.evaluate(policy, "ValidateInheritedDefaultField");

        assertThat(vehicle.getFullCoverages().get(0).getLimitAmount(), equalTo(new BigDecimal("1000")));
        assertThat(vehicle.getFullCoverages().get(0).getDeductibleAmount(), equalTo(new BigDecimal("1000")));
        assertThat(result, hasApplicableResults(2));
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldValidateInheritedPresentationFields(){
        Vehicle vehicle = new Vehicle();
        vehicle.setCollCoverages(Collections.singletonList(new COLLCoverage()));

        final Policy policy = new MockAutoPolicyBuilder().addRiskItems(List.of(vehicle))
                .build();
        final EntryPointResult result = engine.evaluate(policy, "ValidateInheritedPresentationFields");

        assertThat(result, hasApplicableResults(2));
        assertThat(result, hasHiddenFields(1));
        assertThat(result, hasDisabledFields(1));
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldCheckAccessibilityCarCoverage() {
        final EntryPointResult result = engine.evaluate(validPolicy, "AccessibilityCarCoverage");

        assertThat(result, hasApplicableResults(8));
        assertThat(result, hasDisabledFields(8));
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldCheckVisibilityCarCoverage() {
        final EntryPointResult result = engine.evaluate(validPolicy, "VisibilityCarCoverage");

        assertThat(result, hasApplicableResults(7));
        assertThat(result, hasHiddenFields(7));
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldCheckUsagePayloadCarCoverageOnEmptyPolicy() {
        final EntryPointResult result = engine.evaluate(emptyPolicy, "UsagePayloadCarCoverage");

        assertThat(result, hasApplicableResults(13));
        assertThat(result, hasValidationFailures(13));
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldCheckUsagePayloadCarCoverageOnValidPolicy() {
        final EntryPointResult result = engine.evaluate(validPolicy, "UsagePayloadCarCoverage");

        assertThat(result, hasApplicableResults(13));
        assertThat(result, hasValidationFailures(0));
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldCheckInitCarCoverageOnEmptyPolicy() {
        final EntryPointResult result = engine.evaluate(emptyPolicy, "InitCarCoverage");

        assertThat(result, hasApplicableResults(7));
        assertThat(result, hasNoIgnoredRules());
    }

    @Test
    public void shouldCheckInitCarCoverageOnValidPolicy() {
        final EntryPointResult result = engine.evaluate(validPolicy, "InitCarCoverage");

        assertThat(result, hasApplicableResults(8));
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldCheckAssertionCarCoverageOnEmptyData() {
        final EntryPointResult result = engine.evaluate(emptyPolicy, "AssertionCarCoverage");

        assertThat(result, hasApplicableResults(5));
        assertThat(result, hasValidationFailures(0));
        assertThat(result, ignoredRuleCountIs(5));
    }

    @Test
    public void shouldCheckAssertionCarCoverageOnValidData() {
        final EntryPointResult result = engine.evaluate(validPolicy, "AssertionCarCoverage");

        assertThat(result, hasApplicableResults(5));
        assertThat(result, hasValidationFailures(0));
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }
}
