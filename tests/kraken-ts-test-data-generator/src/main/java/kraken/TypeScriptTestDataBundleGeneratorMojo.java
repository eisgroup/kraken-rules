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
package kraken;

import static kraken.el.TargetEnvironment.JAVASCRIPT;
import static kraken.runtime.repository.dynamic.DynamicRuleRepositoryCacheConfig.noCaching;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import kraken.runtime.EvaluationMode;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import kraken.el.scope.Scope;
import kraken.el.serialization.ScopeSerialization;
import kraken.el.serialization.TypeRegistry;
import kraken.model.dsl.read.DSLReader;
import kraken.model.project.KrakenProject;
import kraken.model.project.builder.ResourceKrakenProjectBuilder;
import kraken.model.project.repository.StaticKrakenProjectRepository;
import kraken.model.project.scope.ScopeBuilder;
import kraken.model.project.scope.ScopeBuilderProvider;
import kraken.model.resource.Resource;
import kraken.runtime.engine.dto.bundle.EntryPointBundle;
import kraken.runtime.engine.dto.bundle.EntryPointBundleFactory;
import kraken.runtime.repository.RuntimeProjectRepositoryConfig;
import kraken.runtime.repository.factory.RuntimeProjectRepositoryFactory;
import kraken.testproduct.dimension.filter.StateDimensionFilter;
import kraken.utils.GsonUtils;
import kraken.namespace.Namespaces;

/**
 * Goal build bundles from entry point names and write then into project directory
 *
 * @goal generate
 * @phase compile
 */
@Mojo(name = "generate-ts", defaultPhase = LifecyclePhase.COMPILE)
public class TypeScriptTestDataBundleGeneratorMojo extends AbstractMojo {

    @Parameter(property = "sanityPath", required = true)
    private String sanityPath;
    @Parameter(property = "sanityBasePath", required = true)
    private String sanityBasePath;
    @Parameter(property = "sanityMetadataFile", required = true)
    private String sanityMetadataFile;
    @Parameter(property = "scopeDataPath", required = true)
    private String scopeDataPath;
    @Parameter(property = "scopeDataPath", required = true)
    private String docsDataPath;
    @Parameter(property = "resourcesDir", defaultValue = "database/gap/")
    private String resourcesDir;
    @Parameter(property = "sanity", required = true)
    private String[] sanity;
    @Parameter(property = "coverage", required = true)
    private String[] coverage;

    private final Gson gson = GsonUtils.prettyGson();

    private EntryPointBundleFactory entryPointBundleFactory;

    private SanityData sanityData;
    private KrakenProject policyKrakenProject;

    @Override
    public void execute() {
        loadConfigurationMetadata();
        initializeEntryPointBundleFactory();
        writeSanityTypings();
        writeSanityDimensions();
        writeBundles();
        writeScopeData();
    }

    private void writeScopeData() {
        ScopeBuilder scopeBuilder = ScopeBuilderProvider.forProject(policyKrakenProject);

        String scopeNameTemplate = "scope_%s_.json";

        // generate local scope
        Function<String, Scope> getScope = cdname -> scopeBuilder.buildScope(
            policyKrakenProject.getContextDefinitions().get(cdname)
        );
        Consumer<String> writeScope = scope -> write(
            scopeDataPath,
            String.format(scopeNameTemplate, scope),
            ScopeSerialization.serializeScope(getScope.apply(scope))
        );

        TypeRegistry typeRegistry = new TypeRegistry(
            getScope.apply("Policy").getAllTypes()
        );

        write(
            scopeDataPath,
            "type-registry.json",
            ScopeSerialization.serializeTypeRegistry(typeRegistry)
        );

        writeScope.accept("Policy");
        writeScope.accept("Vehicle");
        writeScope.accept("SuperReferer");
        writeScope.accept("Insured");
        writeScope.accept("BillingAddress");
        writeScope.accept("AddressInfo");

        writeScope.accept("AnubisCoverage");
        writeScope.accept("COLLCoverage");
        writeScope.accept("Insured");
        writeScope.accept("Vehicle");
        writeScope.accept("CreditCardInfo");
    }

    private void writeSanityDimensions() {
        final String dimensions = gson.toJson(
            this.sanityData.evaluations.stream()
                .filter(x -> x.context != null)
                .collect(Collectors.toMap(x -> x.id, x -> x.context))
        );
        final String license = "/*\n"
                + " *  Copyright 2021 EIS Ltd and/or one of its affiliates.\n"
                + " *\n"
                + " *  Licensed under the Apache License, Version 2.0 (the \"License\");\n"
                + " *  you may not use this file except in compliance with the License.\n"
                + " *  You may obtain a copy of the License at\n"
                + " *\n"
                + " *  http://www.apache.org/licenses/LICENSE-2.0\n"
                + " *\n"
                + " *  Unless required by applicable law or agreed to in writing, software\n"
                + " *  distributed under the License is distributed on an \"AS IS\" BASIS,\n"
                + " *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
                + " *  See the License for the specific language governing permissions and\n"
                + " *  limitations under the License.\n"
                + " */\n";
        final String typeString = "export const SANITY_DIMENSIONS = " + dimensions + ";";
        try {
            IOUtils.write(
                license.concat(typeString),
                new FileOutputStream(
                    new File(
                        sanityBasePath + "_SanityDimensions.ts"
                    )
                )
            );
        } catch (IOException e) {
            getLog().error(e);
        }
    }

    private void loadConfigurationMetadata() {
        try {
            final FileReader reader = new FileReader(sanityMetadataFile);
            final var data = String.join("", IOUtils.readLines(reader));
            this.sanityData = gson.fromJson(data, SanityData.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to write entry point bundles", e);
        }
    }

    private void writeBundles() {
        for (Evaluation evaluation : this.sanityData.evaluations) {
            write(sanityPath, evaluation);
        }
    }

    private void initializeEntryPointBundleFactory() {
        DSLReader dslReader = new DSLReader();
        Collection<Resource> resources = dslReader.read(List.of(resourcesDir));
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(resources);

        Collection<KrakenProject> krakenProjects = resources.stream()
            // builds from each namespace, can be made configurable
            .map(Resource::getNamespace)
            .distinct()
            .map(krakenProjectBuilder::buildKrakenProject)
            .collect(Collectors.toList());

        this.policyKrakenProject = krakenProjects.stream()
            .filter(kp -> kp.getNamespace().equals("Policy"))
            .findAny()
            .orElseThrow();

        RuntimeProjectRepositoryConfig config =
            new RuntimeProjectRepositoryConfig(noCaching(), List.of(new StateDimensionFilter()), List.of());
        RuntimeProjectRepositoryFactory factory =
            new RuntimeProjectRepositoryFactory(new StaticKrakenProjectRepository(krakenProjects), config, JAVASCRIPT);

        this.entryPointBundleFactory = new EntryPointBundleFactory(factory);
    }

    @SuppressWarnings("findsecbugs:PATH_TRAVERSAL_IN")
    private void writeSanityTypings() {
        final String types = this.sanityData.evaluations.stream()
            .map(e -> Namespaces.toSimpleName(e.entryPointName))
            .distinct()
            .map(s -> "\"" + s + "\"")
            .collect(Collectors.joining(" \n\t| "));
        final String typeString = "export type EntryPointName = " + types + ";";
        try {
            IOUtils.write(
                typeString,
                new FileOutputStream(
                    new File(
                        sanityBasePath + "_SanityEntryPointNames.d.ts"
                    )
                )
            );
        } catch (IOException e) {
            getLog().error(e);
        }
    }

    @SuppressWarnings("findsecbugs:PATH_TRAVERSAL_OUT")
    private void write(String folderPath, String filename, String data) {
        getLog().debug(String.format("Writing scope '%s'", filename));
        String pathname = folderPath + "/" + filename.replace(":", "_");
        final File file = new File(pathname);
        final boolean mkdirs = file.getParentFile().mkdirs();
        try (FileOutputStream stream = new FileOutputStream(file)) {
            IOUtils.write(
                data,
                stream
            );
            getLog().info(
                String.format(
                    "Scope was written to location: %s",
                    file.getAbsolutePath()
                )
            );
        } catch (IOException exception) {
            getLog().error(exception);
        }
    }

    private void write(String folderPath, Evaluation evaluation) {
        getLog().debug(String.format("Writing bundle '%s'", evaluation.entryPointName));
        String pathname = folderPath + "/" + evaluation.id + ".json";
        final File file = new File(pathname);
        final boolean mkdirs = file.getParentFile().mkdirs();
        try (FileOutputStream stream = new FileOutputStream(file)) {
            IOUtils.write(
                resolveBundleAsJsonString(
                    evaluation.entryPointName,
                    evaluation.context
                ),
                stream
            );
            getLog().info(
                String.format(
                    "EntryPointBundle was written to location: %s",
                    file.getAbsolutePath()
                )
            );
        } catch (IOException exception) {
            getLog().error(exception);
        }
    }

    private String resolveBundleAsJsonString(String entryPointName, Map<String, Object> context) {
        var defaultDimensions = new HashMap<String, Object>();
        EntryPointBundle bundle = entryPointBundleFactory.build(
            entryPointName,
            context == null ? defaultDimensions : context,
            Set.of(),
            EvaluationMode.ALL
        );
        // strip engine version to avoid version incompatibility warnings in local testing environments and snapshots
        bundle = new EntryPointBundle(bundle.getEvaluation(), bundle.getExpressionContext(), null);
        return gson.toJson(sortJson(gson.toJsonTree(bundle)));
    }

    private JsonElement sortJson(JsonElement jsonElement) {
        if (jsonElement.isJsonArray()) {
            List<JsonElement> list = new ArrayList<>();
            for (JsonElement elementInArray : jsonElement.getAsJsonArray()) {
                list.add(sortJson(elementInArray));
            }
            return gson.toJsonTree(list);
        } else if (jsonElement.isJsonObject()) {
            LinkedHashMap<String, JsonElement> sortedObject = jsonElement.getAsJsonObject().entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> {
                    entry.setValue(sortJson(entry.getValue()));
                    return entry;
                })
                .collect(LinkedHashMap::new,
                    (map, entrySet) -> map.put(entrySet.getKey(), entrySet.getValue()),
                    Map::putAll);
            return gson.toJsonTree(sortedObject);
        }
        return jsonElement;
    }

    private static class SanityData {
        Collection<Evaluation> evaluations;
    }

    private static class Evaluation {
        String id;
        String entryPointName;
        Map<String, Object> context;

        public Evaluation(String s) {
            this.entryPointName = s;
            this.id = s;
        }
    }
}
