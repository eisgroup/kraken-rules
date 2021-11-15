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
package kraken.el.functionregistry;

import kraken.annotations.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks Expression Function parameter as either iterable or not iterable.
 * By default, parameter whose type is singular (not collection) is iterable.
 * <p/>
 * When Expression Function has at least one iterable parameter
 * and function is invoked with collection as a value of iterable parameter,
 * then Kraken Expression Language will invoke Expression Function multiple times for each item in the collection.
 * Result of such invocation will itself be a collection of results of each invocation.
 *
 * @author mulevicius
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@API
public @interface Iterable {

     /**
      * @return true if Expression Function parameter is iterable, false otherwise
      */
     boolean value() default true;
}
