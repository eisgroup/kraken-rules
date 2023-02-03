package kraken.el.functions;

import java.util.Collection;
import java.util.Collections;

import kraken.el.functionregistry.*;
import kraken.testproduct.domain.CarCoverage;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.Vehicle;

public class TypeAnnotationTestFunctions implements FunctionLibrary {

    @ExpressionFunction("GetCarCoverage")
    @ReturnType("CarCoverage")
    public static CarCoverage getCarCoverage(@ParameterType("Policy") Policy policySummary) {
        return policySummary.getCoverage();
    }

    @ExpressionFunction("GetVehicleModel")
    @ReturnType("String")
    public static String getVehicleModel(@ParameterType("Vehicle") Vehicle vehicle) {
        return vehicle.getModel();
    }

    @ExpressionFunction("GetFirstElement")
    @ReturnType("String")
    public static String getFirstElement(@ParameterType("String[]") Collection<String> elements) {
        return elements.isEmpty() ? "" : elements.iterator().next();
    }

}

