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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Vehicle extends Identifiable {

    private Boolean included;

    private String model;

    private Integer modelYear;

    private BigDecimal newValue;

    private BigDecimal costNew;

    private Long declaredAnnualMiles;

    private Long odometerReading;

    private Integer numDaysDrivenPerWeek;

    private LocalDate purchasedDate;

    private List<LocalDate> serviceHistory = new ArrayList<>();

    private List<AnubisCoverage> anubisCoverages;

    private List<COLLCoverage> collCoverages;

    private List<FullCoverage> fullCoverages;
    
    private RRCoverage rentalCoverage;

    private AddressInfo addressInfo;

    private String vehicleState;

    public Vehicle() {
    }

    public Vehicle(String model) {
        this.model = model;
    }

    public Vehicle(Boolean included, String model, LocalDate purchasedDate, AddressInfo addressInfo) {
        this.included = included;
        this.model = model;
        this.purchasedDate = purchasedDate;
        this.addressInfo = addressInfo;
    }

    public Boolean getIncluded() {
        return included;
    }

    public void setIncluded(Boolean included) {
        this.included = included;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getModelYear() {
        return modelYear;
    }

    public void setModelYear(Integer modelYear) {
        this.modelYear = modelYear;
    }

    public BigDecimal getNewValue() {
        return newValue;
    }

    public void setNewValue(BigDecimal newValue) {
        this.newValue = newValue;
    }

    public BigDecimal getCostNew() {
        return costNew;
    }

    public void setCostNew(BigDecimal costNew) {
        this.costNew = costNew;
    }

    public Long getDeclaredAnnualMiles() {
        return declaredAnnualMiles;
    }

    public void setDeclaredAnnualMiles(Long declaredAnnualMiles) {
        this.declaredAnnualMiles = declaredAnnualMiles;
    }

    public Long getOdometerReading() {
        return odometerReading;
    }

    public void setOdometerReading(Long odometerReading) {
        this.odometerReading = odometerReading;
    }

    public Integer getNumDaysDrivenPerWeek() {
        return numDaysDrivenPerWeek;
    }

    public void setNumDaysDrivenPerWeek(Integer numDaysDrivenPerWeek) {
        this.numDaysDrivenPerWeek = numDaysDrivenPerWeek;
    }

    public LocalDate getPurchasedDate() {
        return purchasedDate;
    }

    public void setPurchasedDate(LocalDate purchasedDate) {
        this.purchasedDate = purchasedDate;
    }

    public AddressInfo getAddressInfo() {
        return addressInfo;
    }

    public void setAddressInfo(AddressInfo addressInfo) {
        this.addressInfo = addressInfo;
    }

    public List<COLLCoverage> getCollCoverages() {
        return collCoverages;
    }

    public void setCollCoverages(List<COLLCoverage> collCoverages) {
        this.collCoverages = collCoverages;
    }

    public List<FullCoverage> getFullCoverages() {
        return fullCoverages;
    }

    public void setRentalCoverage(RRCoverage rentalCoverage) {
        this.rentalCoverage = rentalCoverage;
    }
    
    public RRCoverage getRentalCoverage() {
        return rentalCoverage;
    }

    public void setFullCoverages(List<FullCoverage> fullCoverages) {
        this.fullCoverages = fullCoverages;
    }

    public List<AnubisCoverage> getAnubisCoverages() {
        return anubisCoverages;
    }

    public void setAnubisCoverages(List<AnubisCoverage> anubisCoverages) {
        this.anubisCoverages = anubisCoverages;
    }

    public List<LocalDate> getServiceHistory() {
        return serviceHistory;
    }

    public void setServiceHistory(List<LocalDate> serviceHistory) {
        this.serviceHistory = serviceHistory;
    }

    public String getVehicleState() {
        return vehicleState;
    }

    public void setVehicleState(String vehicleState) {
        this.vehicleState = vehicleState;
    }

}
