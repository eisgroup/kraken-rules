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
package kraken.model.project.gson;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

import kraken.model.Dimension;
import kraken.model.DimensionDataType;
import kraken.model.Metadata;
import kraken.model.Rule;
import kraken.model.ValueList.DataType;
import kraken.model.factory.RulesModelFactory;
import kraken.model.payload.PayloadType;
import kraken.model.project.KrakenProject;
import kraken.model.validation.ValueListPayload;
import kraken.model.project.gson.KrakenGsonAdapter.Builder;

/**
 * @author Tomas Dapkunas
 * @since 1.48.0
 */
public class KrakenGsonAdapterTest {

    private static final List<Dimension> DEFAULT_DIMENSIONS = Arrays.stream(DimensionDataType.values())
        .map(dataType -> {
            Dimension dimension = RulesModelFactory.getInstance().createDimension();
            dimension.setName(dataType.name().toLowerCase());
            dimension.setDataType(dataType);

            return dimension;
        })
        .collect(Collectors.toList());

    private KrakenProject krakenProject;

    @Before
    public void setUp() {
        krakenProject = mock(KrakenProject.class);
        when(krakenProject.getDimensions()).thenReturn(DEFAULT_DIMENSIONS);
    }

    @Test
    public void shouldSerializeMetadata() {
        Gson gson = new Builder(krakenProject)
            .setPrettyPrinting()
            .create();

        String json = "" +
            "{\n" +
            "    \"type\": \"Metadata\",\n" +
            "    \"properties\": {\n" +
            "        \"string\": \"string\",\n" +
            "        \"boolean\": true,\n" +
            "        \"date\": \"2020-02-02\",\n" +
            "        \"datetime\": \"2020-02-02T01:01:01Z\",\n" +
            "        \"decimal\": 10.1,\n" +
            "        \"integer\": 10\n" +
            "    }\n" +
            "}";
        Metadata metadata = gson.fromJson(json, Metadata.class);
        assertThat(metadata.getProperty("string"), is("string"));
        assertThat(metadata.getProperty("boolean"), is(true));
        assertThat(metadata.getProperty("date"), is(LocalDate.of(2020, 2, 2)));
        assertThat(metadata.getProperty("datetime"), instanceOf(LocalDateTime.class));
        assertThat(metadata.getProperty("decimal"), is(BigDecimal.valueOf(10.1)));
        assertThat(metadata.getProperty("integer"), is(10));
    }

    @Test
    public void shouldDeserializeMetadata() {
        Gson gson = new Builder(krakenProject)
            .setPrettyPrinting()
            .create();

        Metadata metadataJava = RulesModelFactory.getInstance().createMetadata();
        metadataJava.setProperty("string", "string");
        metadataJava.setProperty("boolean", true);
        metadataJava.setProperty("date", LocalDate.of(2020, 2, 2));
        metadataJava.setProperty("decimal", BigDecimal.valueOf(10.1D));
        metadataJava.setProperty("integer", 10);

        String metadataJson = gson.toJson(metadataJava);
        String expectedJson = "" +
            "{\n" +
            "  \"type\": \"Metadata\",\n" +
            "  \"properties\": {\n" +
            "    \"date\": \"2020-02-02\",\n" +
            "    \"boolean\": true,\n" +
            "    \"string\": \"string\",\n" +
            "    \"integer\": 10,\n" +
            "    \"decimal\": 10.1\n" +
            "  }\n" +
            "}";

        assertThat(metadataJson, is(expectedJson));
    }

    @Test
    public void shouldSerializeStringValueListTypeRule() {
        Gson gson = new Builder(krakenProject)
            .setPrettyPrinting()
            .create();

        String ruleJson = ""
            + "{"
            +   "\"physicalNamespace\":\"RulesAutoPolicy\","
            +   "\"context\":\"PersonalPolicySummary\","
            +   "\"targetPath\":\"policyNumber\","
            +   "\"payload\":{"
            +     "\"type\":\"ValueListPayload\","
            +     "\"severity\":\"critical\","
            +     "\"valueList\":{"
            +        "\"valueType\":\"STRING\","
            +        "\"values\":[\"POL001\", \"POL002\"]"
            +     "}"
            +   "},"
            + "\"name\":\"Value List Rule\""
            + "}";

        Rule rule = gson.fromJson(ruleJson, Rule.class);

        assertThat(rule.getName(), is("Value List Rule"));
        assertThat(rule.getContext(), is("PersonalPolicySummary"));
        assertThat(rule.getTargetPath(), is("policyNumber"));
        assertThat(rule.getPayload().getPayloadType(), is(PayloadType.VALUE_LIST));

        ValueListPayload valueListPayload = (ValueListPayload) rule.getPayload();

        assertThat(valueListPayload.getValueList().getValueType(), is(DataType.STRING));
        assertThat(valueListPayload.getValueList().getValues(), containsInAnyOrder("POL001", "POL002"));
    }

    @Test
    public void shouldSerializeDecimalValueListTypeRule() {
        Gson gson = new Builder(krakenProject)
            .setPrettyPrinting()
            .create();

        String ruleJson = ""
            + "{"
            +   "\"physicalNamespace\":\"RulesAutoPolicy\","
            +   "\"context\":\"PersonalPolicySummary\","
            +   "\"targetPath\":\"policyNumber\","
            +   "\"payload\":{"
            +     "\"type\":\"ValueListPayload\","
            +     "\"severity\":\"critical\","
            +     "\"valueList\":{"
            +        "\"valueType\":\"DECIMAL\","
            +        "\"values\":[1, 2]"
            +     "}"
            +   "},"
            + "\"name\":\"Value List Rule\""
            + "}";

        Rule rule = gson.fromJson(ruleJson, Rule.class);

        assertThat(rule.getName(), is("Value List Rule"));
        assertThat(rule.getContext(), is("PersonalPolicySummary"));
        assertThat(rule.getTargetPath(), is("policyNumber"));
        assertThat(rule.getPayload().getPayloadType(), is(PayloadType.VALUE_LIST));

        ValueListPayload valueListPayload = (ValueListPayload) rule.getPayload();

        assertThat(valueListPayload.getValueList().getValueType(), is(DataType.DECIMAL));
        assertThat(valueListPayload.getValueList().getValues(), containsInAnyOrder(BigDecimal.valueOf(1), BigDecimal.valueOf(2)));
    }
}
