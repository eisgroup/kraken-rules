package kraken.engine.namespace.domain.entity;

import kraken.engine.namespace.domain.Id;

import java.math.BigDecimal;
import java.util.Collection;

public class TestRiskItem extends Id {

    private String itemName;
    private Boolean defaultItemName;
    private BigDecimal value;
    private TestAddressInfo addressInfo;
    private Collection<TestCoverage>  coverages;

    public TestRiskItem(String itemName, Boolean defaultItemName, BigDecimal value, TestAddressInfo addressInfo, Collection<TestCoverage> coverages) {
        this.itemName = itemName;
        this.defaultItemName = defaultItemName;
        this.value = value;
        this.addressInfo = addressInfo;
        this.coverages = coverages;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Boolean getDefaultItemName() {
        return defaultItemName;
    }

    public void setDefaultItemName(Boolean defaultItemName) {
        this.defaultItemName = defaultItemName;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public TestAddressInfo getAddressInfo() {
        return addressInfo;
    }

    public void setAddressInfo(TestAddressInfo addressInfo) {
        this.addressInfo = addressInfo;
    }

    public Collection<TestCoverage> getCoverages() {
        return coverages;
    }

    public void setCoverages(Collection<TestCoverage> coverages) {
        this.coverages = coverages;
    }
}


