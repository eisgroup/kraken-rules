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

/**
 * @author mulevicius
 */
public enum NodeType {
    ADDITION("Addition", "+"),
    SUBTRACTION("Subtraction", "-"),
    MULTIPLICATION("Multiplication", "*"),
    DIVISION("Division", "/"),
    MODULUS("Modulus", "%"),
    EXPONENT("Exponent", "**"),

    AND("And", "&&"),
    OR("Or", "||"),
    EQUALS("Equals", "=="),
    NOT_EQUALS("NotEquals", "!="),
    MORE_THAN("MoreThan", ">"),
    MORE_THAN_OR_EQUALS("MoreThanOrEquals", ">="),
    LESS_THAN("LessThan", "<"),
    LESS_THAN_OR_EQUALS("LessThanOrEquals", "<="),
    IN("In", "in"),
    MATCHES_REG_EXP("MatchesRegExp", "matches"),

    NEGATION("Negation", "!"),
    NEGATIVE("Negative", "-"),

    TYPE("Type"),
    STRING("String"),
    BOOLEAN("Boolean"),
    DECIMAL("Decimal"),
    DATE("Date"),
    DATETIME("Datetime"),
    NULL("Null"),

    INLINE_MAP("InlineMap"),
    INLINE_ARRAY("InlineArray"),

    REFERENCE("Reference"),
    THIS("This"),
    IDENTIFIER("Identifier"),
    FUNCTION("Function"),
    IF("If"),

    PATH("Path"),
    ACCESS_BY_INDEX("AccessByIndex"),
    COLLECTION_FILTER("CollectionFilter"),
    SOME("Some"),
    EVERY("Every"),
    FOR("For"),

    INSTANCEOF("InstanceOf", "instanceof"),
    TYPEOF("TypeOf", "typeof"),
    CAST("Cast"),

    TEMPLATE("Template");

    private String name;

    private String operator;

    NodeType(String name) {
        this.name = name;
    }

    NodeType(String name, String operator) {
        this.name = name;
        this.operator = operator;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getOperator() {
        if(operator == null) {
            throw new UnsupportedOperationException("Operator does not exist for: " + name);
        }
        return operator;
    }

}
