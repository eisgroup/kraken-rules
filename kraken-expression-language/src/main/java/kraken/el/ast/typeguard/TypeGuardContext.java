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
package kraken.el.ast.typeguard;

import java.util.Map;
import java.util.Optional;

import kraken.el.ast.token.Token;
import kraken.el.scope.type.Type;

/**
 * Holds information about type guards applicable for current expression nodes
 * 
 * @author mulevicius
 */
public class TypeGuardContext {

    private final Map<String, TypeFact> facts;

    public TypeGuardContext(Map<String, TypeFact> facts) {
        this.facts = facts;
    }

    public Map<String, TypeFact> getFacts() {
        return facts;
    }

    public Optional<Type> findTypeFactOverride(Token token) {
        return Optional.ofNullable(facts.get(token.getText())).map(TypeFact::getType);
    }

    public static TypeGuardContext empty() {
        return new TypeGuardContext(Map.of());
    }
}
