package kraken.runtime.engine.handlers;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import kraken.TestRuleBuilder;
import kraken.model.validation.SizeOrientation;
import kraken.model.validation.ValidationSeverity;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.engine.result.SizePayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.Payload;
import kraken.runtime.model.rule.payload.validation.SizePayload;
import org.junit.Assert;
import org.junit.Test;

import static kraken.runtime.engine.handlers.PayloadHandlerTestConstants.SESSION;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

/**
 * @author psurinin
 */
public class SizePayloadHandlerTest {

    @Test
    public void shouldCheckForMinEquals() {
        final SizePayload payload = payload(SizeOrientation.MIN, 2);
        final PayloadResult payloadResult = handler().executePayload(payload, rule(payload), dataContext(2), SESSION);
        assertSuccess(payloadResult, true);
    }
    @Test
    public void shouldCheckForMinEqualsWithNullCollection() {
        final SizePayload payload = payload(SizeOrientation.MIN, 2);
        final PayloadResult payloadResult = handler().executePayload(payload, rule(payload), dataContext(null), SESSION);
        assertSuccess(payloadResult, false);
    }
    @Test
    public void shouldCheckForMinHasLess() {
        final SizePayload payload = payload(SizeOrientation.MIN, 2);
        final PayloadResult payloadResult = handler().executePayload(payload, rule(payload), dataContext(1), SESSION);
        assertSuccess(payloadResult, false);
    }
    @Test
    public void shouldCheckForMaxHasMore() {
        final SizePayload payload = payload(SizeOrientation.MAX, 2);
        final PayloadResult payloadResult = handler().executePayload(payload, rule(payload), dataContext(3), SESSION);
        assertSuccess(payloadResult, false);
    }
    @Test
    public void shouldCheckForMaxWhenCollectionNull() {
        final SizePayload payload = payload(SizeOrientation.MAX, 2);
        final PayloadResult payloadResult = handler().executePayload(payload, rule(payload), dataContext(null), SESSION);
        assertSuccess(payloadResult, true);
    }
    @Test
    public void shouldCheckForMaxEquals() {
        final SizePayload payload = payload(SizeOrientation.MAX, 2);
        final PayloadResult payloadResult = handler().executePayload(payload, rule(payload), dataContext(2), SESSION);
        assertSuccess(payloadResult, true);
    }
    @Test
    public void shouldCheckForMaxHasLess() {
        final SizePayload payload = payload(SizeOrientation.MAX, 2);
        final PayloadResult payloadResult = handler().executePayload(payload, rule(payload), dataContext(1), SESSION);
        assertSuccess(payloadResult, true);
    }
    @Test
    public void shouldCheckForMinHasMore() {
        final SizePayload payload = payload(SizeOrientation.MIN, 2);
        final PayloadResult payloadResult = handler().executePayload(payload, rule(payload), dataContext(3), SESSION);
        assertSuccess(payloadResult, true);
    }
    @Test
    public void shouldCheckForEqualsHasMore() {
        final SizePayload payload = payload(SizeOrientation.EQUALS, 2);
        final PayloadResult payloadResult = handler().executePayload(payload, rule(payload), dataContext(3), SESSION);
        assertSuccess(payloadResult, false);
    }
    @Test
    public void shouldCheckForEqualsEquals() {
        final SizePayload payload = payload(SizeOrientation.EQUALS, 2);
        final PayloadResult payloadResult = handler().executePayload(payload, rule(payload), dataContext(2), SESSION);
        assertSuccess(payloadResult, true);
    }
    @Test
    public void shouldCheckForEqualsWhenCollectionNull() {
        final SizePayload payload = payload(SizeOrientation.EQUALS, 1);
        final PayloadResult payloadResult = handler().executePayload(payload, rule(payload), dataContext(null), SESSION);
        assertSuccess(payloadResult, false);
    }
    @Test
    public void shouldCheckForEqualsHasLess() {
        final SizePayload payload = payload(SizeOrientation.EQUALS, 2);
        final PayloadResult payloadResult = handler().executePayload(payload, rule(payload), dataContext(1), SESSION);
        assertSuccess(payloadResult, false);
    }
    private void assertSuccess(PayloadResult result, boolean toMatch){
        Assert.assertThat(result, instanceOf(SizePayloadResult.class));
        Assert.assertThat(((SizePayloadResult) result).getSuccess(), is(toMatch));
    }

    private SizePayloadHandler handler() {
        return new SizePayloadHandler(new KrakenExpressionEvaluator());
    }

    private DataContext dataContext(int size) {
        final Collection<Object> collection = IntStream.range(0, size).boxed().collect(Collectors.toList());
        return dataContext(collection);
    }

    private DataContext dataContext(Collection<Object> collection){
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

    private SizePayload payload(SizeOrientation orientation, int size) {
        return new kraken.runtime.model.rule.payload.validation.SizePayload(
                null,
                ValidationSeverity.critical,
                false,
                null,
                orientation,
                size
        );
    }

}
