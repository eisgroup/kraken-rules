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

package kraken.facade;

import kraken.TestApp;
import kraken.config.*;
import kraken.model.EntryPointName;
import kraken.namespace.Namespaces;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author psurinin@eisgroup.com
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {
        TestApp.class,
        TestAppConfig.class,
        SerializationConfig.class,
        TestAppPropertiesConfig.class,
        EntryPointDeserializer.class,
        RuleDeserializer.class,
        ContextDefinitionDeserializer.class
})
public abstract class AbstractFacadeTest {

    @Autowired
    protected TestRestTemplate rest;

    protected String fullEntryPointName(EntryPointName entryPointName) {
        return Namespaces.toFullName("Policy", entryPointName.name());
    }

}
