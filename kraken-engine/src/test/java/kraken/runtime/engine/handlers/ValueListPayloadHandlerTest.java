/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.runtime.engine.handlers;

import static kraken.runtime.engine.handlers.PayloadHandlerTestConstants.SESSION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.money.Monetary;

import org.junit.Before;
import org.junit.Test;

import kraken.TestRuleBuilder;
import kraken.model.ValueList;
import kraken.model.context.Cardinality;
import kraken.model.context.PrimitiveFieldDataType;
import kraken.runtime.KrakenRuntimeException;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.result.ValueListPayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.context.ContextField;
import kraken.runtime.model.context.RuntimeContextDefinition;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.test.domain.policy.Policy;

/**
 * @author Tomas Dapkunas
 * @since 1.43.0
 */
public class ValueListPayloadHandlerTest {

    private static final TestRuleBuilder RULE_BUILDER = TestRuleBuilder.getInstance();

    private ValueListPayloadHandler valueListPayloadHandler;

    @Before
    public void setUp() {
        valueListPayloadHandler = new ValueListPayloadHandler(new KrakenExpressionEvaluator());
    }

    @Test
    public void shouldReturnSuccessResultForNullValueField() {
        Policy policy = new Policy("P0000001");
        policy.setPolicyCurrency(null);

        DataContext dataContext = new DataContext();
        dataContext.setDataObject(policy);
        dataContext.setContextDefinition(createPolicyContext());

        RuntimeRule rule = RULE_BUILDER
            .targetPath("policyCurrency")
            .valueListPayload(ValueList.fromString(List.of("USD", "CAD")))
            .build();

        ValueListPayloadResult payloadResult = (ValueListPayloadResult) valueListPayloadHandler
            .executePayload(rule.getPayload(), rule, dataContext, SESSION);

        assertThat(payloadResult.getSuccess(), is(true));
        assertThat(payloadResult.getValueList(), notNullValue());
        assertThat(payloadResult.getMessage(), nullValue());
    }

    @Test
    public void shouldReturnSuccessResultForStringValueField() {
        Policy policy = new Policy("P0000001");
        policy.setPolicyCurrency("USD");

        DataContext dataContext = new DataContext();
        dataContext.setDataObject(policy);
        dataContext.setContextDefinition(createPolicyContext());

        RuntimeRule rule = RULE_BUILDER
            .targetPath("policyCurrency")
            .valueListPayload(ValueList.fromString(List.of("USD", "CAD")))
            .build();

        ValueListPayloadResult payloadResult = (ValueListPayloadResult) valueListPayloadHandler
            .executePayload(rule.getPayload(), rule, dataContext, SESSION);

        assertThat(payloadResult.getSuccess(), is(true));
        assertThat(payloadResult.getValueList(), notNullValue());
        assertThat(payloadResult.getMessage(), nullValue());
    }

    @Test
    public void shouldReturnFailureResultForStringValueField() {
        Policy policy = new Policy("P0000001");
        policy.setPolicyCurrency("EUR");

        DataContext dataContext = new DataContext();
        dataContext.setDataObject(policy);
        dataContext.setContextDefinition(createPolicyContext());

        RuntimeRule rule = RULE_BUILDER
            .targetPath("policyCurrency")
            .valueListPayload(ValueList.fromString(List.of("USD", "CAD")))
            .build();

        ValueListPayloadResult payloadResult = (ValueListPayloadResult) valueListPayloadHandler
            .executePayload(rule.getPayload(), rule, dataContext, SESSION);

        assertThat(payloadResult.getSuccess(), is(false));
        assertThat(payloadResult.getValueList(), notNullValue());
    }

    @Test
    public void shouldReturnSuccessResultForIntegerValueField() {
        Policy policy = new Policy("P0000001");
        policy.setPolicyTermNo(5);

        DataContext dataContext = new DataContext();
        dataContext.setDataObject(policy);
        dataContext.setContextDefinition(createPolicyContext());

        RuntimeRule rule = RULE_BUILDER
            .targetPath("policyTermNo")
            .valueListPayload(ValueList.fromNumber(
                List.of(BigDecimal.valueOf(1), BigDecimal.valueOf(2.0), BigDecimal.valueOf(5))))
            .build();

        ValueListPayloadResult payloadResult = (ValueListPayloadResult) valueListPayloadHandler
            .executePayload(rule.getPayload(), rule, dataContext, SESSION);

        assertThat(payloadResult.getSuccess(), is(true));
        assertThat(payloadResult.getValueList(), notNullValue());
    }

    @Test
    public void shouldReturnFailureResultForIntegerValueField() {
        Policy policy = new Policy("P0000001");
        policy.setPolicyTermNo(5);

        DataContext dataContext = new DataContext();
        dataContext.setDataObject(policy);
        dataContext.setContextDefinition(createPolicyContext());

        RuntimeRule rule = RULE_BUILDER
            .targetPath("policyTermNo")
            .valueListPayload(ValueList.fromNumber(
                List.of(BigDecimal.valueOf(1), BigDecimal.valueOf(2.0))))
            .build();

        ValueListPayloadResult payloadResult = (ValueListPayloadResult) valueListPayloadHandler
            .executePayload(rule.getPayload(), rule, dataContext, SESSION);

        assertThat(payloadResult.getSuccess(), is(false));
        assertThat(payloadResult.getValueList(), notNullValue());
    }

    @Test
    public void shouldReturnSuccessResultForMoneyValueField() {
        Policy policy = new Policy("P0000001");
        policy.setPolicyCost(Monetary.getDefaultAmountFactory()
            .setCurrency(Monetary.getCurrency("USD"))
            .setNumber(200)
            .create());

        DataContext dataContext = new DataContext();
        dataContext.setDataObject(policy);
        dataContext.setContextDefinition(createPolicyContext());

        RuntimeRule rule = RULE_BUILDER
            .targetPath("policyCost")
            .valueListPayload(ValueList.fromNumber(
                List.of(BigDecimal.valueOf(100), BigDecimal.valueOf(200.0), BigDecimal.valueOf(50.5))))
            .build();

        ValueListPayloadResult payloadResult = (ValueListPayloadResult) valueListPayloadHandler
            .executePayload(rule.getPayload(), rule, dataContext, SESSION);

        assertThat(payloadResult.getSuccess(), is(true));
        assertThat(payloadResult.getValueList(), notNullValue());
    }

    @Test
    public void shouldReturnFailureResultForMoneyValueField() {
        Policy policy = new Policy("P0000001");
        policy.setPolicyCost(Monetary.getDefaultAmountFactory()
            .setCurrency(Monetary.getCurrency("USD"))
            .setNumber(200.1)
            .create());

        DataContext dataContext = new DataContext();
        dataContext.setDataObject(policy);
        dataContext.setContextDefinition(createPolicyContext());

        RuntimeRule rule = RULE_BUILDER
            .targetPath("policyCost")
            .valueListPayload(ValueList.fromNumber(
                List.of(BigDecimal.valueOf(100), BigDecimal.valueOf(200.0), BigDecimal.valueOf(50.5))))
            .build();

        ValueListPayloadResult payloadResult = (ValueListPayloadResult) valueListPayloadHandler
            .executePayload(rule.getPayload(), rule, dataContext, SESSION);

        assertThat(payloadResult.getSuccess(), is(false));
        assertThat(payloadResult.getValueList(), notNullValue());
    }

    @Test
    public void shouldReturnSuccessResultForDecimalValueField() {
        Policy policy = new Policy("P0000001");
        policy.setPolicyLimit(BigDecimal.valueOf(50.5));

        DataContext dataContext = new DataContext();
        dataContext.setDataObject(policy);
        dataContext.setContextDefinition(createPolicyContext());

        RuntimeRule rule = RULE_BUILDER
            .targetPath("policyLimit")
            .valueListPayload(ValueList.fromNumber(
                List.of(BigDecimal.valueOf(100), BigDecimal.valueOf(200.0), BigDecimal.valueOf(50.5))))
            .build();

        ValueListPayloadResult payloadResult = (ValueListPayloadResult) valueListPayloadHandler
            .executePayload(rule.getPayload(), rule, dataContext, SESSION);

        assertThat(payloadResult.getSuccess(), is(true));
        assertThat(payloadResult.getValueList(), notNullValue());
    }

    @Test
    public void shouldReturnFailureResultForDecimalValueField() {
        Policy policy = new Policy("P0000001");
        policy.setPolicyLimit(BigDecimal.valueOf(50.0));

        DataContext dataContext = new DataContext();
        dataContext.setDataObject(policy);
        dataContext.setContextDefinition(createPolicyContext());

        RuntimeRule rule = RULE_BUILDER
            .targetPath("policyLimit")
            .valueListPayload(ValueList.fromNumber(
                List.of(BigDecimal.valueOf(10.0), BigDecimal.valueOf(200.0), BigDecimal.valueOf(50.5))))
            .build();

        ValueListPayloadResult payloadResult = (ValueListPayloadResult) valueListPayloadHandler
            .executePayload(rule.getPayload(), rule, dataContext, SESSION);

        assertThat(payloadResult.getSuccess(), is(false));
        assertThat(payloadResult.getValueList(), notNullValue());
    }

    @Test
    public void shouldThrowExceptionIfTypesAreIncompatible() {
        Policy policy = new Policy("P0000001");
        policy.setPolicyLimit(BigDecimal.valueOf(50.0));

        DataContext dataContext = new DataContext();
        dataContext.setDataObject(policy);
        dataContext.setContextDefinition(createPolicyContext());

        RuntimeRule rule = RULE_BUILDER
            .targetPath("policyCurrency")
            .valueListPayload(ValueList.fromNumber(
                List.of(BigDecimal.valueOf(10.0), BigDecimal.valueOf(200.0), BigDecimal.valueOf(50.5))))
            .build();

        assertThrows(KrakenRuntimeException.class, () -> valueListPayloadHandler
            .executePayload(rule.getPayload(), rule, dataContext, SESSION));
    }

    private RuntimeContextDefinition createPolicyContext() {
        return new RuntimeContextDefinition(
            "Policy",
            Map.of(),
            Map.of(
                "policyCurrency", createField("policyCurrency", PrimitiveFieldDataType.STRING),
                "policyTermNo", createField("policyTermNo", PrimitiveFieldDataType.INTEGER),
                "policyCost", createField("policyCost", PrimitiveFieldDataType.MONEY),
                "policyLimit", createField("policyLimit", PrimitiveFieldDataType.DECIMAL)
                ),
            List.of());
    }

    private ContextField createField(String name, PrimitiveFieldDataType type) {
        return new ContextField(name, type.toString(), name, Cardinality.SINGLE);
    }

}
