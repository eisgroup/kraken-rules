/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Documentation link appender, that can append additional details to the message
 * based on whether field is annotated with {@link Documented}.
 *
 * <p>Usage example:
 * <blockquote>
 * <pre>{@code
 *     public class MyMessageBuilder {
 *
 *         public enum MyMessage {
 *             @Documented
 *             MY_MESSAGE("errorCode", "message");
 *             ...
 *         }
 *
 *         public Message build() {
 *             ...
 *             return new Message(code, DocumentationAppender.append(myMessage, originalMessage));
 *         }
 *     }
 * }</pre>
 * </blockquote>
 *
 * <p>Documentation can be disabled by setting {@link #DISABLE_DOCUMENTATION_PROPERTY}
 * system property to false, for example in application properties:
 * <blockquote>
 * <pre>{@code
 *     kraken.message.documentation.disable=true
 * }</pre>
 * </blockquote>
 * @author Tomas Dapkunas
 * @since 1.54.0
 */
public final class DocumentationAppender {

    private static final String MESSAGE_DOCUMENTATION_PROPERTIES_FILE = "kraken-message-documentation.properties";
    private static final String DISABLE_DOCUMENTATION_PROPERTY = "kraken.message.documentation.disable";
    private static final String DOCUMENTATION_LINK_PROPERTY = "kraken.message.documentation.link";
    private static final String DOCUMENTATION_LINK_TEMPLATE = "For more details and possible solutions visit: %s";

    private static final Properties PROPERTIES;
    private static final Boolean IS_DISABLED;

    static {
        PROPERTIES = new Properties();
        IS_DISABLED = Boolean.parseBoolean(System.getProperty(DISABLE_DOCUMENTATION_PROPERTY));

        try (InputStream is
            = SystemMessageBuilder.class.getClassLoader().getResourceAsStream(MESSAGE_DOCUMENTATION_PROPERTIES_FILE)) {
            if (is != null) {
                PROPERTIES.load(is);
            }
        } catch (IOException e) {
            // Ignore
        }
    }

    /**
     * Appends additional documentation link to original message if given enum instance
     * if documentable.
     *
     * @param enumInstance    Enum instance.
     * @param originalMessage Original message to append link to.
     * @return Either original or modified message.
     */
    public static String append(Enum<?> enumInstance, String originalMessage) {
        if (!IS_DISABLED && isDocumented(enumInstance)) {
            String documentedAt = PROPERTIES.getProperty(DOCUMENTATION_LINK_PROPERTY);

            if (documentedAt != null) {
                originalMessage = originalMessage
                    .concat(System.lineSeparator())
                    .concat(String.format(DOCUMENTATION_LINK_TEMPLATE, documentedAt));
            }
        }

        return originalMessage;
    }

    private static boolean isDocumented(Enum<?> enumInstance) {
        try {
            return enumInstance.getClass()
                .getField(enumInstance.name())
                .getAnnotation(Documented.class) != null;
        } catch (NoSuchFieldException e) {
            // Ignore, return false
            return false;
        }
    }

}
