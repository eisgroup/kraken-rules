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
package kraken.el.functionregistry.functions;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;

import javax.money.Monetary;
import javax.money.MonetaryAmount;

import org.junit.Test;

public class MoneyFunctionsTest {

    @Test
    public void shouldExtractNumberFromMonetaryAmount() {
        MonetaryAmount monetaryAmount = Monetary.getDefaultAmountFactory()
            .setCurrency("USD")
            .setNumber(1.99)
            .create();

        Number numberValue = MoneyFunctions.fromMoney(monetaryAmount);

        assertThat(numberValue, equalTo(BigDecimal.valueOf(1.99)));
    }

    @Test
    public void shouldReturnNumberValue() {
        Number numberValue = MoneyFunctions.fromMoney(1.99);

        assertThat(numberValue, equalTo(1.99));
    }

    @Test
    public void shouldReturnNullWhenParameterIsNull() {
        Number numberValue = MoneyFunctions.fromMoney(null);

        assertThat(numberValue, is(nullValue()));
    }

}
