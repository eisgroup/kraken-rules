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
package kraken.tracer;

/**
 * An SPI for implementing void trace operation. A void operation is a specific subtype
 * of {@code Operation} which should be used when there is no result to describe
 * or result description is not needed.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public interface VoidOperation extends Operation<Void> {

}
