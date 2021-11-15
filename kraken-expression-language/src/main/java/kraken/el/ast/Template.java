/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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

import java.util.List;

import kraken.el.ast.token.Token;
import kraken.el.scope.Scope;
import kraken.el.scope.type.Type;

/**
 * @author mulevicius
 */
public class Template extends Expression {

    private final List<String> templateParts;

    private final List<Expression> templateExpressions;

    public Template(List<String> templateParts, List<Expression> templateExpressions, Scope scope, Token token) {
        super(NodeType.TEMPLATE, scope, Type.STRING, token);

        this.templateParts = templateParts;
        this.templateExpressions = templateExpressions;
    }

    public List<String> getTemplateParts() {
        return templateParts;
    }

    public List<Expression> getTemplateExpressions() {
        return templateExpressions;
    }

    @Override
    public String toString() {
        StringBuilder expressionBuilder = new StringBuilder();
        for(int i = 0; i < templateParts.size(); i++) {
            expressionBuilder.append(templateParts.get(i));
            if(i < templateExpressions.size()) {
                expressionBuilder.append("${").append(templateExpressions.get(i)).append("}");
            }
        }
        return asTemplateExpression(expressionBuilder.toString());
    }

    public static String asTemplateExpression(String message) {
        return "`" + message.replace("`", "\\`") + "`";
    }

}
