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

package kraken.testproduct.domain;

import kraken.testproduct.domain.meta.Identifiable;

/**
 * @author psurinin@eisgroup.com
 * @since 1.0.41
 */
public class Referer extends Identifiable {

    private String name;
    private AddressInfo addressInfo;
    private RefererInfo refererInfo;
    private SuperReferer superReferer;

    public Referer() {
    }

    public Referer(String name, AddressInfo addressInfo, RefererInfo refererInfo) {
        this.name = name;
        this.addressInfo = addressInfo;
        this.refererInfo = refererInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AddressInfo getAddressInfo() {
        return addressInfo;
    }

    public void setAddressInfo(AddressInfo addressInfo) {
        this.addressInfo = addressInfo;
    }

    public RefererInfo getRefererInfo() {
        return refererInfo;
    }

    public void setRefererInfo(RefererInfo refererInfo) {
        this.refererInfo = refererInfo;
    }

    public SuperReferer getSuperReferer() {
        return superReferer;
    }

    public void setSuperReferer(SuperReferer superReferer) {
        this.superReferer = superReferer;
    }
}
