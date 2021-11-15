package kraken.engine.namespace.domain.base;

import kraken.engine.namespace.domain.Id;

/**
 * @author psurinin
 */
class BLOBEntity extends Id {
    private String blobCd;

    public BLOBEntity(String blobCd) {
        this.blobCd = blobCd;
    }

    public String getBlobCd() {
        return blobCd;
    }

    public void setBlobCd(String blobCd) {
        this.blobCd = blobCd;
    }
}
