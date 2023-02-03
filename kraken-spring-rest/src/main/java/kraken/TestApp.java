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
package kraken;

import java.util.Iterator;

import javax.money.MonetaryAmount;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import kraken.config.TestAppPropertiesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.fasterxml.jackson.databind.JavaType;

@EnableAutoConfiguration
@ComponentScan(basePackages = "kraken")
public class TestApp {

    @Autowired
    private TestAppPropertiesConfig properties;

    public static void main(String[] args){
        SpringApplication.run(TestApp.class, args);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public OpenAPI krakenOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("Kraken Testing Application API")
                .version(properties.getProjectVersion())
                .description("Build Info: " +
                    properties.getBuildDate() + " " +
                    properties.getBuildStartTime() + " " +
                    properties.getBuildNumber() + " " +
                    properties.getBuildRevision()
                ));
    }

    @Bean
    public MonetaryAmountConverter monetaryAmountConverter(){
        return new MonetaryAmountConverter();
    }

    /**
     * Monetary amount converter for springdoc, to correctly resolve monetary amounts in schema.
     */
    public class MonetaryAmountConverter implements ModelConverter {

        @Override
        public Schema resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
            if (type.isSchemaProperty()) {
                JavaType _type = Json.mapper().constructType(type.getType());
                if (_type != null) {
                    Class<?> cls = _type.getRawClass();
                    if (MonetaryAmount.class.isAssignableFrom(cls)) {
                        return new ObjectSchema()
                            .addProperty("amount", new NumberSchema())
                            .addProperty("currency", new StringSchema());
                    }
                }
            }
            return (chain.hasNext()) ? chain.next().resolve(type, context, chain) : null;
        }
    }

}
