/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.engine.dto.bundle.trace;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import kraken.dimensions.DimensionSet;
import kraken.runtime.engine.dto.bundle.EntryPointBundle;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.tracer.Operation;

/**
 * @author mulevicius
 */
public class EntryPointBundleBuildOperation implements Operation<EntryPointBundle> {

    private final String entryPointName;
    private final Set<DimensionSet> excludes;

    public EntryPointBundleBuildOperation(String entryPointName, Set<DimensionSet> excludes) {
        this.entryPointName = entryPointName;
        this.excludes = excludes;
    }

    @Override
    public String describe() {
        StringBuilder message = new StringBuilder("Collecting rules for entry point '" + entryPointName + "'");

        if (!excludes.isEmpty()) {
            message.append(". Rules that vary by these dimension sets will be excluded from entry point bundle: ");

            for (DimensionSet set : excludes) {
                message.append(set.toString());
            }
        }

        return message.toString();
    }

    @Override
    public String describeAfter(EntryPointBundle bundle) {
        var template = "Collected rules for entry point '%s':%s";

        return String.format(template, entryPointName, describeRules(bundle));
    }

    private String describeRules(EntryPointBundle bundle) {
        if (bundle.getEvaluation().getRules().size() > 0) {
            return System.lineSeparator() + bundle.getEvaluation().getRules()
                .stream()
                .map(RuntimeRule::getName)
                .collect(Collectors.joining(System.lineSeparator()));
        }

        return "";
    }
}
