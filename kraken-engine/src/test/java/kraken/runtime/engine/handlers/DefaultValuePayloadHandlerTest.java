/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package kraken.runtime.engine.handlers;

import static kraken.model.context.PrimitiveFieldDataType.DATE;
import static kraken.model.context.PrimitiveFieldDataType.DATETIME;
import static kraken.model.context.PrimitiveFieldDataType.DECIMAL;
import static kraken.model.context.PrimitiveFieldDataType.MONEY;
import static kraken.model.context.PrimitiveFieldDataType.STRING;
import static kraken.runtime.engine.handlers.PayloadHandlerTestConstants.SESSION;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.javamoney.moneta.Money;
import org.junit.Before;
import org.junit.Test;

import kraken.TestRuleBuilder;
import kraken.model.context.Cardinality;
import kraken.model.derive.DefaultingType;
import kraken.runtime.KrakenRuntimeException;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.events.ValueChangedEvent;
import kraken.runtime.engine.result.DefaultValuePayloadResult;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.context.ContextField;
import kraken.runtime.model.context.RuntimeContextDefinition;
import kraken.runtime.model.rule.RuntimeRule;

public class DefaultValuePayloadHandlerTest {
    private DefaultValuePayloadHandler defaultValuePayloadHandler;

    @Before
    public void setUp() {
        defaultValuePayloadHandler = new DefaultValuePayloadHandler(new KrakenExpressionEvaluator());
    }

    @Test
    public void executePayloadShouldReturnInstanceOfDefaultValuePayloadResult() {
        Coverage coverage = new Coverage();
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);

        RuntimeRule rule = TestRuleBuilder.getInstance()
                .defaultPayload("'Q001'", DefaultingType.defaultValue)
                .targetPath("code")
                .build();
        PayloadResult payloadResult = defaultValuePayloadHandler.executePayload(
                rule,
                dataContext,
                SESSION
        );

        assertThat(payloadResult, is(instanceOf(DefaultValuePayloadResult.class)));
    }

    @Test
    public void executePayloadShouldReturnEventWithDefaultValueWhenDefaultingToDefaultValue() {
        Coverage coverage = new Coverage();
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);
        RuntimeRule rule = TestRuleBuilder.getInstance()
                .defaultPayload("'Q001'", DefaultingType.defaultValue)
                .targetPath("code")
                .build();

        DefaultValuePayloadResult payloadResult = (DefaultValuePayloadResult) defaultValuePayloadHandler.executePayload(
                rule,
                dataContext,
                SESSION
        );
        assertThat(
                ((ValueChangedEvent) payloadResult.getEvents().get(0)).getNewValue().toString(),
                is(equalTo("Q001"))
        );
    }

    @Test
    public void executePayloadShouldReturnEventWithResetValueWhenResettingToResetValue() {
        Coverage coverage = new Coverage();
        coverage.setCode("Q001");
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);
        RuntimeRule rule = TestRuleBuilder.getInstance()
                .defaultPayload("'Q777'", DefaultingType.resetValue)
                .targetPath("code")
                .build();

        DefaultValuePayloadResult payloadResult = (DefaultValuePayloadResult) defaultValuePayloadHandler.executePayload(
                rule,
                dataContext,
                SESSION
        );

        assertThat(
                ((ValueChangedEvent) payloadResult.getEvents().get(0)).getNewValue(),
                is(equalTo("Q777"))
        );
    }

    @Test
    public void shouldCoerceNumberToMoney() {
        Coverage coverage = new Coverage();
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);
        dataContext.setContextDefinition(coverageDefinition());
        RuntimeRule rule = TestRuleBuilder.getInstance()
            .defaultPayload("10", DefaultingType.resetValue)
            .targetPath("moneyLimit")
            .build();

        defaultValuePayloadHandler.executePayload(rule, dataContext, SESSION);

        assertThat(coverage.getMoneyLimit(), is(equalTo(Money.of(10, "USD"))));
    }

    @Test
    public void shouldCoerceMoneyToNumber() {
        Coverage coverage = new Coverage();
        coverage.setMoneyLimit(Money.of(10, "USD"));
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);
        dataContext.setContextDefinition(coverageDefinition());
        RuntimeRule rule = TestRuleBuilder.getInstance()
            .defaultPayload("moneyLimit", DefaultingType.resetValue)
            .targetPath("decimalLimit")
            .build();

        defaultValuePayloadHandler.executePayload(rule, dataContext, SESSION);

        assertThat(coverage.getDecimalLimit(), is(equalTo(new BigDecimal("10"))));
    }

    @Test
    public void shouldCoerceDateToDatetime() {
        Coverage coverage = new Coverage();
        coverage.setLocalDate(LocalDate.of(2022, 1, 1));
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);
        dataContext.setContextDefinition(coverageDefinition());
        RuntimeRule rule = TestRuleBuilder.getInstance()
            .defaultPayload("localDate", DefaultingType.resetValue)
            .targetPath("localDateTime")
            .build();

        defaultValuePayloadHandler.executePayload(rule, dataContext, SESSION);

        assertThat(coverage.getLocalDateTime(), is(equalTo(LocalDateTime.of(2022, 1, 1, 0, 0, 0))));
    }

    @Test
    public void shouldCoerceDatetimeToDate() {
        Coverage coverage = new Coverage();
        coverage.setLocalDateTime(LocalDateTime.of(2022, 1, 1, 10, 10, 10));
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);
        dataContext.setContextDefinition(coverageDefinition());
        RuntimeRule rule = TestRuleBuilder.getInstance()
            .defaultPayload("localDateTime", DefaultingType.resetValue)
            .targetPath("localDate")
            .build();

        defaultValuePayloadHandler.executePayload(rule, dataContext, SESSION);

        assertThat(coverage.getLocalDate(), is(equalTo(LocalDate.of(2022, 1, 1))));
    }

    @Test
    public void shouldThrowIfIncompatibleType() {
        Coverage coverage = new Coverage();
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);
        dataContext.setContextDefinition(coverageDefinition());
        RuntimeRule rule = TestRuleBuilder.getInstance()
            .defaultPayload("10", DefaultingType.resetValue)
            .targetPath("localDate")
            .build();

        DefaultValuePayloadResult payloadResult = (DefaultValuePayloadResult) defaultValuePayloadHandler.executePayload(
            rule,
            dataContext,
            SESSION
        );

        assertThat(coverage.getLocalDate(), is(nullValue()));
        assertThat(payloadResult.getException().get(), is(instanceOf(KrakenRuntimeException.class)));
    }

    @Test
    public void shouldDefaultToNull() {
        Coverage coverage = new Coverage();
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);
        dataContext.setContextDefinition(coverageDefinition());
        RuntimeRule rule = TestRuleBuilder.getInstance()
            .defaultPayload("null", DefaultingType.resetValue)
            .targetPath("localDate")
            .build();

        DefaultValuePayloadResult payloadResult = (DefaultValuePayloadResult) defaultValuePayloadHandler.executePayload(
            rule,
            dataContext,
            SESSION
        );

        assertThat(coverage.getLocalDate(), is(nullValue()));
        assertThat(payloadResult.getException().isEmpty(), is(true));
    }

    @Test
    public void shouldDefaultNullCollectionOfPrimitives() {
        Coverage coverage = new Coverage();
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);
        dataContext.setContextDefinition(coverageDefinition());
        RuntimeRule rule = TestRuleBuilder.getInstance()
            .defaultPayload("{'A', 'B', 'C'}", DefaultingType.defaultValue)
            .targetPath("labels")
            .build();

        var result = (DefaultValuePayloadResult) defaultValuePayloadHandler.executePayload(rule, dataContext, SESSION);

        assertThat(result.getException().isEmpty(), is(true));
        assertThat(coverage.getLabels(), hasItems("A", "B", "C"));
    }

    @Test
    public void shouldDefaultEmptyCollectionOfPrimitives() {
        Coverage coverage = new Coverage();
        coverage.setLabels(List.of());
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);
        dataContext.setContextDefinition(coverageDefinition());
        RuntimeRule rule = TestRuleBuilder.getInstance()
            .defaultPayload("{'A', 'B', 'C'}", DefaultingType.defaultValue)
            .targetPath("labels")
            .build();

        var result = (DefaultValuePayloadResult) defaultValuePayloadHandler.executePayload(rule, dataContext, SESSION);

        assertThat(result.getException().isEmpty(), is(true));
        assertThat(coverage.getLabels(), hasItems("A", "B", "C"));
    }

    @Test
    public void shouldResetCollectionOfPrimitives() {
        Coverage coverage = new Coverage();
        coverage.setLabels(List.of("A"));
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);
        dataContext.setContextDefinition(coverageDefinition());
        RuntimeRule rule = TestRuleBuilder.getInstance()
            .defaultPayload("{'A', 'B', 'C'}", DefaultingType.resetValue)
            .targetPath("labels")
            .build();

        var result = (DefaultValuePayloadResult) defaultValuePayloadHandler.executePayload(rule, dataContext, SESSION);

        assertThat(result.getException().isEmpty(), is(true));
        assertThat(coverage.getLabels(), hasItems("A", "B", "C"));
    }

    @Test
    public void shouldDefaultCoercedCollectionOfPrimitives() {
        Coverage coverage = new Coverage();
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);
        dataContext.setContextDefinition(coverageDefinition());
        RuntimeRule rule = TestRuleBuilder.getInstance()
            .defaultPayload("{10, 20}", DefaultingType.resetValue)
            .targetPath("moneys")
            .build();

        var result = (DefaultValuePayloadResult) defaultValuePayloadHandler.executePayload(rule, dataContext, SESSION);

        assertThat(result.getException().isEmpty(), is(true));
        assertThat(coverage.getMoneys(), hasItems(Money.of(10, "USD"), Money.of(20, "USD")));
    }

    @Test
    public void shouldThrowIfDefaultingContext() {
        Coverage coverage = new Coverage();
        DataContext dataContext = new DataContext();
        dataContext.setDataObject(coverage);
        dataContext.setContextDefinition(coverageDefinition());
        RuntimeRule rule = TestRuleBuilder.getInstance()
            .defaultPayload("null", DefaultingType.resetValue)
            .targetPath("address")
            .build();

        assertThrows(
            UnsupportedOperationException.class,
            () -> defaultValuePayloadHandler.executePayload(
                rule,
                dataContext,
                SESSION
            )
        );
    }

    private RuntimeContextDefinition coverageDefinition() {
        return new RuntimeContextDefinition(
            "Coverage",
            Map.of(),
            Map.of(
                "decimalLimit",
                field("decimalLimit", DECIMAL.toString(), Cardinality.SINGLE),
                "moneyLimit",
                field("moneyLimit", MONEY.toString(), Cardinality.SINGLE),
                "localDate",
                field("localDate", DATE.toString(), Cardinality.SINGLE),
                "localDateTime",
                field("localDateTime", DATETIME.toString(), Cardinality.SINGLE),
                "labels",
                field("labels", STRING.toString(), Cardinality.MULTIPLE),
                "moneys",
                field("moneys", MONEY.toString(), Cardinality.MULTIPLE),
                "address",
                field("address", "Address", Cardinality.SINGLE)
            ),
            List.of(),
            false
        );
    }

    private ContextField field(String name, String type, Cardinality cardinality) {
        return new ContextField(name, type, name, cardinality, false);
    }
}
