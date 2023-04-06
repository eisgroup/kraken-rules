/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.MonetaryAmountFactory;
import javax.money.UnknownCurrencyException;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * @author mulevicius
 */
public class GsonUtils {

    public static Gson prettyGson() {
        return builder().setPrettyPrinting().create();
    }

    public static Gson gson() {
        return builder().create();
    }

    private static GsonBuilder builder() {
        return new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (date, type, ctx) ->
                new JsonPrimitive(Dates.convertLocalDateToISO(date)))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (elem, type, ctx) -> {
                if(elem.isJsonNull() || StringUtils.isEmpty(elem.getAsString())) {
                    return null;
                }
                return Dates.convertISOToLocalDate(elem.getAsString());
            })
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (date, type, ctx) -> {
                return new JsonPrimitive(Dates.convertLocalDateTimeToISO(date));
            })
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (elem, type, ctx) -> {
                if(elem.isJsonNull() || StringUtils.isEmpty(elem.getAsString())) {
                    return null;
                }
                return Dates.convertISOToLocalDateTime(elem.getAsString());
            })
            .registerTypeAdapterFactory(new MoneyTypeAdapterFactory())
            .disableHtmlEscaping();
    }

    static class MoneyTypeAdapterFactory implements TypeAdapterFactory {

        private final MonetaryAmountFactory<? extends MonetaryAmount> monetaryFactory;

        /**
         * This is the Default implementation. Here, the implementation of the {@code javax.money} defined in the classpath
         * will be used.
         */
        public MoneyTypeAdapterFactory() {
            this(Monetary.getDefaultAmountFactory());
        }

        public MoneyTypeAdapterFactory(final MonetaryAmountFactory<? extends MonetaryAmount> monetaryFactory) {
            this.monetaryFactory = monetaryFactory;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> typeToken) {

            final Class<T> clazz = (Class<T>) typeToken.getRawType();

            if (MonetaryAmount.class.isAssignableFrom(clazz)) {
                return (TypeAdapter<T>) new MonetaryAmountAdapter(monetaryFactory);
            } else if (CurrencyUnit.class.isAssignableFrom(clazz)) {
                return (TypeAdapter<T>) new CurrencyUnitAdapter();
            }

            return null;
        }
    }

    static class CurrencyUnitAdapter extends TypeAdapter<CurrencyUnit> {

        @Override
        public void write(final JsonWriter writer, final CurrencyUnit value) throws IOException {

            if (value == null) {
                writer.nullValue();
                return;
            }

            writer.value(value.getCurrencyCode());
        }

        @Override
        public CurrencyUnit read(final JsonReader reader) throws IOException {

            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
                return null;
            }

            try {
                return Monetary.getCurrency(reader.nextString());
            } catch (UnknownCurrencyException e) {
                throw new JsonSyntaxException(e);
            }
        }
    }

    static class MonetaryAmountAdapter extends TypeAdapter<MonetaryAmount> {

        private MonetaryAmountFactory<? extends MonetaryAmount> monetaryFactory;

        MonetaryAmountAdapter(final MonetaryAmountFactory<? extends MonetaryAmount> monetaryFactory) {
            this.monetaryFactory = monetaryFactory;
        }

        @Override
        public void write(final JsonWriter writer, final MonetaryAmount value) throws IOException {

            if (value == null) {
                writer.nullValue();
                return;
            }

            writer.beginObject()                                                 //
                .name("amount")                                                //
                .value(value.getNumber().numberValueExact(BigDecimal.class))   //
                .name("currency").value(value.getCurrency().getCurrencyCode()) //
                .endObject();
        }

        @Override
        public MonetaryAmount read(final JsonReader reader) throws IOException {

            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
                return null;
            }

            BigDecimal amount = null;
            CurrencyUnit currency = null;

            try {

                reader.beginObject();
                while (reader.hasNext()) {

                    switch (reader.nextName()) {

                        case "amount" :
                            amount = new BigDecimal(reader.nextString());
                            break;

                        case "currency" :
                            currency = Monetary.getCurrency(reader.nextString());
                            break;

                        default :
                            reader.skipValue();
                    }
                }

                reader.endObject();

            } catch (NumberFormatException e) {
                throw new JsonSyntaxException("Non numeric String contained in the [amount] field.", e);
            } catch (UnknownCurrencyException e) {
                throw new JsonSyntaxException(e);
            }

            if (amount == null || currency == null) {
                String errorMessage = buildMissingFieldsErrorMessage(amount, currency);
                throw new JsonSyntaxException(errorMessage);
            }

            return monetaryFactory.setCurrency(currency).setNumber(amount).create();
        }

        private String buildMissingFieldsErrorMessage(final BigDecimal amount, final CurrencyUnit currency) {
            StringBuilder builder = new StringBuilder();
            builder.append("Missing required fields from Monetary Amount: [");

            if (amount == null) {
                builder.append("amount");
            }

            if (currency == null) {
                builder.append("currency");
            }

            builder.append("].");
            return builder.toString();
        }
    }
}
