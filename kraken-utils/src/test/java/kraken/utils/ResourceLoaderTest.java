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
package kraken.utils;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ResourceLoaderTest {

    @Test
    public void shouldExcludeFiles() {
        var loader = ResourceLoader.builder()
                .pattern(".*\\.test")
                .excludePattern("**url**")
                .excludePattern("**huuuu**")
                .excludePattern("**uuuuh**")
                .build();
        var result = loader.load();
        assertThat(result, hasSize(4));
    }

    @Test
    public void shouldLoadFromRoot() {
        var loader = ResourceLoader.builder()
                .pattern("root.test")
                .build();

        var result = loader.load();

        assertThat(result, hasSize(1));
        assertThat(result, contains(content("baz")));
    }

    @Test
    public void shouldLoadFromNested() {
        var loader = ResourceLoader.builder()
                .baseDir("nested/")
                .pattern("nested-1.test")
                .build();

        var result = loader.load();

        assertThat(result, hasSize(1));
        assertThat(result, contains(content("foo")));
    }

    @Test
    public void shouldLoadByPattern() {
        var loader = ResourceLoader.builder()
                .baseDir("nested/")
                .pattern("nested-.*\\.test")
                .build();

        var result = loader.load();

        assertThat(result, hasSize(3));
        assertThat(result, containsInAnyOrder(content("foo"),
                                              content("bar"),
                                              content("deep")));

    }

    @Test
    public void shouldLoadDeepPatterns() {
        var loader = ResourceLoader.builder()
                .baseDir("nested/very/very/deep")
                .pattern("nested-.*\\.test")
                .build();

        var result = loader.load();

        assertThat(result, hasSize(1));
        assertThat(result, contains(content("deep")));
    }

    @Test
    public void shouldResolveFromURLClassLoader() {
        var classLoader = new URLClassLoader(new URL[]{Thread.currentThread().getContextClassLoader().getResource("url/")});
        var loader = ResourceLoader.builder()
                .baseDir("url/")
                .pattern("urlRoot.test")
                .classLoader(classLoader)
                .build();

        var result = loader.load();

        assertThat(result, hasSize(1));
        assertThat(result, contains(content("urlRoot")));
    }

    @Test
    public void shouldResolveNestedFromURLClassLoader() {
        var classLoader = new URLClassLoader(new URL[]{Thread.currentThread().getContextClassLoader().getResource("url/")});
        var loader = ResourceLoader.builder()
                .baseDir("urlNested/")
                .pattern(".*\\.test")
                .classLoader(classLoader)
                .build();

        var result = loader.load();

        assertThat(result, hasSize(1));
        assertThat(result, contains(content("urlNested")));
    }

    @Test
    public void shouldLoadFromZipNested() throws IOException {
        var classLoader = new URLClassLoader(new URL[]{createTestZip()});

        var loader = ResourceLoader.builder()
                .baseDir("zipNested/")
                .pattern(".*\\.test")
                .classLoader(classLoader)
                .build();

        var result = loader.load();

        assertThat(result, hasSize(1));
        assertThat(result, contains(content("zipNested")));
    }

    @Test
    public void shouldLoadFromZipRoot() throws IOException {
        var classLoader = new URLClassLoader(new URL[]{createTestZip()});

        var loader = ResourceLoader.builder()
                .pattern("zipRoot.test")
                .classLoader(classLoader)
                .build();

        var result = loader.load();

        assertThat(result, hasSize(1));
        assertThat(result, contains(content("zipRoot")));
    }

    private URL createTestZip() throws IOException {
        File tempZip = File.createTempFile("test", ".jar");
        tempZip.deleteOnExit();

        try (ZipOutputStream os = new ZipOutputStream(new FileOutputStream(tempZip))) {
            ZipEntry root = new ZipEntry("zipRoot.test");
            os.putNextEntry(root);
            os.write("zipRoot".getBytes());
            os.closeEntry();

            ZipEntry nested = new ZipEntry("zipNested/zipNested-1.test");
            os.putNextEntry(nested);
            os.write("zipNested".getBytes());
            os.closeEntry();
        }

        return tempZip.toURI().toURL();
    }

    private Matcher<URL> content(String content) {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(URL url) {
                try (InputStream is = url.openStream()) {
                    var scanner = new Scanner(is).useDelimiter("\\A");
                    return scanner.next().equals(content);
                } catch (IOException e) {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Content ")
                        .appendValue(content);
            }
        };
    }

}