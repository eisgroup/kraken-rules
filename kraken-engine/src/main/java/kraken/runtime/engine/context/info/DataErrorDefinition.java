package kraken.runtime.engine.context.info;

/**
 * Class to hold data object validation error info, that will be returned from {@link DataObjectInfoResolver}
 *
 * @author psurinin@eisgroup.com
 * @since 10.13
 */
public class DataErrorDefinition {

    private final String message;

    public DataErrorDefinition(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
