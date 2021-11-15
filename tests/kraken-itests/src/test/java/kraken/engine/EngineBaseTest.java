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
package kraken.engine;

import java.util.List;
import java.util.stream.Stream;

import kraken.model.project.repository.StaticKrakenProjectRepository;
import kraken.runtime.EvaluationConfig;
import kraken.runtime.RuleEngine;
import kraken.runtime.RuleEngineBuilder;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.context.info.DataObjectInfoResolver;
import kraken.runtime.engine.context.info.navpath.DataNavigationContextInstanceInfoResolver;
import kraken.runtime.engine.context.type.ContextTypeAdapter;
import kraken.runtime.engine.context.type.IterableContextTypeAdapter;
import kraken.runtime.engine.result.reducers.validation.ValidationStatusReducer;
import kraken.test.TestResources;
import kraken.utils.Namespaces;
import org.junit.Before;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.SystemOutRule;

/**
 * @author psurinin
 * @author avasiliauskas
 * @since 1.0
 */
public abstract class EngineBaseTest {

    protected RuleEngine engine;
    protected Object dataObject;
    protected ValidationStatusReducer validationStatusReducer;

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Before
    public void setUp() {
        final DataNavigationContextInstanceInfoResolver resolver = new DataNavigationContextInstanceInfoResolver();
        resolver.setInfoResolver(getResolver());
        TestResources resources = getResources();
        final RuleEngineBuilder builder = RuleEngineBuilder.newInstance()
                .setKrakenProjectRepository(new StaticKrakenProjectRepository(List.of(resources.getKrakenProject())))
                .setContextInstanceResolver(resolver);
        Stream.of(getIterableTypeAdapters().toArray())
                .map(a -> ((IterableContextTypeAdapter) a))
                .forEach(builder::addIterableTypeAdapter);
        Stream.of(getInstanceTypeAdapters().toArray())
                .map(a -> ((ContextTypeAdapter) a))
                .forEach(builder::addCustomTypeAdapter);
        engine = new TestEngine(builder.buildEngine(), resources.getKrakenProject().getNamespace());
        dataObject = getDataObject();
        validationStatusReducer = new ValidationStatusReducer();
    }

    protected abstract TestResources getResources();

    protected abstract DataObjectInfoResolver getResolver();

    protected abstract Object getDataObject();

    protected abstract List<IterableContextTypeAdapter> getIterableTypeAdapters();

    protected abstract List<ContextTypeAdapter> getInstanceTypeAdapters();

    protected static class TestEngine implements RuleEngine {

        private final RuleEngine ruleEngine;
        private final String namespace;

        public TestEngine(RuleEngine ruleEngine, String namespace) {
            this.ruleEngine = ruleEngine;
            this.namespace = namespace;
        }

        @Override
        public EntryPointResult evaluate(Object data, String entryPointName) {
            return ruleEngine.evaluate(data, Namespaces.toFullName(namespace, entryPointName));
        }

        @Override
        public EntryPointResult evaluateSubtree(Object data, Object node, String entryPointName) {
            return ruleEngine.evaluateSubtree(data, node, Namespaces.toFullName(namespace, entryPointName));
        }

        @Override
        public EntryPointResult evaluate(Object data, String entryPointName, EvaluationConfig evaluationConfig) {
            return ruleEngine.evaluate(data, Namespaces.toFullName(namespace, entryPointName), evaluationConfig);
        }

        @Override
        public EntryPointResult evaluateSubtree(Object data, Object node, String entryPointName, EvaluationConfig evaluationConfig) {
            return ruleEngine.evaluateSubtree(data, node, Namespaces.toFullName(namespace, entryPointName), evaluationConfig);
        }
    }

}
