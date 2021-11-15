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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.factory.RulesModelFactory;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Desirializes {@link EntryPoint} in test scope
 *
 * @author psurinin
 * @since 1.0
 */
@JsonComponent
public class EntryPointDeserializer extends JsonDeserializer<EntryPoint> {
    @Override
    public EntryPoint deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        TreeNode treeNode = jsonParser.getCodec().readTree(jsonParser);
        TextNode name = (TextNode) treeNode.get("name");
        ArrayNode rules = ((ArrayNode) treeNode.get("ruleNames"));
        final EntryPoint entryPoint = RulesModelFactory.getInstance().createEntryPoint();
        entryPoint.setName(name.asText());
        final List<String> ruleNames = StreamSupport
                .stream(rules.spliterator(), false)
                .map(JsonNode::asText)
                .collect(Collectors.toList());
        entryPoint.setRuleNames(ruleNames);
        return entryPoint;
    }
}
