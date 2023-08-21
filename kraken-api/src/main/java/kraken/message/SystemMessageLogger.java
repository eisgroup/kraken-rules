/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.message;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kraken.message.SystemMessageBuilder.Message;

/**
 * Handles formatting and logging of system messages that include message codes.
 * Parameters can optionally have {@link Throwable} as a last item in array which will be
 * handled by extracted and passing as a throwable to {@link Logger}.
 *
 * @author Mindaugas Ulevicius
 */
public class SystemMessageLogger {

    private final Logger logger;

    public SystemMessageLogger(Logger logger) {
        this.logger = logger;
    }

    public void error(Message message, Object... parameters) {
        if(logger.isErrorEnabled()) {
            var p = new MessageParameters(parameters);
            var m = SystemMessageBuilder.create(message).parameters(p.parameters).build();
            logger.error(m.formatMessageWithCode(), p.throwable);
        }
    }

    public void warn(Message message, Object... parameters) {
        if(logger.isWarnEnabled()) {
            var p = new MessageParameters(parameters);
            var m = SystemMessageBuilder.create(message).parameters(p.parameters).build();
            logger.warn(m.formatMessageWithCode(), p.throwable);
        }
    }

    public void info(Message message, Object... parameters) {
        if(logger.isInfoEnabled()) {
            var p = new MessageParameters(parameters);
            var m = SystemMessageBuilder.create(message).parameters(p.parameters).build();
            logger.info(m.formatMessageWithCode(), p.throwable);
        }
    }

    public void debug(Message message, Object... parameters) {
        if(logger.isDebugEnabled()) {
            var p = new MessageParameters(parameters);
            var m = SystemMessageBuilder.create(message).parameters(p.parameters).build();
            logger.debug(m.formatMessageWithCode(), p.throwable);
        }
    }

    static class MessageParameters {

        private final Throwable throwable;

        private final Object[] parameters;

        public MessageParameters(Object[] parameters) {
            if(parameters.length > 0 && parameters[parameters.length - 1] instanceof Throwable) {
                this.throwable = (Throwable) parameters[parameters.length - 1];
                this.parameters = Arrays.copyOf(parameters, parameters.length - 1);
            } else {
                this.throwable = null;
                this.parameters = parameters;
            }
        }
    }

    public Logger getSl4jLogger() {
        return logger;
    }

    public static SystemMessageLogger getLogger(Class<?> klass) {
        return new SystemMessageLogger(LoggerFactory.getLogger(klass));
    }
}
