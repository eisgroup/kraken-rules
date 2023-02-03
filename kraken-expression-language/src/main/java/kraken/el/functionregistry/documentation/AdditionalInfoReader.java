/*
 *  Copyright © 2019 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 *  CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.
 *
 */

package kraken.el.functionregistry.documentation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;

import kraken.el.functionregistry.FunctionHeader;

public class AdditionalInfoReader {

    public static String read(FunctionHeader functionHeader) {
        String resourceName = String
            .format("docs/%s(%s).md", functionHeader.getName(), functionHeader.getParameterCount());

        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);

            if (url != null) {
                // A workaround for potential concurrency issue in Java. Disable JVM built-in
                // URL caching to avoid issues when resources loaded as a stream are closed by
                // JAR file being closed elsewhere.
                URLConnection urlConnection = url.openConnection();
                urlConnection.setUseCaches(false);

                try (InputStream inputStream = urlConnection.getInputStream()) {
                    if (inputStream != null) {
                        List<String> lines = IOUtils.readLines(inputStream, Charset.defaultCharset());
                        return String.join(System.lineSeparator(), lines);
                    }
                }
            }

            return null;
        } catch (IOException e) {
            throw new IllegalArgumentException("Error while reading function documentation from: " + resourceName, e);
        }
    }
}
