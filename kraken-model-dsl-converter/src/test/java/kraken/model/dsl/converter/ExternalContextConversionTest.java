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
package kraken.model.dsl.converter;

import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.context.external.ExternalContextDefinitionReference;
import kraken.model.factory.RulesModelFactory;
import kraken.model.resource.builder.ResourceBuilder;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;

/**
 * Unit tests to test conversion of {@code ExternalContext}'s to string.
 *
 * @author Tomas Dapkunas
 * @since 1.3.0
 */
public class ExternalContextConversionTest {

    private static final String SEPARATOR = System.lineSeparator();
    private static final RulesModelFactory RULES_MODEL_FACTORY = RulesModelFactory.getInstance();

    private final DSLModelConverter converter = new DSLModelConverter();

    @Test
    public void shouldConvertExternalContexts() {
        Map<String, ExternalContextDefinition> granddaughterBounded = new HashMap<>();
        granddaughterBounded.put("granddaughterBounded", createExternalContextDefinition("GranddaughterBounded"));

        Map<String, ExternalContextDefinition> grandsonBounded = new HashMap<>();
        grandsonBounded.put("grandsonBounded", createExternalContextDefinition("GrandsonBounded"));

        Map<String, ExternalContextDefinition> childBounded = new HashMap<>();
        childBounded.put("childBounded", createExternalContextDefinition("ChildBounded"));

        Map<String, ExternalContextDefinition> parentBounded = new HashMap<>();
        parentBounded.put("parentBounded", createExternalContextDefinition("ParentBounded"));
        parentBounded.put("parentAnotherBounded", createExternalContextDefinition("ParentAnotherBounded"));

        ExternalContext granddaughter = createExternalContext(Map.of(), granddaughterBounded);
        ExternalContext grandson = createExternalContext(Map.of(), grandsonBounded);

        Map<String, ExternalContext> grandChildContexts = new HashMap<>();
        grandChildContexts.put("granddaughter", granddaughter);
        grandChildContexts.put("grandson", grandson);

        ExternalContext child = createExternalContext(grandChildContexts, childBounded);

        Map<String, ExternalContext> parentContexts = new HashMap<>();
        parentContexts.put("child", child);

        ExternalContext parent = createExternalContext(parentContexts, parentBounded);

        String convertedString = convert(parent);

        assertEquals(
                "ExternalContext {" + SEPARATOR +
                        "    child: {" + SEPARATOR +
                        "        grandson: {" + SEPARATOR +
                        "            grandsonBounded: GrandsonBounded" + SEPARATOR +
                        "        }," + SEPARATOR +
                        "        granddaughter: {" + SEPARATOR +
                        "            granddaughterBounded: GranddaughterBounded" + SEPARATOR +
                        "        }," + SEPARATOR +
                        "        childBounded: ChildBounded" + SEPARATOR +
                        "    }," + SEPARATOR +
                        "    parentBounded: ParentBounded," + SEPARATOR +
                        "    parentAnotherBounded: ParentAnotherBounded" + SEPARATOR +
                        "}" + SEPARATOR, convertedString);
    }

    private String convert(ExternalContext externalContext) {
        return converter.convert(ResourceBuilder.getInstance()
                .setExternalContext(externalContext)
                .build());
    }

    private ExternalContext createExternalContext(Map<String, ExternalContext> childContexts,
                                                  Map<String, ExternalContextDefinition> boundedContextsDefinitions) {
        ExternalContext eCtx = RULES_MODEL_FACTORY.createExternalContext();
        eCtx.setContexts(childContexts);
        eCtx.setExternalContextDefinitions(boundedContextsDefinitions.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> {
                            ExternalContextDefinitionReference ref = RULES_MODEL_FACTORY.createExternalContextDefinitionReference();
                            ref.setName(entry.getValue().getName());

                            return ref;
                        })));
        eCtx.setPhysicalNamespace("whatever");
        return eCtx;
    }

    private ExternalContextDefinition createExternalContextDefinition(String name) {
        ExternalContextDefinition eCtxDef = RULES_MODEL_FACTORY.createExternalContextDefinition();
        eCtxDef.setName(name);
        eCtxDef.setPhysicalNamespace("whatever");
        return eCtxDef;
    }

}
