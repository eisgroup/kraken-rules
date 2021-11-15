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
package kraken.benchmarks.interpreting.benchmarks;

import kraken.benchmarks.interpreting.AbstractExpressionBenchmark;
import kraken.el.scope.Scope;
import kraken.el.scope.ScopeType;
import kraken.el.scope.SymbolTable;
import kraken.el.scope.symbol.VariableSymbol;
import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.Type;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author mulevicius
 */
public class FilteringBenchmark extends AbstractExpressionBenchmark {

    public static void main(String[] args) {
        new FilteringBenchmark().log();
    }

    @Override
    public String[] getExpressions() {
        String expression = "riskItems[Count(riskItems[*].coverages[this.limitAmount > 5000]) > 1].coverages[this.limitAmount > 5000]";
        return new String[] { expression };
    }

    @Override
    public Object getContext() {
        Collection<RiskItem> riskItems = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            Collection<Coverage> coverages = new ArrayList<>();
            for(int c = 0; c < 100; c++) {
                coverages.add(new Coverage(BigDecimal.valueOf(i*c)));
            }
            riskItems.add(new RiskItem(coverages));
        }
        return new Policy(riskItems);
    }

    // scope is required so that filtering expression has 'this' prepended correctly when translating for MVEL execution
    @Override
    public Scope getContextScope() {
        Type coverage = new Type(
                "Coverage",
                new SymbolTable(
                        List.of(),
                        Map.of("limitAmount", new VariableSymbol("limitAmount", Type.NUMBER))
                )
        );
        Type riskItem = new Type(
                "RiskItem",
                new SymbolTable(
                        List.of(),
                        Map.of("coverages", new VariableSymbol("coverages", ArrayType.of(coverage)))
                )
        );
        Type policy = new Type(
                "Policy",
                new SymbolTable(
                        List.of(),
                        Map.of("riskItems", new VariableSymbol("riskItems", ArrayType.of(riskItem)))
                )
        );

        Scope globalScope = new Scope(Type.ANY, Map.of());
        return new Scope(ScopeType.LOCAL, globalScope, policy);
    }

    public static class Policy {
        private Collection<RiskItem> riskItems;

        public Policy(Collection<RiskItem> riskItems) {
            this.riskItems = riskItems;
        }

        public Collection<RiskItem> getRiskItems() {
            return riskItems;
        }
    }

    public static class RiskItem {
        private Collection<Coverage> coverages;

        public RiskItem(Collection<Coverage> coverages) {
            this.coverages = coverages;
        }

        public Collection<Coverage> getCoverages() {
            return coverages;
        }
    }

    public static class Coverage {
        private BigDecimal limitAmount;

        public Coverage(BigDecimal limitAmount) {
            this.limitAmount = limitAmount;
        }

        public BigDecimal getLimitAmount() {
            return limitAmount;
        }
    }

}
