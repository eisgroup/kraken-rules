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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rimas on 25/01/17.
 */
public final class RiskItem {

    private List<CoverageA> coverageAs;

    private CoverageB coverageB;

    private String itemName;

    private RiskItem() {
    }

    public RiskItem(String itemName) {
        this();
        this.itemName = itemName;
        this.coverageAs = new ArrayList<>();
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setCoverageAs(List<CoverageA> coverageAs) {
        this.coverageAs = coverageAs;
    }

    public List<CoverageA> getCoverageAs() {
        return coverageAs;
    }

    public void setCoverageB(CoverageB coverageB) {
        this.coverageB = coverageB;
    }

    public CoverageB getCoverageB() {
        return coverageB;
    }
}
