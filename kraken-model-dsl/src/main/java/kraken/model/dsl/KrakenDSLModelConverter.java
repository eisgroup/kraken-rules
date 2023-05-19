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
package kraken.model.dsl;

import static kraken.model.dsl.KrakenDSLModelContextConverter.convertContexts;
import static kraken.model.dsl.KrakenDSLModelEntryPointConverter.convertEntryPoints;
import static kraken.model.dsl.KrakenDSLModelExternalContextConverter.convertExternalContext;
import static kraken.model.dsl.KrakenDSLModelExternalContextDefinitionConverter.convertExternalContextDefinitions;
import static kraken.model.dsl.KrakenDSLModelFunctionConverter.convertFunctions;
import static kraken.model.dsl.KrakenDSLModelFunctionSignatureConverter.convertFunctionSignatures;
import static kraken.model.dsl.KrakenDSLModelRuleConverter.convertRules;
import static kraken.model.dsl.KrakenDslModelDimensionConverter.convertDimensions;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import kraken.model.Dimension;
import kraken.model.Function;
import kraken.model.FunctionSignature;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.dsl.model.DSLImportReference;
import kraken.model.dsl.model.DSLModel;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.resource.Resource;
import kraken.model.resource.RuleImport;

/**
 * Converts {@link DSLModel} to {@link Resource} by converting rule, context, external context
 * and entry point DSL representations to kraken model.
 *
 * @author mulevicius
 */
public class KrakenDSLModelConverter {

    private KrakenDSLModelConverter() {
    }

    public static Resource toResource(DSLModel dsl, URI uri) {

        List<ContextDefinition> contextDefinitions = convertContexts(dsl);
        List<EntryPoint> entryPoints = convertEntryPoints(dsl, uri);
        List<Rule> rules = convertRules(dsl, uri);
        List<RuleImport> ruleImports = convertRuleImports(dsl.getRuleImports());
        List<ExternalContextDefinition> externalContextDefinition = convertExternalContextDefinitions(dsl);
        ExternalContext externalContext = convertExternalContext(dsl, externalContextDefinition);
        List<FunctionSignature> functionSignatures = convertFunctionSignatures(dsl);
        List<Function> functions = convertFunctions(dsl);
        List<Dimension> dimensions = convertDimensions(dsl);

        return new Resource(
            dsl.getNamespace(),
            contextDefinitions,
            entryPoints,
            rules,
            dsl.getIncludes(),
            ruleImports,
            externalContext,
            externalContextDefinition,
            functionSignatures,
            functions,
            dimensions,
            uri
        );
    }

    private static List<RuleImport> convertRuleImports(Collection<DSLImportReference> importReferences){
        return importReferences.stream()
                .flatMap(imp -> imp.getImportNames().stream().map(i -> new RuleImport(imp.getNamespaceName(), i)))
                .collect(Collectors.toList());
    }

}
