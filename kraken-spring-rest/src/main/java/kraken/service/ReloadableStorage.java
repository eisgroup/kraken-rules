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

package kraken.service;

import kraken.model.EntryPointName;
import kraken.model.Rule;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.factory.RulesModelFactory;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author psurinin@eisgroup.com
 * @since 1.1.0
 */
public class ReloadableStorage {

    private final Map<EntryPointName, EntryPoint> entryPoints;
    private final Map<String, Rule> rules;
    private String namespace;

    public ReloadableStorage(String namespace) {
        this.namespace = namespace;
        entryPoints = new HashMap<>();
        rules = new HashMap<>();
        initEntryPoints();
    }

    public void add(EntryPointName entryPointName, Collection<Rule> rules) {
        final EntryPoint entryPoint = this.entryPoints.get(entryPointName);
        for (Rule rule : rules) {
            if (!entryPoint.getRuleNames().contains(rule.getName())) {
                entryPoint.getRuleNames().add(rule.getName());
            }
            this.rules.put(rule.getName(), rule);
        }
    }

    public void remove(EntryPointName entryPointName, Collection<String> ruleNames) {
        final EntryPoint entryPoint = this.entryPoints.get(entryPointName);
        for (String ruleName : ruleNames) {
            entryPoint.getRuleNames().remove(ruleName);
            this.rules.remove(ruleName);
        }
    }

    public void clear() {
        rules.clear();
        initEntryPoints();
    }

    public void clear(EntryPointName entryPointName) {
        entryPoints.put(entryPointName, createEntryPoint(entryPointName));
    }

    public Stream<EntryPoint> getEntryPoints() {
        return entryPoints.values().stream();
    }

    public Stream<Rule> getRules() {
        return rules.values().stream();
    }

    private EntryPoint createEntryPoint(EntryPointName entryPointName) {
        final EntryPoint entryPoint = RulesModelFactory.getInstance().createEntryPoint();
        entryPoint.setRuleNames(new ArrayList<>());
        entryPoint.setName(entryPointName.name());
        entryPoint.setPhysicalNamespace(namespace);
        return entryPoint;
    }

    private void initEntryPoints() {
        entryPoints.put(EntryPointName.QA1, createEntryPoint(EntryPointName.QA1));
        entryPoints.put(EntryPointName.QA2, createEntryPoint(EntryPointName.QA2));
        entryPoints.put(EntryPointName.QA3, createEntryPoint(EntryPointName.QA3));
        entryPoints.put(EntryPointName.QA4, createEntryPoint(EntryPointName.QA4));
        entryPoints.put(EntryPointName.QA5, createEntryPoint(EntryPointName.QA5));
        entryPoints.put(EntryPointName.UI, createEntryPoint(EntryPointName.UI));
    }
}
