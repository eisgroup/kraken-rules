/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package kraken.el.ast;

import kraken.el.ast.token.Token;
import kraken.el.scope.Scope;
import kraken.el.scope.type.Type;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mulevicius
 */
public class Function extends Reference {

    private String functionName;

    private List<Expression> parameters;

    public Function(String functionName, List<Expression> parameters, Scope scope, Type evaluationType, Token token) {
        super(NodeType.FUNCTION, scope, evaluationType, token);

        this.functionName = functionName;
        this.parameters = parameters;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<Expression> getParameters() {
        return parameters;
    }

    public String getFunctionSignatureString() {
        return parameters.stream()
                .map(Expression::getEvaluationType)
                .map(Type::toString)
                .collect(Collectors.joining(", ", functionName + "(", ")"));
    }

    @Override
    String getFirstToken() {
        return functionName + "()";
    }

    @Override
    public String toString() {
        return parameters.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ", functionName + "(", ")"));
    }
}
