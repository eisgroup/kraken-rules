package kraken.engine.sanity.check;

import java.math.BigDecimal;
import java.util.Map;

import kraken.runtime.EvaluationConfig;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.PolicyDetail;
import kraken.testproduct.domain.TransactionDetails;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author mulevicius
 */
public class EngineSanityMathematicalOperationsTest extends SanityEngineBaseTest {

    @BeforeClass
    public static void beforeAll() {
        start();
    }

    @Test
    public void shouldEvaluateMathematicalOperations() {
        Policy policy = new Policy();
        policy.setTransactionDetails(new TransactionDetails());

        EvaluationConfig evaluationConfig = new EvaluationConfig(Map.of(), "USD");
        engine.evaluate(policy, "Math", evaluationConfig);

        assertThat(policy.getTransactionDetails().getTotalLimit(), comparesEqualTo(number("10.0")));
        assertThat(policy.getTransactionDetails().getChangePremium(), comparesEqualTo(number("8.0")));
        assertThat(policy.getTransactionDetails().getTotalPremium(), comparesEqualTo(number("-0.8413")));
    }

    @Test
    public void shouldEvaluateMathematicalOperations_DevTesting() {
        Policy policy = new Policy();
        policy.setTransactionDetails(new TransactionDetails());
        policy.setPolicyDetail(new PolicyDetail());

        EvaluationConfig evaluationConfig = new EvaluationConfig(Map.of(), "USD");
        engine.evaluate(policy, "Math_DevTesting", evaluationConfig);

        assertThat(policy.getPolicyNumber(), is("7"));
        assertThat(policy.getTransactionDetails().getTxType(), is("8"));
        assertThat(policy.getTransactionDetails().getTxReason(), is("0.117"));
        assertThat(policy.getPolicyDetail().getOosProcessingStage(), is("6.923076923076923"));
        assertThat(policy.getTransactionDetails().getChangePremium(), comparesEqualTo(number("0")));
        assertThat(policy.getTransactionDetails().getTotalPremium(), comparesEqualTo(number("9025")));
        assertThat(policy.getPolicyDetail().getVersionDescription(), is(
                "1000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
                        "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
                        "00000000000000000000000000000000000000"
        ));
    }

    private BigDecimal number(String number) {
        return new BigDecimal(number);
    }
}
