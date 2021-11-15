package kraken.engine.namespace;

import kraken.engine.EngineBaseTest;
import kraken.engine.namespace.domain.Id;
import kraken.engine.namespace.domain.entity.TestAddressInfo;
import kraken.engine.namespace.domain.entity.TestCoverage;
import kraken.engine.namespace.domain.entity.TestPolicy;
import kraken.engine.namespace.domain.entity.TestRiskItem;
import kraken.runtime.engine.context.info.DataObjectInfoResolver;
import kraken.runtime.engine.context.info.SimpleDataObjectInfoResolver;
import kraken.runtime.engine.context.type.ContextTypeAdapter;
import kraken.runtime.engine.context.type.IterableContextTypeAdapter;
import kraken.test.TestResources;

import javax.money.Monetary;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * @author psurinin
 */
public abstract class NamespaceBaseTest extends EngineBaseTest {

    private static final TestResources RESOURCES = TestResources.create(TestResources.Info.NAMESPACE);

    @Override
    protected TestResources getResources() {
        return RESOURCES;
    }

    @Override
    protected DataObjectInfoResolver getResolver() {
        return new SPI();
    }

    @Override
    protected Object getDataObject() {
        final TestAddressInfo addressInfo = new TestAddressInfo(
                "work",
                "Vilnius",
                "Ulonu g. 2",
                "CCC111",
                false,
                "LT"
        );
        final TestCoverage testCoverage = new TestCoverage(
                Monetary.getDefaultAmountFactory()
                        .setCurrency(Monetary.getCurrency("USD"))
                        .setNumber(10)
                        .create(),
                Monetary.getDefaultAmountFactory()
                        .setCurrency(Monetary.getCurrency("USD"))
                        .setNumber(10)
                        .create()
        );
        return new TestPolicy(
                "CA",
                "AZ",
                "pizza",
                LocalDate.MIN,
                List.of(
                        new TestRiskItem(
                                "BMW",
                                false,
                                new BigDecimal(100),
                                addressInfo,
                                List.of(testCoverage)
                        )
                )
        );
    }

    @Override
    protected List<IterableContextTypeAdapter> getIterableTypeAdapters() {
        return Collections.emptyList();
    }

    @Override
    protected List<ContextTypeAdapter> getInstanceTypeAdapters() {
        return Collections.emptyList();
    }

    private static class SPI extends SimpleDataObjectInfoResolver {
        @Override
        public String resolveContextIdForObject(Object data) {
            return ((Id) data).getId();
        }
    }
}
