/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.converter;

import static kraken.model.FunctionSignature.format;

import java.util.Map;

import kraken.converter.translation.KrakenExpressionTranslator;
import kraken.el.KrakenKel;
import kraken.el.TargetEnvironment;
import kraken.el.functionregistry.FunctionHeader;
import kraken.el.functionregistry.FunctionRegistry;
import kraken.el.functionregistry.JavaFunction;
import kraken.model.FunctionSignature;
import kraken.model.dimensions.DimensionSetService;
import kraken.model.project.KrakenProject;
import kraken.model.project.dependencies.RuleDependencyExtractor;
import kraken.model.project.scope.ScopeBuilder;
import kraken.model.project.scope.ScopeBuilderProvider;
import kraken.runtime.model.project.RuntimeKrakenProject;

/**
 * Converts designtime {@link KrakenProject} to {@link RuntimeKrakenProject} for {@link TargetEnvironment}
 *
 * @author mulevicius
 */
public class KrakenProjectConverter {

    private final KrakenProject krakenProject;

    private final RuleConverter ruleConverter;

    private final EntryPointConverter entryPointConverter;

    private final ContextDefinitionConverter contextDefinitionConverter;

    private final FunctionConverter functionConverter;

    private final ScopeBuilder scopeBuilder;

    public KrakenProjectConverter(KrakenProject krakenProject, TargetEnvironment targetEnvironment) {
        this.krakenProject = krakenProject;

        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        KrakenExpressionTranslator krakenExpressionTranslator = new KrakenExpressionTranslator(krakenProject,
            targetEnvironment, ruleDependencyExtractor);
        DimensionSetService dimensionSetService = new DimensionSetService(krakenProject);

        this.ruleConverter = new RuleConverter(
            ruleDependencyExtractor,
            krakenExpressionTranslator,
            dimensionSetService,
            krakenProject.getNamespace()
        );
        this.entryPointConverter = new EntryPointConverter(dimensionSetService, krakenProject.getNamespace());
        this.contextDefinitionConverter = new ContextDefinitionConverter(krakenProject, krakenExpressionTranslator);

        this.functionConverter = new FunctionConverter(krakenExpressionTranslator);

        this.scopeBuilder = ScopeBuilderProvider.forProject(krakenProject);
    }

    public RuntimeKrakenProject convert() {
        ensureThatFunctionExistsForEachFunctionSignature();

        return new RuntimeKrakenProject(
            krakenProject.getIdentifier(),
            krakenProject.getNamespace(),
            krakenProject.getRootContextName(),
            contextDefinitionConverter.convert(krakenProject.getContextDefinitions()),
            entryPointConverter.convert(krakenProject.getEntryPoints()),
            ruleConverter.convert(krakenProject.getRules()),
            functionConverter.convert(krakenProject.getFunctions())
        );
    }

    private void ensureThatFunctionExistsForEachFunctionSignature() {
        Map<FunctionHeader, JavaFunction> functions = FunctionRegistry.getFunctions(KrakenKel.EXPRESSION_TARGET);

        for (FunctionSignature functionSignature : krakenProject.getFunctionSignatures()) {
            FunctionHeader header = FunctionSignature.toHeader(functionSignature);
            if (!functions.containsKey(header)) {
                String template = "Critical error encountered while converting Kraken Project '%s' "
                    + "from design-time to runtime model: function signature '%s' is defined "
                    + "but implementation for this function does not exist in system";
                String message = String.format(template, krakenProject.getNamespace(), format(functionSignature));
                throw new KrakenProjectConvertionException(message);
            }
            JavaFunction javaFunction = functions.get(header);
            if (!isFunctionMatchesSignature(javaFunction, functionSignature)) {
                String template = "Critical error encountered while converting Kraken Project '%s' "
                    + "from design-time to runtime model: function signature '%s' is defined "
                    + "but implementation of this function is not compatible with defined signature";
                String message = String.format(template, krakenProject.getNamespace(), format(functionSignature));
                throw new KrakenProjectConvertionException(message);
            }
        }
    }

    private boolean isFunctionMatchesSignature(JavaFunction function, FunctionSignature signature) {
        var functionSymbol = scopeBuilder.buildFunctionSymbol(function);
        var signatureSymbol = scopeBuilder.buildFunctionSymbol(signature);
        return functionSymbol.equals(signatureSymbol);
    }

}
