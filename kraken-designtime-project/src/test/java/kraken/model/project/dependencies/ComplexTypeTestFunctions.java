package kraken.model.project.dependencies;

import java.util.Collection;
import java.util.List;

import kraken.el.functionregistry.ExpressionFunction;
import kraken.el.functionregistry.FunctionLibrary;
import kraken.el.functionregistry.ReturnType;

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
