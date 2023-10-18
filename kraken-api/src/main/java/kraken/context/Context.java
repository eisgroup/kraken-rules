package kraken.context;

import kraken.annotations.API;

/**
 * Contains keys for Kraken engine evaluation context.
 *
 * @author psurinin
 * @since 1.0.36
 */
@API
public class Context {

    /**
     * Key in context, where value is dimension values by which
     * rules and entry points can be resolved.
     */
    public static final String DIMENSIONS = "dimensions";

    /**
     * Key in context, where value is external data during rules evaluation.
     * This data is passed from external, for rules model, source.
     */
    public static final String EXTERNAL_DATA = "external";

    /**
     * Key in dimensions map, where value is an instance of {@link java.time.ZoneId} for rule evaluation.
     */
    public static final String RULE_TIMEZONE_ID_DIMENSION = "__ruleTimezoneId__";

}
