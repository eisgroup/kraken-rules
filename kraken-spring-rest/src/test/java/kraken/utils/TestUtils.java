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
package kraken.utils;

import kraken.model.Rule;
import kraken.model.dsl.converter.DSLModelConverter;
import kraken.model.factory.RulesModelFactory;
import kraken.model.resource.builder.ResourceBuilder;
import kraken.model.state.VisibilityPayload;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class TestUtils {

    private final static RulesModelFactory RULE_MODEL_FACTORY = RulesModelFactory.getInstance();

    public static List<Rule> createMockRules(String... ruleNames){
        return Arrays.asList(ruleNames).stream()
                .map(ruleName -> {
                    Rule rule = RULE_MODEL_FACTORY.createRule();
                    rule.setName(ruleName);
                    rule.setTargetPath("policyNumber");
                    rule.setContext("Policy");

                    VisibilityPayload p = RULE_MODEL_FACTORY.createVisibilityPayload();
                    p.setVisible(false);
                    rule.setPayload(p);

                    return rule;
                }).collect(Collectors.toList());
    }

    public static String toDSL(Collection<Rule> rules) {
        return new DSLModelConverter().convert(
                ResourceBuilder.getInstance().addRules(rules).build()
        );
    }

}
