package kraken.engine.sanity.check;

import static kraken.testing.matchers.KrakenMatchers.hasRuleResults;
import static kraken.testing.matchers.KrakenRuleMatchers.isApplied;
import static kraken.testing.matchers.KrakenRuleMatchers.isIgnored;
import static kraken.testing.matchers.KrakenRuleMatchers.isSkipped;
import static kraken.testing.matchers.KrakenRuleMatchers.isUnused;
import static kraken.testing.matchers.KrakenRuleMatchers.ruleResult;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import kraken.runtime.KrakenRuntimeException;
import kraken.runtime.engine.EntryPointResult;
import kraken.testproduct.domain.BillingInfo;
import kraken.testproduct.domain.Policy;

/**
 * @author mulevicius
 */
public class EngineSanityDefaultRulePriorityTest extends SanityEngineBaseTest {

    @Test
    public void shouldEvaluateDefaultRulesByPriority_ApplyWithPriorityMIN() {
        Policy policy = new Policy();
        policy.setBillingInfo(new BillingInfo());

        EntryPointResult result = engine.evaluate(policy, "DefaultRuleByPriority");

        assertThat(policy.getPolicyNumber(), is("MIN"));
        assertThat(result, hasRuleResults(7));

        assertThat(ruleResult(result, "DefaultPolicyNumber-PriorityMAX-defaultExpressionError"), isIgnored());
        assertThat(ruleResult(result, "DefaultPolicyNumber-Priority999-conditionExpressionError"), isIgnored());
        assertThat(ruleResult(result, "DefaultPolicyNumber-Priority10"), isSkipped());
        assertThat(ruleResult(result, "DefaultPolicyNumber-Priority0"), isSkipped());
        assertThat(ruleResult(result, "DefaultPolicyNumber-Priority-10"), isSkipped());
        assertThat(ruleResult(result, "DefaultPolicyNumber-PriorityMIN"), isApplied());
        assertThat(ruleResult(result, "DefaultPolicyNumber-PriorityMIN2"), isSkipped());
    }

    @Test
    public void shouldEvaluateDefaultRulesByPriority_ApplyWithPriority10() {

        Policy policy = new Policy();
        policy.setBillingInfo(new BillingInfo());
        policy.getBillingInfo().setAccountName("10");

        EntryPointResult result = engine.evaluate(policy, "DefaultRuleByPriority");

        assertThat(policy.getPolicyNumber(), is("10"));
        assertThat(result, hasRuleResults(3));

        assertThat(ruleResult(result, "DefaultPolicyNumber-PriorityMAX-defaultExpressionError"), isIgnored());
        assertThat(ruleResult(result, "DefaultPolicyNumber-Priority999-conditionExpressionError"), isIgnored());
        assertThat(ruleResult(result, "DefaultPolicyNumber-Priority10"), isApplied());
    }

    @Test
    public void shouldEvaluateDefaultRulesByPriority_FailTwoDefaultRulesAppliedWithSamePriority() {
        Policy policy = new Policy();
        policy.setBillingInfo(new BillingInfo());
        policy.getBillingInfo().setAccountName("MIN");

        assertThrows("On field 'Policy:-1:policyNumber' applied '2' default rules: "
            + "DefaultPolicyNumber-PriorityMIN2, DefaultPolicyNumber-PriorityMIN. "
            + "Only one default rule can be applied on the same field.",
            KrakenRuntimeException.class,
            () -> engine.evaluate(policy, "DefaultRuleByPriority")
        );
    }
}
