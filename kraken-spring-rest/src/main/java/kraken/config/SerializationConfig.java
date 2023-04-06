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

package kraken.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import javax.money.MonetaryAmount;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;
import org.javamoney.moneta.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import kraken.utils.Dates;

/**
 * @author psurinin@eisgroup.com
 * @since 1.1.0
 */
@Configuration
public class SerializationConfig {

    @Configuration
    public static class WebMvcConfig extends WebMvcConfigurerAdapter {

        @Autowired
        private ObjectMapper objectMapper;

        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        }
    }

    @Bean
    public Module module(){
        SimpleModule module = new SimpleModule();
        module.addDeserializer(MonetaryAmount.class, new MoneyDeserializer());
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        return module;
    }

    public static class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            String isoDateTime = parser.getText().trim();
            return Dates.convertISOToLocalDateTime(isoDateTime);
        }
    }

    public static class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
        @Override
        public void serialize(LocalDateTime localDateTime, JsonGenerator g, SerializerProvider p) throws IOException {
            g.writeString(Dates.convertLocalDateTimeToISO(localDateTime));
        }
    }

    public static class MoneyDeserializer extends JsonDeserializer<MonetaryAmount> {

        @Override
        public MonetaryAmount deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            String currencyCode = "";
            ObjectMapper mapper = (ObjectMapper) jp.getCodec();
            ObjectNode root = mapper.readTree(jp);
            JsonNode amountNode = root.findValue("amount");
            String amount = null;
            if (null != amountNode) {
                amount = amountNode.asText();
            }
            JsonNode currencyUnitNode = root.get("currency");
            currencyCode = currencyUnitNode.textValue();
            if (StringUtils.isBlank(amount) || StringUtils.isBlank(currencyCode)) {
                throw new IOException("unable to parse json");
            }
            return Money.parse(currencyCode + " " + amount);
        }}
}
