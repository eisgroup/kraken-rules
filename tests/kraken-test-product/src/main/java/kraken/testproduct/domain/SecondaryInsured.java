package kraken.testproduct.domain;

import kraken.testproduct.domain.meta.Identifiable;

/**
 * @author psurinin
 */
public class SecondaryInsured extends Identifiable {

    private String name;

    public SecondaryInsured(String name, String id) {
        this.name = name;
        this.setId(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
