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

import java.util.List;

public class Party extends Identifiable {

    private List<PartyRole> roles;

    private String relationToPrimaryInsured;

    private PersonInfo personInfo;

    private DriverInfo driverInfo;

    public Party(String id) {
        super.setId(id);
    }

    public Party(){}

    public Party(PersonInfo personInfo) {
        this.personInfo = personInfo;
    }

    public Party(List<PartyRole> roles, String relationToPrimaryInsured, String id) {
        this.roles = roles;
        this.relationToPrimaryInsured = relationToPrimaryInsured;
        super.setId(id);
    }

    public List<? extends PartyRole> getRoles() {
        return roles;
    }

    public void setRoles(List<PartyRole> roles) {
        this.roles = roles;
    }

    public String getRelationToPrimaryInsured() {
        return relationToPrimaryInsured;
    }

    public void setRelationToPrimaryInsured(String relationToPrimaryInsured) {
        this.relationToPrimaryInsured = relationToPrimaryInsured;
    }

    public PersonInfo getPersonInfo() {
        return personInfo;
    }

    public void setPersonInfo(PersonInfo personInfo) {
        this.personInfo = personInfo;
    }

    public DriverInfo getDriverInfo() {
        return driverInfo;
    }

    public void setDriverInfo(DriverInfo driverInfo) {
        this.driverInfo = driverInfo;
    }

}
