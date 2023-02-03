package kraken.runtime.engine.handlers;

import kraken.TestRuleBuilder;
import kraken.model.validation.ValidationSeverity;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.engine.result.SizeRangePayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.Payload;
import kraken.runtime.model.rule.payload.validation.SizeRangePayload;
import org.junit.Test;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static kraken.runtime.engine.handlers.PayloadHandlerTestConstants.SESSION;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author psurinin
 */
public class SizeRangePayloadHandlerTest {

    @Test
    public void shouldFailOnLessThanRanges() {
        final SizeRangePayload payload = payload(2, 3);
        final DataContext dataContext = dataContext(1);
        final PayloadResult payloadResult = handler().executePayload(payload, rule(payload), dataContext, SESSION);
        assertSuccess(payloadResult, false);
    }
    @Test
    public void shouldFailOnLesThanRangesWithCollectionNull() {
        final SizeRangePayload payload = payload(2, 3);
        final PayloadResult payloadResult = handler().executePayload(payload, rule(payload), dataContext(null), SESSION);
        assertSuccess(payloadResult, false);
    }
    @Test
    public void shouldSuccessOnMinRanges() {
        final SizeRangePayload payload = payload(2, 3);
        final DataContext dataContext = dataContext(2);
        final PayloadResult payloadResult = handler().executePayload(payload, rule(payload), dataContext, SESSION);
        assertSuccess(payloadResult, true);
    }
    @Test
    public void shouldSuccessOnMaxRanges() {
        final SizeRangePayload payload = payload(2, 3);
        final DataContext dataContext = dataContext(3);
        final PayloadResult payloadResult = handler().executePayload(payload, rule(payload), dataContext, SESSION);
        assertSuccess(payloadResult, true);
    }

    @Test
    public void shouldFailOnMoreThanRanges() {
        final SizeRangePayload payload = payload(2, 3);
        final DataContext dataContext = dataContext(4);
        final PayloadResult payloadResult = handler().executePayload(payload, rule(payload), dataContext, SESSION);
        assertSuccess(payloadResult, false);
    }

    @Test
    public void shouldSuccessWhenCollectionNullAndInRange() {
        final SizeRangePayload payload = payload(0, 3);
        final PayloadResult payloadResult = handler().executePayload(payload, rule(payload), dataContext(null), SESSION);
        assertSuccess(payloadResult, true);
    }

    private void assertSuccess(PayloadResult result, boolean toMatch){
        assertThat(result, instanceOf(SizeRangePayloadResult.class));
        assertThat(((SizeRangePayloadResult) result).getSuccess(), is(toMatch));
    }

    private SizeRangePayloadHandler  handler() {
        return new SizeRangePayloadHandler(new KrakenExpressionEvaluator());
    }

    private DataContext dataContext(int size) {
        final Collection<Object> collect = IntStream.range(0, size).boxed().collect(Collectors.toList());
        return dataContext(collect);
    }

    private DataContext dataContext(Collection<Object> collection) {
        final Coverage coverage = new Coverage(collection);
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);
        return dataContext;
    }

    private RuntimeRule rule(Payload payload) {
        return TestRuleBuilder.getInstance()
                .targetPath("conditions")
                .payload(payload)
                .build();
    }

    private SizeRangePayload payload(int min, int max) {
        return new SizeRangePayload(
                null,
                ValidationSeverity.critical,
                false,
                null,
                min,
                max
        );
    }


}
