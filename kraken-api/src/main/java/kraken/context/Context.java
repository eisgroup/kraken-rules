package kraken.context;

/**
 * Contains keys for Kraken engine evaluation context.
 *
 * @author psurinin
 * @since 1.0.36
 */
public class Context {

    /**
     * Key in context, where value is dimension values by which
     * rules and entry points can be resolved.
     */
    public static String DIMENSIONS = "dimensions";

    /**
     * Key in context, where value is external data during rules evaluation.
     * This data is passed from external, for rules model, source.
     */
    public static String EXTERNAL_DATA = "external";

}
