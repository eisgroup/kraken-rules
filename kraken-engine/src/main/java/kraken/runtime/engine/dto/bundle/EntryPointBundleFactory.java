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
package kraken.runtime.engine.dto.bundle;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import kraken.runtime.engine.core.EntryPointData;
import kraken.runtime.engine.core.EntryPointEvaluation;
import kraken.runtime.engine.core.EntryPointOrderedEvaluationFactory;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.repository.RuntimeContextRepository;
import kraken.runtime.repository.RuntimeRuleRepository;
import kraken.runtime.repository.factory.RuntimeProjectRepositoryFactory;
import kraken.utils.Namespaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 * @author psurinin
 * @since 1.0.20
 */
public final class EntryPointBundleFactory {

    private static final Logger logger = LoggerFactory.getLogger(EntryPointBundleFactory.class);
    private RuntimeProjectRepositoryFactory runtimeProjectRepositoryFactory;

    public EntryPointBundleFactory(RuntimeProjectRepositoryFactory runtimeProjectRepositoryFactory) {
        this.runtimeProjectRepositoryFactory = requireNonNull(runtimeProjectRepositoryFactory);
    }

    /**
     * Builds {@link EntryPointBundle}, containing all Kraken model data for specified entry point name.
     * Constructed bundle is used by core rule engine to evaluate rules.
     * Executed each time entry point evaluation is requested from backend.
     *
     * @param entryPointName full entry point name with namespace prefixed
     * @param context        to resolve {@link RuntimeRule}s.
     */
    public EntryPointBundle build(String entryPointName, Map<String, Object> context) {
        return build(entryPointName, context, false);
    }

    /**
     * Builds {@link EntryPointBundle}, containing all Kraken model data for specified entry point name.
     * Constructed bundle is used by core rule engine to evaluate rules.
     * Executed each time entry point evaluation is requested from backend.
     *
     * @param entryPointName full entry point name with namespace prefixed
     * @param context        to resolve {@link RuntimeRule}s.
     * @param isDelta        flag to filter out static rules and send only dimensional rules.
     *                       {@link RuntimeRule#isDimensional()}
     * @return
     */
    public EntryPointBundle build(String entryPointName, Map<String, Object> context, boolean isDelta) {
        Objects.requireNonNull(entryPointName);
        Objects.requireNonNull(context);

        String namespace = Namespaces.toNamespaceName(entryPointName);
        RuntimeContextRepository contextRepository = runtimeProjectRepositoryFactory.resolveContextRepository(namespace);
        RuntimeRuleRepository ruleRepository = runtimeProjectRepositoryFactory.resolveRuleRepository(namespace);

        String simpleEntryPointName = Namespaces.toSimpleName(entryPointName);
        Map<String, RuntimeRule> rules = ruleRepository.resolveRules(simpleEntryPointName, context);
        EntryPointData entryPointData = new EntryPointData(entryPointName, rules);
        EntryPointOrderedEvaluationFactory evaluationFactory = new EntryPointOrderedEvaluationFactory(contextRepository);
        EntryPointEvaluation entryPointEvaluation = evaluationFactory.create(entryPointData, entryPointName, isDelta);

        String version = null;
        try {
            final Properties properties = new Properties();
            properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
            version = properties.getProperty("version");
        } catch (IOException e) {
            logger.error("Failed to load 'project.properties' file", e);
        }

        return new EntryPointBundle(entryPointEvaluation, context, version);
    }

}
