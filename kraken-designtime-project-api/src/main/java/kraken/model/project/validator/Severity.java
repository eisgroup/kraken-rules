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
package kraken.model.project.validator;

import kraken.annotations.API;

/**
 * Indicates severity of {@link ValidationMessage}.
 * <p/>
 * INFO will be logged as informative messages.
 * <p/>
 * WARNING is used for warnings that are not critical.
 * <p/>
 * ERROR is used for severe errors and {@link kraken.model.project.exception.KrakenProjectValidationException}
 * will be thrown in the system if at least one validation ERROR is found in the {@link kraken.model.project.KrakenProject}
 */
@API
public enum Severity {
    ERROR, WARNING, INFO
}
