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
package kraken.testproduct.domain.extended;

import kraken.testproduct.domain.Party;
import kraken.testproduct.domain.PartyRole;

import java.util.List;

public class PartyExtended extends Party {

    private List<? extends PartyRole> roles;

    private PersonInfoExtended personInfoExtended;

    private DriverInfoExtended driverInfoExtended;

    public PartyExtended(String id) {
        super.setId(id);
    }

    public PartyExtended(){}

    public PartyExtended(PersonInfoExtended personInfoExtended) {
        this.personInfoExtended = personInfoExtended;
    }

    public PartyExtended(List<PartyRoleExtended> roles, String relationToPrimaryInsured, String id, DriverInfoExtended driverInfoExtended) {
        super.setRelationToPrimaryInsured(relationToPrimaryInsured);
        this.driverInfoExtended = driverInfoExtended;
        this.roles = roles;
        super.setId(id);
    }

    public List<? extends PartyRole> getRoles() {
        return roles;
    }

    public void setRoles(List<PartyRole> roles) {
        this.roles = roles;
    }

    public PersonInfoExtended getPersonInfoExtended() {
        return personInfoExtended;
    }

    public void setPersonInfoExtended(PersonInfoExtended personInfoExtended) {
        this.personInfoExtended = personInfoExtended;
    }

    public DriverInfoExtended getDriverInfo() {
        return driverInfoExtended;
    }

    public void setDriverInfo(DriverInfoExtended driverInfoExtended) {
        this.driverInfoExtended = driverInfoExtended;
    }

}
