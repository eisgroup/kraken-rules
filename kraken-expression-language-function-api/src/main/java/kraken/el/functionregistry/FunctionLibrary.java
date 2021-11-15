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

import kraken.annotations.SPI;

/**
 * Marker interface used to register Function Library for Kraken Expression Language.
 * Function Library is a collection of Expression Functions that can be invoked in expression.
 * Expression Function is a static Java method marked with {@link ExpressionFunction}.
 * <p/>
 * Expression Function can have one or more Expression Target assigned.
 * Expression Target determines Kraken Expression Language environment in which this function can be invoked.
 * Expression Target can be assigned by annotating Function Library or Expression Function with {@link ExpressionTarget}.
 * If Expression Function is not assigned to any specific Expression Target,
 * then it will be available for every Expression Target.
 * <p/>
 * Implementations of {@link FunctionLibrary} will be loaded using {@link java.util.ServiceLoader} mechanism
 * and must be registered in the system as described in {@link java.util.ServiceLoader} documentation.
 *
 * @author mulevicius
 */
@SPI
public interface FunctionLibrary {

}
