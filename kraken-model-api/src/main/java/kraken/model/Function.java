/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.model;

import java.util.List;

import kraken.annotations.API;

/**
 * Models function implementation that can be used in rule expressions.
 * Function implementation consists of a function name, return type, a list of parameters and a function body.
 * <p/>
 * Function name must be unique in scope of kraken project. There cannot be another function with the same name
 * defined as function implementation or imported as a function signature.
 * <p/>
 * Function body is a KEL expression which can use function parameters by name and must return a value.
 * <p/>
 * Function return type is optional. If not provided then return type is determined implicitly from function body.
 * <p/>
 * Function return type or parameter type is specified as a type token in a certain format.
 * Supported token formats are:
 *       <ul>
 *           <li>native types - Any, Number, Money, Boolean, String, Date, DateTime </li>
 *           <li>registered type - EntityType</li>
 *           <li>array - Type[]</li>
 *           <li>union - Type1 | Type2</li>
 *           <li>generic - &lt;T&gt;</li>
 *       </ul>
 *
 * @author mulevicius
 */
@API
public interface Function extends KrakenModelItem {

    String getReturnType();

    void setReturnType(String returnType);

    List<FunctionParameter> getParameters();

    void setParameters(List<FunctionParameter> parameters);

    List<GenericTypeBound> getGenericTypeBounds();

    void setGenericTypeBounds(List<GenericTypeBound> genericTypeBounds);

    Expression getBody();

    void setBody(Expression expression);

    FunctionDocumentation getDocumentation();

    void setDocumentation(FunctionDocumentation documentation);

}
