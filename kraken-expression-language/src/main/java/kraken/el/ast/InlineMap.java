/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
package kraken.el.ast;

import kraken.el.ast.token.Token;
import kraken.el.scope.Scope;
import kraken.el.scope.SymbolTable;
import kraken.el.scope.symbol.VariableSymbol;
import kraken.el.scope.type.Type;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author mulevicius
 */
public class InlineMap extends Expression {

    private List<KeyValuePair> keyValuePairs;

    public InlineMap(List<KeyValuePair> keyValuePairs, Scope scope, Token token) {
        super(NodeType.INLINE_MAP, scope, buildMapType(keyValuePairs), token);
        this.keyValuePairs = keyValuePairs;
    }

    public List<KeyValuePair> getKeyValuePairs() {
        return keyValuePairs;
    }

    private static Type buildMapType(List<KeyValuePair> keyValuePairs) {
        return new Type(
                "InlineMap_" + UUID.randomUUID().toString(),
                new SymbolTable(
                        List.of(),
                        keyValuePairs.stream()
                                .map(keyValuePair -> new VariableSymbol(keyValuePair.getKey(), keyValuePair.getValue().getEvaluationType()))
                                .collect(Collectors.toMap(r -> r.getName(), r -> r))
                )
        );
    }

    public static class KeyValuePair {

        private String key;

        private Expression value;

        public KeyValuePair(String key, Expression value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Expression getValue() {
            return value;
        }
    }

    @Override
    public String toString() {
        return keyValuePairs.stream()
                .map(entry -> "'" + entry.getKey() + "' : " + entry.getValue())
                .collect(Collectors.joining(",", "{", "}"));
    }
}
