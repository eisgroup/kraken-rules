package kraken.el.functions;

import kraken.el.functionregistry.*;

import java.util.Collection;
import java.util.List;

/**
 * Functions used to test dependency extraction for function return types.
 *
 * @author Tomas Dapkunas
 * @since 1.0.37
 */
public class ComplexTypeTestFunctions implements FunctionLibrary {

    @ExpressionFunction("GetAddress")
    @ReturnType("Address")
    public static Object getAddress(Object param) {
        return param;
    }

    @ExpressionFunction("GetAddresses")
    @ReturnType("Address[]")
    public static Collection<Object> getAddresses(Object param) {
        return List.of(param);
    }

}
