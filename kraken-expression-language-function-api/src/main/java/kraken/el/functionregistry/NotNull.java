package kraken.el.functionregistry;

import kraken.annotations.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that Expression Function parameter cannot be null.
 * Kraken Expression Language will take care when invoking such functions
 * and will automatically throw an exception if parameter value is null before invoking function implementation.
 * This allows function implementation to assume that the parameter will never be null.
 * How thrown exception is handled is engine specific.
 *
 * @author mulevicius
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@API
public @interface NotNull {
}
