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
package kraken.engine.adapter.test.domain;
/**
 * Created by rimas on 25/01/17.
 */
public final class CoverageB {

    private String coverageCd;

    private double limitAmount;

    public CoverageB(String coverageCd, double limitAmount) {
        this.coverageCd = coverageCd;
        this.limitAmount = limitAmount;
    }

    public void setCoverageCd(String coverageCd) {
        this.coverageCd = coverageCd;
    }

    public void setLimitAmount(double limitAmount) {
        this.limitAmount = limitAmount;
    }

    public String getCoverageCd() {
        return coverageCd;
    }

    public double getLimitAmount() {
        return limitAmount;
    }

}
