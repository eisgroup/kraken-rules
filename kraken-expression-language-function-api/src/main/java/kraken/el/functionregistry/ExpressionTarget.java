/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
package kraken.el.functionregistry;

import kraken.annotations.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Assigns Expression Function to one or more Expression Target available in the system.
 * If Expression Function is not assigned to any specific Expression Target,
 * then it will be available for every Expression Target.
 * <p/>
 * Expression Target limits function availability only to certain Kraken Expression Language environments.
 * <p/>
 * Please consult documentation for a list of all available environments.
 *
 * @author mulevicius
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@API
public @interface ExpressionTarget {

    /**
     * @return an array of Expression Target names
     */
    String[] value();

}
