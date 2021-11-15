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
package kraken.engine.adapter.test.domain.wrappers;

import kraken.engine.adapter.test.domain.CoverageA;
import kraken.engine.adapter.test.domain.CoverageB;
import kraken.engine.adapter.test.domain.Insured;
import kraken.engine.adapter.test.domain.RiskItem;

import java.util.Optional;
import java.util.Set;

/**
 * @author psurinin
 * @since 1.0
 */
public final class InfoAdapterHolder {

    public Optional<RiskItem> riskItem;

    public Wrapper<CoverageA> coverageAWrapper;

    public ExtendedWrapper<CoverageB> extendedWrapper;

    public Wrapper<Set<Optional<Insured>>> wrappedInsureds;

    private Iterable<RiskItem> riskItemIterable;

    public Optional<RiskItem> getRiskItem() {
        return riskItem;
    }

    public void setRiskItem(Optional<RiskItem> riskItem) {
        this.riskItem = riskItem;
    }

    public Wrapper<CoverageA> getCoverageAWrapper() {
        return coverageAWrapper;
    }

    public void setCoverageAWrapper(Wrapper<CoverageA> coverageAWrapper) {
        this.coverageAWrapper = coverageAWrapper;
    }

    public ExtendedWrapper<CoverageB> getExtendedWrapper() {
        return extendedWrapper;
    }

    public void setExtendedWrapper(ExtendedWrapper<CoverageB> extendedWrapper) {
        this.extendedWrapper = extendedWrapper;
    }

    public Wrapper<Set<Optional<Insured>>> getWrappedInsureds() {
        return wrappedInsureds;
    }

    public void setWrappedInsureds(Wrapper<Set<Optional<Insured>>> wrappedInsureds) {
        this.wrappedInsureds = wrappedInsureds;
    }

    public Iterable<RiskItem> getRiskItemIterable() {
        return riskItemIterable;
    }

    public void setRiskItemIterable(Iterable<RiskItem> riskItemIterable) {
        this.riskItemIterable = riskItemIterable;
    }
}
