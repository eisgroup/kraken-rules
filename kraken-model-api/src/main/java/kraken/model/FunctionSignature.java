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
package kraken.model;

import java.util.List;

import kraken.annotations.API;

/**
 * Models a signature of a custom function that can be used in Rule expressions.
 * A function signature consists of a function name, function return type and a list of parameter types.
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
public interface FunctionSignature extends KrakenModelItem {

    String getReturnType();

    void setReturnType(String returnType);

    List<String> getParameterTypes();

    void setParameterTypes(List<String> parameterTypes);

    /**
     * @param f
     * @return function signature formatted into human-readable representation equivalent to DSL syntax to be used for
     *         messages
     */
    static String format(FunctionSignature f) {
        return f.getName() + "(" + String.join(", ", f.getParameterTypes()) + ") : " + f.getReturnType();
    }
}
