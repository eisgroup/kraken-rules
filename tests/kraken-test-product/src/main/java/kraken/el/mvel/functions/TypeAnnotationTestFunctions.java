package kraken.el.mvel.functions;

import kraken.el.functionregistry.*;
import kraken.testproduct.domain.CarCoverage;
import kraken.testproduct.domain.Policy;

public class TypeAnnotationTestFunctions implements FunctionLibrary {

    @ExpressionFunction("GetCarCoverage")
    @ReturnType("CarCoverage")
    public static CarCoverage getCarCoverage(@ParameterType("Policy") Policy policySummary) {
        return policySummary.getCoverage();
    }

}

