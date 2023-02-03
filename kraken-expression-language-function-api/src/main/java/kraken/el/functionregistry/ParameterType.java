package kraken.el.functionregistry;

import kraken.annotations.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies type of Expression Function parameter.
 * <p/>
 * If parameter is annotated with {@link ParameterType} then type of the parameter will be resolved from annotation.
 * <p/>
 * Annotation should be used in exceptional cases when default parameter type resolution by using Class name is not possible.
 * See {@link ExpressionFunction} on how type is resolved from Class name when {@link ParameterType} is not specified.
 * <p/>
 * Type is specified as a string value in specific format. Must be one of:
 * <ul>
 *     <li>Native type - Any, String, Number, Money, Date, DateTime, Boolean</li>
 *     <li>Entity type - name of entity defined in domain model or a name of base type</li>
 *     <li>
 *         Array type - any type with [] suffix, for example:
 *         <ul>
 *             <li>String[]</li>
 *             <li>Coverage[]</li>
 *             <li>&lt;T&gt;[]</li>
 *         </ul>
 *     </li>
 *     <li>
 *         Generic type - letter between &lt;&gt; symbols, for example: &lt;T&gt;.
 *         Generic type is useful when parameter type is not known (generic), but related with the output of the function,
 *         for example: <p>
 *         {@code public static @ReturnType("<T>") Object GetFirst(@ParameterType("<T>[]") Collection items)}
 *     </li>
 *     <li>
 *         Union type - two types around | symbol, for example:
 *         <ul>
 *             <li>
 *                 Date | DateTime - useful when implementing function that supports both
 *                 {@link java.time.LocalDate} and {@link java.time.LocalDateTime}.
 *             </li>
 *             <li>
 *                 Coverage | Coverage[] - useful when implementing function that supports both
 *                 a single entity and a collection of entities.
 *             </li>
 *         </ul>
 *     </li>
 * </ul>
 * @author Tomas Dapkunas
 * @since 1.0.37
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@API
public @interface ParameterType {

    /**
     * @return type of function parameter
     */
    String value();

}
