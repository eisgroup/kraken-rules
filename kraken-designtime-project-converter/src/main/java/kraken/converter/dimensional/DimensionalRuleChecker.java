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

package kraken.converter.dimensional;

import kraken.model.Metadata;
import kraken.model.Rule;
import kraken.model.project.KrakenProject;

/**
 * Checks is rule dimensional or not.
 * Rule is considered dimensional if it is marked as dimensional in {@link Metadata#asMap()}
 * Or rule has two or more versions.
 *
 * @see Metadata#asMap()
 * @author psurinin@eisgroup.com
 * @since 1.0.41
 */
public class DimensionalRuleChecker {

    private final KrakenProject krakenProject;

    public DimensionalRuleChecker(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
    }

    public boolean isRuleDimensional(Rule rule) {
        return rule.isDimensional() || isVersioned(rule) || hasMoreThanOneVersion(rule);
    }

    private boolean isVersioned(Rule rule) {
        return rule.getMetadata() != null && !rule.getMetadata().asMap().isEmpty();
    }

    private boolean hasMoreThanOneVersion(Rule rule) {
        return krakenProject.getRuleVersions().containsKey(rule.getName())
                ? krakenProject.getRuleVersions().get(rule.getName()).size() > 1
                : false;
    }

}
