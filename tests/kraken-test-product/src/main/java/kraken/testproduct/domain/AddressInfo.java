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

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AddressInfo extends Identifiable {

    private Boolean doNotSolicit;

    private String countryCd;

    private String postalCode;

    private String city;

    private String street;
    
    private AddressLine1 addressLine1;
    
    private AddressLine2 addressLine2;

    public AddressInfo() {
    }

    public AddressInfo(Boolean doNotSolicit, String countryCd, String postalCode, String city) {
        this.doNotSolicit = doNotSolicit;
        this.countryCd = countryCd;
        this.postalCode = postalCode;
        this.city = city;
    }

    public Boolean getDoNotSolicit() {
        return doNotSolicit;
    }

    public void setDoNotSolicit(Boolean doNotSolicit) {
        this.doNotSolicit = doNotSolicit;
    }

    public String getCountryCd() {
        return countryCd;
    }

    public void setCountryCd(String countryCd) {
        this.countryCd = countryCd;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public AddressLine getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(AddressLine1 addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public AddressLine getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(AddressLine2 addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public Collection<AddressLine> getAddressLines() {
        return Stream.of(addressLine1, addressLine2)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
