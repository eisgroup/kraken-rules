package kraken.engine.namespace.domain.entity;

import kraken.engine.namespace.domain.base.RootEntity;

import java.time.LocalDate;
import java.util.Collection;

public class TestPolicy extends RootEntity {
    private String riskStateCd;
    private String state;
    private String packageCd;
    private LocalDate effectiveDate;
    private Collection<TestRiskItem> riskItems;

    public TestPolicy(String riskStateCd, String state, String packageCd, LocalDate effectiveDate, Collection<TestRiskItem> riskItems) {
        this.riskStateCd = riskStateCd;
        this.state = state;
        this.packageCd = packageCd;
        this.effectiveDate = effectiveDate;
        this.riskItems = riskItems;
    }

    public String getRiskStateCd() {
        return riskStateCd;
    }

    public void setRiskStateCd(String riskStateCd) {
        this.riskStateCd = riskStateCd;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPackageCd() {
        return packageCd;
    }

    public void setPackageCd(String packageCd) {
        this.packageCd = packageCd;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Collection<TestRiskItem> getRiskItems() {
        return riskItems;
    }

    public void setRiskItems(Collection<TestRiskItem> riskItems) {
        this.riskItems = riskItems;
    }
}
