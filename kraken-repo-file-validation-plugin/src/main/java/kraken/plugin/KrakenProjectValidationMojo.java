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
package kraken.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import kraken.model.project.ResourceKrakenProjectFactoryHolder;
import kraken.namespace.Namespaced;
import kraken.model.dsl.read.DSLReader;
import kraken.model.project.KrakenProjectFactory;
import kraken.model.resource.Resource;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates all Kraken Rules files (*.rules) that are available in plugin classpath or in current maven artifact.
 *
 * @author mulevicius
 */
@Mojo(
        name = "validate",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE
)
public class KrakenProjectValidationMojo extends AbstractMojo {

    private static final Logger logger = LoggerFactory.getLogger(KrakenProjectValidationMojo.class);

    @Parameter(property = "skipRulesValidation", defaultValue = "false")
    private Boolean skip;

    @Parameter(property = "baseDir")
    private String baseDir;

    @Parameter(property = "baseDirs")
    private String[] baseDirs;

    @Parameter(property = "excludes")
    private String[] excludes;

    @Component
    private PluginDescriptor pluginDescriptor;

    @Component
    private MavenProject mavenProject;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            return;
        }
        addOutputToClasspath();

        List<String> excludePatterns = excludes != null && excludes.length > 0
                ? Arrays.asList(excludes)
                : List.of();

        List<String> baseDirectories = baseDirs != null && baseDirs.length > 0
                ? new ArrayList<>(Arrays.asList(baseDirs))
                : new ArrayList<>();
        if(baseDir != null) {
            baseDirectories.add(baseDir);
        }
        if(baseDirectories.isEmpty()) {
            baseDirectories.add("");
        }

        DSLReader dslReader = new DSLReader(excludePatterns);
        Collection<Resource> resources = dslReader.read(baseDirectories);

        // by default validates only those namespaces that has Root Context
        List<String> namespacesToValidate = resources.stream()
                .filter(r -> r.getContextDefinitions().stream().anyMatch(c -> c.isRoot()))
                .map(Resource::getNamespace)
                .map(namespace -> namespace == null ? Namespaced.GLOBAL : namespace)
                .distinct()
                .collect(Collectors.toList());

        KrakenProjectFactory krakenProjectFactory = ResourceKrakenProjectFactoryHolder.getInstance()
                .createKrakenProjectFactory(resources);
        namespacesToValidate.stream()
                .peek(this::logValidation)
                .forEach(krakenProjectFactory::createKrakenProject);
    }

    private void logValidation(String namespace) {
        logger.info("Validating KrakenProject for namespace: " + namespace);
    }

    private void addOutputToClasspath() throws MojoExecutionException {
        try {
            ClassRealm realm = pluginDescriptor.getClassRealm();
            for (String element : mavenProject.getCompileClasspathElements()) {
                realm.addURL(new File(element).toURI().toURL());
            }
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Error while resolving classpath for plugin execution", e);
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("Error while resolving dependencies", e);
        }
    }
}
