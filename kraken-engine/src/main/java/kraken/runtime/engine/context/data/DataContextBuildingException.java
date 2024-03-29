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
package kraken.runtime.engine.context.data;

import kraken.message.SystemMessage;
import kraken.runtime.KrakenRuntimeException;

/**
 * Indicates abnormal situation during building context instance
 *
 * @author rimas
 * @since 1.0
 */
public class DataContextBuildingException extends KrakenRuntimeException {

    private static final long serialVersionUID = 6809328671826957607L;

    /**
     * Constructor to build exception with message
     *
     * @param message   message text
     */
    public DataContextBuildingException(SystemMessage message) {
        super(message);
    }

    /**
     * Constructor to build exception instance with message and cause
     *
     * @param message   message text
     * @param cause     cause exception
     */
    public DataContextBuildingException(SystemMessage message, Throwable cause) {
        super(message, cause);
    }
}
