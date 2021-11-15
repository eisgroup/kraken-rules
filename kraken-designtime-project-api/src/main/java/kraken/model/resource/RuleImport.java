/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.model.resource;

import java.util.Objects;

import kraken.annotations.API;

/**
 * Represents a rule import by {@link #ruleName} from {@link #namespace} to the {@link Resource} in another namespace
 *
 * @author mulevicius
 */
@API
public class RuleImport {

    private String namespace;

    private String ruleName;

    public RuleImport(String namespace, String ruleName) {
        this.namespace = namespace;
        this.ruleName = ruleName;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getRuleName() {
        return ruleName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleImport that = (RuleImport) o;
        return namespace.equals(that.namespace) &&
                ruleName.equals(that.ruleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, ruleName);
    }
}
