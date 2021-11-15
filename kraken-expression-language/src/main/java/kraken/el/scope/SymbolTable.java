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
package kraken.el.scope;

import kraken.el.scope.symbol.FunctionSymbol;
import kraken.el.scope.symbol.VariableSymbol;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents all {@link kraken.el.scope.symbol.Symbol}s that can be referred to in expression within current {@link Scope}.
 * That includes all locally defined or available variables represented by {@link VariableSymbol}
 * and functions represented by {@link FunctionSymbol}.
 * <p/>
 * Note, that this is a {@link SymbolTable} for a current {@link Scope} only and {@link Scope}'s can be nested.
 *
 * @author mulevicius
 */
public class SymbolTable {

    private final Collection<FunctionSymbol> functions;
    private final Map<String, VariableSymbol> references;

    public SymbolTable() {
        this.functions = Collections.emptyList();
        this.references = Collections.emptyMap();
    }

    public SymbolTable(Collection<FunctionSymbol> functions, Map<String, VariableSymbol> references) {
        this.functions = Collections.unmodifiableCollection(functions);
        this.references = Collections.unmodifiableMap(references);
    }

    public Collection<FunctionSymbol> getFunctions() {
        return functions;
    }

    public Map<String, VariableSymbol> getReferences() {
        return references;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SymbolTable that = (SymbolTable) o;
        return Objects.equals(functions, that.functions) &&
                Objects.equals(references, that.references);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functions, references);
    }

    @Override
    public String toString() {
        return "{" +
                references.values()
                        .stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(",")) +

                functions.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(",")) +
                "}";
    }
}
