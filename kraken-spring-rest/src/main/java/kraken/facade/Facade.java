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
package kraken.facade;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import kraken.config.TestAppConfig;
import kraken.config.TestAppPropertiesConfig;
import kraken.el.scope.Scope;
import kraken.el.serialization.ScopeSerialization;
import kraken.el.serialization.TypeRegistry;
import kraken.exceptions.KrakenRestRepoException;
import kraken.model.BundleRequest;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.project.KrakenProject;
import kraken.model.project.repository.KrakenProjectRepository;
import kraken.model.project.scope.ScopeBuilder;
import kraken.model.project.scope.ScopeBuilderProvider;
import kraken.runtime.engine.dto.bundle.EntryPointBundle;
import kraken.runtime.engine.dto.bundle.EntryPointBundleFactory;
import kraken.testproduct.TestProduct;
import kraken.utils.GsonUtils;

/**
 * Controller class that, has endpoints to all kraken resources.
 *
 * @author psurinin
 * @since 1.1.0
 */
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class Facade {

    @Autowired
    private TestAppPropertiesConfig properties;

    @Autowired
    private EntryPointBundleFactory entryPointBundleFactory;

    @Autowired
    private KrakenProjectRepository krakenProjectRepository;

    @ApiOperation(value = "Get EntryPointBundle by name and dimensions", response = String.class)
    @PostMapping(value = "/bundle/{entrypoint}", produces = "application/json")
    public String loadBundle(
            @RequestBody BundleRequest request,
            @PathVariable("entrypoint") String entrypoint,
            @RequestParam(name = "isDelta", defaultValue = "false") boolean isDelta
    ) {
        final EntryPointBundle bundle = entryPointBundleFactory.build(
                TestProduct.toEntryPointName(entrypoint),
                Optional.ofNullable(request.getDimensions()).orElse(new HashMap<>()),
                isDelta
        );
        return GsonUtils.prettyGson().toJson(bundle);
    }

    @ApiOperation(value = "Get EntryPoint by name", response = EntryPoint.class)
    @GetMapping("/entrypoint/{name}")
    public EntryPoint loadEntryPoint(@PathVariable String name) {
        return getKrakenProject().getEntryPoints().stream()
                .filter(ep -> ep.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new KrakenRestRepoException(name, EntryPoint.class));
    }

    @ApiOperation(value = "Get Rule by name", response = Rule.class)
    @GetMapping("/rule/{name}")
    public Rule loadRule(@PathVariable String name) {
        return getKrakenProject().getRules().stream()
                .filter(rule -> rule.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new KrakenRestRepoException(name, Rule.class));
    }

    @ApiOperation(value = "Get ContextDefinition by name and dimensions", response = ContextDefinition.class)
    @GetMapping("/context/{name}")
    public ContextDefinition loadContext(@PathVariable String name) {
        return Optional.ofNullable(getKrakenProject().getContextProjection(name))
                .orElseThrow(() -> new KrakenRestRepoException(name, ContextDefinition.class));
    }

    @ApiOperation(value = "Get all EntryPoints", response = EntryPoint.class)
    @GetMapping("/entrypoint/all")
    public Collection<EntryPoint> loadEntryPoints() {
        return getKrakenProject().getEntryPoints();
    }

    @ApiOperation(value = "Get all Rules", response = EntryPoint.class)
    @GetMapping("/rule/all")
    public Collection<Rule> loadRules() {
        return getKrakenProject().getRules();
    }

    @ApiOperation(value = "Get all Context definitions", response = ContextDefinition.class)
    @GetMapping("/context/all")
    public Collection<ContextDefinition> loadEntryContexts() {
        return getKrakenProject().getContextDefinitions().values();
    }

    @GetMapping("/scope/type-registry")
    public String getTypeRegistry() {
        KrakenProject krakenProject = krakenProjectRepository.getKrakenProject("Policy");
        ScopeBuilder scopeBuilder = ScopeBuilderProvider.forProject(krakenProject);
        Scope scope = scopeBuilder.buildScope(krakenProject.getContextProjection("Policy"));
        return ScopeSerialization.serializeTypeRegistry(new TypeRegistry(
            scope.getParentScope().getAllTypes(),
            "Policy"
        ));
    }


    @GetMapping("/scope/{contextName}")
    public String getScope(@PathVariable String contextName) {
        KrakenProject krakenProject = krakenProjectRepository.getKrakenProject("Policy");
        ScopeBuilder scopeBuilder = ScopeBuilderProvider.forProject(krakenProject);
        Scope scope = scopeBuilder.buildScope(krakenProject.getContextProjection(contextName));
        return ScopeSerialization.serializeScope(scope);
    }


        @GetMapping("/build/properties")
    public Map<String, String> getBuildProperties() {
        Map<String, String> buildProperties = new HashMap<>();
        buildProperties.put("number", properties.getBuildNumber());
        buildProperties.put("date", properties.getBuildDate());
        buildProperties.put("startTime", properties.getBuildStartTime());
        buildProperties.put("revision", properties.getBuildRevision());
        buildProperties.put("version", properties.getProjectVersion());
        buildProperties.put("branch", properties.getBuildRevision());
        return buildProperties;
    }

    private KrakenProject getKrakenProject() {
        return krakenProjectRepository.getKrakenProject(TestAppConfig.NAMESPACE);
    }
}
