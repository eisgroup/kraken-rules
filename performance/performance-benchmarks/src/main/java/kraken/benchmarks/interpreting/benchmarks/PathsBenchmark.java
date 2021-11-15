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

/**
 * @author mulevicius
 */
public class PathsBenchmark extends AbstractExpressionBenchmark {

    private int pathLength = 100;

    public static void main(String[] args) {
        new PathsBenchmark().log();
    }

    @Override
    public String[] getExpressions() {
        String expression = "coverage.riskItem";
        for(int i = 1; i < pathLength / 2; i++) {
            expression += ".coverage.riskItem";
        }
        return new String[] { expression };
    }

    @Override
    public Object getContext() {
        RiskItem riskItem = new RiskItem();
        for(int i = 0; i < pathLength / 2; i++) {
            Coverage coverage = new Coverage();
            coverage.setRiskItem(riskItem);
            riskItem = new RiskItem();
            riskItem.setCoverage(coverage);
        }
        return riskItem;
    }

    public static class RiskItem {
        private Coverage coverage;

        public Coverage getCoverage() {
            return coverage;
        }

        public void setCoverage(Coverage coverage) {
            this.coverage = coverage;
        }
    }

    public static class Coverage {
        private RiskItem riskItem;

        public RiskItem getRiskItem() {
            return riskItem;
        }

        public void setRiskItem(RiskItem riskItem) {
            this.riskItem = riskItem;
        }
    }
}
