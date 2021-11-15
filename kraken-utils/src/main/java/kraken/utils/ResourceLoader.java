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

import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.cache.DefaultFilesCache;
import org.apache.commons.vfs2.impl.DefaultFileReplicator;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.jar.JarFileProvider;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs2.provider.url.UrlFileProvider;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A service that loads resources from classpath. Searches all classpath jars.
 * Allows to define the base resource directory to search in and additional regex patterns to apply on the filenames.
 *
 * @author gvisokinskas
 */
public final class ResourceLoader {
    private static final FileSystemManager MANAGER = createManager();

    private final String baseDir;
    private final Pattern filePattern;
    private final Collection<URL> searchPath;
    private final Predicate<URL> canInclude;

    private ResourceLoader(String baseDir, Pattern filePattern, Collection<URL> searchPath, Predicate<URL> canInclude) {
        this.baseDir = baseDir.isBlank() ? "." : baseDir;
        this.filePattern = filePattern;
        this.searchPath = searchPath;
        this.canInclude = canInclude;
    }

    /**
     * @return a builder for creating instances of {@link ResourceLoader}.
     */
    public static Builder builder() {
        return new Builder();
    }

    private static DefaultFileSystemManager createManager() {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();
        try {
            manager.setFilesCache(new DefaultFilesCache());
            manager.setCacheStrategy(CacheStrategy.ON_RESOLVE);

            manager.addProvider("jar", new JarFileProvider());
            manager.addProvider("file", new DefaultLocalFileProvider());
            manager.addExtensionMap("jar", "jar");

            manager.setDefaultProvider(new UrlFileProvider());

            File tempDir = Files.createTempDirectory("kraken-vfs-cache").toFile();
            tempDir.deleteOnExit();

            DefaultFileReplicator defaultReplicator = new DefaultFileReplicator(tempDir);
            manager.setTemporaryFileStore(defaultReplicator);
            manager.setReplicator(defaultReplicator);

            manager.init();
            return manager;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Loads the resources matching the pattern from the classpath.
     *
     * @return a collection of the loaded resource URLs.
     */
    public Collection<URL> load() {
        return searchPath.stream()
                .flatMap(this::search)
                .filter(canInclude)
                .collect(Collectors.toList());
    }

    private Stream<URL> search(URL url) {
        try {
            var root = MANAGER.resolveFile(externalize(url));

            if (MANAGER.canCreateFileSystem(root)) {
                root = MANAGER.createFileSystem(root);
            }

            return find(root);
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Stream<URL> find(FileObject root) throws FileSystemException {
        try {
            FileObject fileObject = root.resolveFile(baseDir);

            if (exists(fileObject)) {
                return findFiles(fileObject).map(this::toUrl);
            }

            return Stream.empty();
        } finally {
            if (Objects.nonNull(root.getFileSystem().getParentLayer())) {  // check if layered filesystem (Zip, Jar)
                MANAGER.closeFileSystem(root.getFileSystem()); // safely close it, it will be recreated
            }
        }
    }

    private URL toUrl(FileObject fileObject) {
        try {
            return fileObject.getURL();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Stream<FileObject> findFiles(FileObject directory) {
        try {
            return Arrays.stream(directory.findFiles(new SimpleNameSelector()));
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String externalize(URL url) {
        // https://github.com/spring-projects/spring-boot/issues/7096
        String path = url.toExternalForm();
        if (path.endsWith("!/")) {
            path = path.substring(0, path.lastIndexOf("!/")) + "/";
        }
        return path;
    }

    private boolean exists(FileObject fileObject) {
        try {
            return fileObject != null && fileObject.exists();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * A builder that should be used in order to construct an instance of {@link ResourceLoader}.
     */
    public static final class Builder {
        private Collection<String> excludePatterns;
        private String baseDir = "";
        private String pattern;
        private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        private Builder() {
            excludePatterns = new ArrayList<>();
        }

        /**
         * Sets the pattern to ignore loaded files
         *
         * @param excludePattern for folders or text in extracted from classpath URL.
         *                        it can be **ignoredToken** or **\/ignoredFolder\/**
         * @return this builder.
         */
        public Builder excludePattern(String excludePattern) {
            this.excludePatterns.add(excludePattern);
            return this;
        }

        /**
         * Sets the classloader to use for resolving the paths.
         *
         * @param classLoader the classloader to set.
         * @return this builder.
         */
        public Builder classLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        /**
         * Sets the provided base directory to search in the classpath.
         *
         * @param baseDir the base directory to search in.
         * @return this builder.
         */
        public Builder baseDir(String baseDir) {
            this.baseDir = baseDir;
            return this;
        }

        /**
         * Sets the pattern to apply on the resources residing in the classpath.
         *
         * @param pattern a valid regex pattern to apply.
         * @return this builder.
         */
        public Builder pattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        /**
         * Creates an instance of the {@link ResourceLoader}.
         *
         * @return an instance of the created {@link ResourceLoader}.
         */
        public ResourceLoader build() {

            Predicate<URL> canInclude = url -> true;
            if (!excludePatterns.isEmpty()) {
                canInclude = url -> excludePatterns.stream().noneMatch(
                        p -> Pattern
                                .compile(p.replace("**", ".*"))
                                .asMatchPredicate()
                                .test(url.getPath())
                );
            }
            return new ResourceLoader(
                    baseDir,
                    Pattern.compile(this.pattern),
                    getSearchPath(),
                    canInclude
            );
        }

        private Collection<URL> getSearchPath() {
            return Stream.of(fromGenericClassLoader(),
                             fromUrlClassLoader(classLoader),
                             fromProperty())
                    .flatMap(Function.identity())
                    .distinct()
                    .collect(Collectors.toList());
        }

        private Stream<URL> fromGenericClassLoader() {
            try {
                var resources = classLoader.getResources("").asIterator();
                var spliterator = Spliterators.spliteratorUnknownSize(resources, Spliterator.ORDERED);
                return StreamSupport.stream(spliterator, false);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private Stream<URL> fromUrlClassLoader(ClassLoader classLoader) {
            var currentUrls = Optional.ofNullable(classLoader)
                    .filter(URLClassLoader.class::isInstance)
                    .map(URLClassLoader.class::cast)
                    .stream()
                    .flatMap(urlLoader -> Arrays.stream(urlLoader.getURLs()));

            var parentUrls = Optional.ofNullable(classLoader)
                    .map(ClassLoader::getParent)
                    .stream()
                    .flatMap(this::fromUrlClassLoader);

            return Stream.concat(currentUrls, parentUrls);
        }

        private Stream<URL> fromProperty() {
            var cpEntries = System.getProperty("java.class.path")
                    .split(File.pathSeparator);

            return Arrays.stream(cpEntries)
                    .map(Paths::get)
                    .map(name -> {
                        try {
                            return name.toUri().toURL();
                        } catch (MalformedURLException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
    }

    private final class SimpleNameSelector implements FileSelector {
        @Override
        public boolean includeFile(FileSelectInfo fileInfo) {
            try {
                return fileInfo.getFile().getType() != FileType.FOLDER &&
                        filePattern.matcher(fileInfo.getFile().getName().getBaseName()).matches();
            } catch (FileSystemException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public boolean traverseDescendents(FileSelectInfo fileInfo) {
            return true;
        }
    }
}
