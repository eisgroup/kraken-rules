package kraken.engine.namespace.domain.base;

import kraken.engine.namespace.domain.Id;

/**
 * @author psurinin
 */
class LOBEntity extends Id {
    private String lobCd;

    public LOBEntity(String lobCd) {
        this.lobCd = lobCd;
    }

    public String getLobCd() {
        return lobCd;
    }

    public void setLobCd(String lobCd) {
        this.lobCd = lobCd;
    }
}
