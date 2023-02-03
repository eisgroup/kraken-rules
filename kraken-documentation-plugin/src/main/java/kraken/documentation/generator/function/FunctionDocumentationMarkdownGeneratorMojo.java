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
package kraken.documentation.generator.function;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import kraken.documentation.FunctionDocumentationWriter;
import kraken.model.dsl.read.DSLReader;
import kraken.model.project.builder.ResourceKrakenProjectBuilder;
import kraken.model.resource.Resource;
import kraken.namespace.Namespaced;

/**
 * A plugin which generates markdown files from Kraken function documentation.
 * Artifacts (java implementations and dsl definitions) containing kraken functions must be provided as
 * a dependencies for plugin. It is assumed that DSL function definitions are valid.
 * <p>
 * Plugin Configuration example:
 * <pre>
 *     <code>
 *       <plugin>
 *          <groupId>kraken</groupId>
 *          <artifactId>kraken-documentation-plugin</artifactId>
 *          <version>${project.version}</version>
 *             <executions>
 *                <execution>
 *                   <id>generate-docs</id>
 *                   <phase>package</phase>
 *                   <goals>
 *                      <goal>generate-function-documentation</goal>
 *                   </goals>
 *                   <configuration>
 *                      <namespaces>
 *                          <namespace>Policy</namespace>
 *                      </namespaces>
 *                   </configuration>
 *                 </execution>
 *              </executions>
 *              <dependencies>
 *                 <dependency>
 *                    <!-- Artifacts containing kraken functions to generate documentation for -->
 *                    <groupId>kraken</groupId>
 *                    <artifactId>kraken-expression-language</artifactId>
 *                    <version>${project.version}</version>
 *                  </dependency>
 *              </dependencies>
 *       </plugin>
 *     </code>
 * </pre>
 *
 * @author Tomas Dapkunas
 * @since 1.24
 */
@Mojo(name = "generate-function-documentation", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public final class FunctionDocumentationMarkdownGeneratorMojo extends AbstractMojo {

    @Component
    private MavenProject project;

    @Parameter(
        property = "outputDirectory",
        defaultValue = "${project.build.directory}/kraken-function-docs"
    )
    private File outputDirectory;

    @Parameter(property = "namespaces")
    private String[] namespaces;

    @Parameter(property = "baseDirs")
    private String[] baseDirs;

    @Parameter(property = "excludes")
    private String[] excludes;

    @Override
    public void execute() {
        List<String> excludePatterns = excludes != null && excludes.length > 0
            ? Arrays.asList(excludes)
            : new ArrayList<>();
        List<String> baseDirectories = baseDirs != null && baseDirs.length > 0
            ? Arrays.asList(baseDirs)
            : List.of("");
        List<String> namespaceList = namespaces != null && namespaces.length > 0
            ? Arrays.asList(namespaces)
            : List.of(Namespaced.GLOBAL);

        Collection<Resource> resources = new DSLReader(excludePatterns).read(baseDirectories);
        var krakenProjectBuilder = new ResourceKrakenProjectBuilder(resources);
        var writer = new FunctionDocumentationWriter(krakenProjectBuilder, Map.of());
        Path outputDirectoryPath = outputDirectory.toPath();
        for(String namespace : namespaceList) {
            writer.write(namespace, outputDirectoryPath);
        }
    }

}
