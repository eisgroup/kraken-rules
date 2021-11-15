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
package kraken.model.project.exception;

import kraken.annotations.API;
import kraken.exception.KrakenException;
import kraken.model.project.KrakenProject;

/**
 * {@link KrakenProject} has invalid state which indicates that it was constructed incorrectly.
 *
 * @author mulevicius
 */
@API
public class IllegalKrakenProjectStateException extends KrakenException {

    public IllegalKrakenProjectStateException(String message) {
        super(message);
    }

    public IllegalKrakenProjectStateException(String message, Throwable cause) {
        super(message, cause);
    }

}
