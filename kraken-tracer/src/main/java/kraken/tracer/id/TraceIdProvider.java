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
package kraken.tracer.id;

/**
 * Responsible for generating and providing trace ids. By default, a random UUID is generated each time as a trace id.
 * This could be implemented when random trace id is undesirable, for example when testing tracer output.
 * <p/>
 * Implementations of {@link TraceIdProvider} will be loaded using {@link java.util.ServiceLoader} mechanism
 * and must be registered in the system as described in {@link java.util.ServiceLoader} documentation.
 *
 * @author mulevicius
 */
public interface TraceIdProvider {

    String nextId();
}
