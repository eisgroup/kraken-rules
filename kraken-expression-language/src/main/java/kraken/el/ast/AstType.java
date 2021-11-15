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

/**
 * Indicates type of AST that was parsed from expression
 *
 * @author mulevicius
 */
public enum AstType {

    /**
     * Indicates that expression was a simple literal, like string, number or boolean
     */
    LITERAL,

    /**
     * Indicates that expression is a name of simple bean property
     */
    PROPERTY,

    /**
     * Indicates that expression is a bean path that consists of more than one identifier separated by dot
     */
    PATH,

    /**
     * Indicates that expression is more complex that any other simple types or that type cannot be determined
     */
    COMPLEX
}
