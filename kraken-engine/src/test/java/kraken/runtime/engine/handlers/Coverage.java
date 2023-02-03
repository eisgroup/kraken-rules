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
package kraken.runtime.engine.handlers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import javax.money.MonetaryAmount;

public class Coverage {
    private String code;
    private int level;
    private BigDecimal decimalLimit;
    private MonetaryAmount moneyLimit;
    private LocalDate localDate;
    private LocalDateTime localDateTime;
    private List<String> labels;
    private Collection<Object> conditions;
    private Object address;

    public Coverage(Collection<Object> conditions) {
        this.conditions = conditions;
    }

    public Coverage() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public BigDecimal getDecimalLimit() {
        return decimalLimit;
    }

    public void setDecimalLimit(BigDecimal decimalLimit) {
        this.decimalLimit = decimalLimit;
    }

    public MonetaryAmount getMoneyLimit() {
        return moneyLimit;
    }

    public void setMoneyLimit(MonetaryAmount moneyLimit) {
        this.moneyLimit = moneyLimit;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public Object getAddress() {
        return address;
    }

    public void setAddress(Object address) {
        this.address = address;
    }

    public Collection<Object> getConditions() {
        return conditions;
    }

    public void setConditions(Collection<Object> conditions) {
        this.conditions = conditions;
    }
}
