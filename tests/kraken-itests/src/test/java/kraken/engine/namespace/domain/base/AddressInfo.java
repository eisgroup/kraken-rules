package kraken.engine.namespace.domain.base;

import kraken.engine.namespace.domain.Id;

/**
 * @author psurinin
 */
public class AddressInfo extends Id {
    private String addressType;

    protected AddressInfo(String addressType) {
        this.addressType = addressType;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }
}
