package kraken.engine.namespace.domain.entity;

import kraken.engine.namespace.domain.Id;

import javax.money.MonetaryAmount;

public class TestCoverage extends Id {

    private MonetaryAmount deductibleAmount;
    private MonetaryAmount limitAmount;

    public TestCoverage(MonetaryAmount deductibleAmount, MonetaryAmount limitAmount) {
        this.deductibleAmount = deductibleAmount;
        this.limitAmount = limitAmount;
    }

    public MonetaryAmount getDeductibleAmount() {
        return deductibleAmount;
    }

    public void setDeductibleAmount(MonetaryAmount deductibleAmount) {
        this.deductibleAmount = deductibleAmount;
    }

    public MonetaryAmount getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(MonetaryAmount limitAmount) {
        this.limitAmount = limitAmount;
    }
}
