
package kraken.engine.namespace.domain.entity;

import kraken.engine.namespace.domain.base.AddressInfo;

public class TestAddressInfo extends AddressInfo {

    private String city;
    private String street;
    private String postalCode;
    private Boolean doNotSilicit;
    private String countryCd;

    public TestAddressInfo(String addressType, String city, String street, String postalCode, Boolean doNotSilicit, String countryCd) {
        super(addressType);
        this.city = city;
        this.street = street;
        this.postalCode = postalCode;
        this.doNotSilicit = doNotSilicit;
        this.countryCd = countryCd;
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

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Boolean getDoNotSilicit() {
        return doNotSilicit;
    }

    public void setDoNotSilicit(Boolean doNotSilicit) {
        this.doNotSilicit = doNotSilicit;
    }

    public String getCountryCd() {
        return countryCd;
    }

    public void setCountryCd(String countryCd) {
        this.countryCd = countryCd;
    }
}
